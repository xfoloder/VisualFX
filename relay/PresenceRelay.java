import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** In-memory VisualFX relay. Runs separately from Minecraft. */
public final class PresenceRelay {
    private static final String ROOT = "/v1/presence/";
    private static final long TTL = TimeUnit.SECONDS.toNanos(9);
    private static final int MAX_ROOMS = 256, MAX_PEERS = 64;
    private final Map<String, Map<String, Peer>> rooms = new HashMap<>();

    private record Peer(String status, long seenAt) {}

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 27640;
        System.setProperty("sun.net.httpserver.maxReqTime", "6");
        var server = HttpServer.create(new InetSocketAddress(host, port), 32);
        server.setExecutor(new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(64)));
        var relay = new PresenceRelay();
        server.createContext(ROOT, relay::handle);
        server.start();
        var cleanup = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().factory());
        cleanup.scheduleAtFixedRate(relay::expire, 3, 3, TimeUnit.SECONDS);
        System.out.println("VisualFX relay listening on " + host + ":" + server.getAddress().getPort());
    }

    private void handle(HttpExchange request) throws IOException {
        try (request) {
            if (!request.getRequestMethod().equals("POST")) { respond(request, 405, ""); return; }
            String path = request.getRequestURI().getPath();
            String scope = path.substring(ROOT.length());
            String auth = request.getRequestHeaders().getFirst("Authorization");
            if (!scope.matches("[0-9a-f]{64}") || auth == null || !auth.matches("Bearer [A-Za-z0-9_-]{16,96}")) {
                respond(request, 403, ""); return;
            }
            byte[] input = request.getRequestBody().readNBytes(100);
            String[] fields = new String(input, StandardCharsets.UTF_8).split("\t", -1);
            if (input.length >= 100 || fields.length != 2 || !fields[0].matches("[0-9a-f]{64}")
                    || !fields[1].matches("[A-Z_]{1,30}")) {
                respond(request, 400, ""); return;
            }
            String rows = update(key(auth, scope), fields[0], fields[1]);
            respond(request, rows == null ? 429 : 200, rows == null ? "" : rows);
        }
    }

    private synchronized String update(String scope, String id, String activity) {
        long now = System.nanoTime();
        expire(now);
        var peers = rooms.get(scope);
        if (peers == null) {
            if (rooms.size() >= MAX_ROOMS) return null;
            peers = new HashMap<>();
            rooms.put(scope, peers);
        }
        if (peers.size() >= MAX_PEERS && !peers.containsKey(id)) return null;
        peers.put(id, new Peer(activity, now));
        StringBuilder body = new StringBuilder(peers.size() * 88);
        peers.forEach((peer, state) -> body.append(peer).append('\t').append(state.status).append('\n'));
        return body.toString();
    }

    private synchronized void expire() { expire(System.nanoTime()); }

    private void expire(long now) {
        rooms.values().removeIf(peers -> {
            peers.values().removeIf(peer -> now - peer.seenAt > TTL);
            return peers.isEmpty();
        });
    }

    private static String key(String auth, String scope) {
        try {
            byte[] bytes = (auth + '\n' + scope).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    private static void respond(HttpExchange request, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        request.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        request.getResponseHeaders().set("Cache-Control", "no-store");
        if (bytes.length == 0) { request.sendResponseHeaders(status, -1); return; }
        request.sendResponseHeaders(status, bytes.length);
        request.getResponseBody().write(bytes);
    }
}
