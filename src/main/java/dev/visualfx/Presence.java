package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class Presence {
    public enum Activity {
        IN_GAME(""), NEON_ECHO("Neon Echo"), TARGET_ESP("Target ESP"), TARGET_HUD("Target HUD"),
        EFFECT_STATUS("Effect Status"), WATERMARK("Watermark"), OPTIMIZATION("Оптимизация"),
        SOUND_CLOUD("SoundCloud"), DEATH_EFFECT("Death Effect"), HIT_EFFECT("Hit Effect"), MENU("Меню"),
        ITEM_PHYSICS("Item Physics"), ITEM_SCROLLER("Item Scroller"), SEASONAL("Частицы"),
        PRESENCE("Связь клиентов"), ADJUSTING("Меняет параметр"), COLORS("Редактор цветов"),
        HUD_EDITOR("Редактор HUD");

        final String label;
        final Component suffix;
        Activity(String label) {
            this.label = label;
            var cat = Component.literal("\uE002").setStyle(Style.EMPTY
                    .withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("visualfx", "presence")))
                    .withColor(0x39FF75));
            var icon = Component.literal(" ").append(cat);
            if (!label.isEmpty()) icon.append(Component.literal(" " + label).withColor(0xA7E7B8));
            suffix = icon;
        }
    }

    private record Connection(URI endpoint, String key, String server, UUID player) {}
    private static final long HEARTBEAT_NS = 2_500_000_000L;
    private static final long MIN_GAP_NS = 400_000_000L;
    private static final int MAX_RESPONSE = 8192;
    private Connection active;
    private CompletableFuture<Map<String, Activity>> request;
    private static volatile Map<UUID, Activity> peers = Map.of();
    private static volatile String status = "Связь выключена";
    private long sentAt = Long.MIN_VALUE / 2;
    private Activity lastSent;

    void tick(Minecraft mc) {
        var config = VisualFX.config;
        var server = mc.getCurrentServer();
        if (!config.presenceEnabled || mc.player == null || server == null) {
            clear();
            status = config.presenceEnabled ? "Подключитесь к серверу Minecraft" : "Связь выключена";
            return;
        }
        if (active == null || !active.player.equals(mc.player.getUUID())
                || !active.server.equalsIgnoreCase(server.ip) || !active.key.equals(config.presenceKey)
                || !active.endpoint.toString().equals(config.presenceUrl)) {
            clear();
            if (!validKey(config.presenceKey) || !validUrl(config.presenceUrl)) {
                status = "Укажите адрес и ключ комнаты";
                return;
            }
            active = new Connection(URI.create(config.presenceUrl), config.presenceKey, server.ip, mc.player.getUUID());
        }
        if (request != null && request.isDone()) {
            try {
                var rows = request.join();
                var match = new HashMap<UUID, Activity>();
                var network = mc.getConnection();
                if (network != null) for (var other : network.getOnlinePlayers()) {
                    UUID uuid = other.getProfile().id();
                    if (uuid.equals(active.player)) continue;
                    Activity activity = rows.get(hash(active.key, uuid.toString()));
                    if (activity != null) match.put(uuid, activity);
                }
                peers = Map.copyOf(match);
                status = "В комнате: " + (match.size() + 1);
            } catch (RuntimeException failure) {
                peers = Map.of();
                status = "Ретранслятор недоступен";
            } finally {
                request = null;
            }
        }
        Activity activity = config.presenceShareMenu ? currentActivity(mc) : Activity.IN_GAME;
        long now = System.nanoTime();
        if (request != null || now - sentAt < (lastSent == activity ? HEARTBEAT_NS : MIN_GAP_NS)) return;
        var connection = active;
        request = new CompletableFuture<>();
        var future = request;
        lastSent = activity;
        sentAt = now;
        Thread.ofVirtual().name("VisualFX-presence").start(() -> {
            try { future.complete(exchange(connection, activity)); }
            catch (Exception error) { future.completeExceptionally(error); }
        });
    }

    private static Activity currentActivity(Minecraft mc) {
        return switch (mc.gui.screen()) {
            case TerminalScreen menu -> menu.presenceActivity();
            case ColorEditorScreen ignored -> Activity.COLORS;
            case HudEditorScreen ignored -> Activity.HUD_EDITOR;
            case SoundCloudScreen ignored -> Activity.SOUND_CLOUD;
            case PresenceScreen ignored -> Activity.PRESENCE;
            case null, default -> Activity.IN_GAME;
        };
    }

    void clear() {
        if (request != null) request.cancel(false);
        request = null;
        active = null;
        peers = Map.of();
        sentAt = Long.MIN_VALUE / 2;
        lastSent = null;
    }

    public static Component decorate(Component name, UUID player) {
        Activity activity = peers.get(player);
        return activity == null ? name : name.copy().append(activity.suffix);
    }

    public static Activity activity(UUID player) { return peers.get(player); }
    static String status() { return status; }

    static boolean validKey(String key) { return key != null && key.matches("[A-Za-z0-9_-]{16,96}"); }

    static boolean validUrl(String address) {
        if (address == null || address.isBlank() || address.length() > 200) return false;
        try {
            URI uri = URI.create(address);
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))) return false;
            if ("https".equals(uri.getScheme())) return true;
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            return "http".equals(uri.getScheme()) && (host.equals("localhost") || host.equals("[::1]")
                    || host.matches("(?:127|10)\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
                    || host.matches("192\\.168\\.\\d{1,3}\\.\\d{1,3}")
                    || host.matches("172\\.(?:1[6-9]|2[0-9]|3[01])\\.\\d{1,3}\\.\\d{1,3}"));
        } catch (IllegalArgumentException invalid) { return false; }
    }

    private static Map<String, Activity> exchange(Connection connection, Activity activity) throws IOException {
        String scope = hash(connection.key, connection.server.toLowerCase(Locale.ROOT));
        var endpoint = connection.endpoint.resolve("/v1/presence/" + scope);
        var http = (HttpURLConnection) endpoint.toURL().openConnection();
        try {
            http.setInstanceFollowRedirects(false);
            http.setConnectTimeout(2500);
            http.setReadTimeout(2800);
            http.setRequestMethod("POST");
            http.setRequestProperty("Authorization", "Bearer " + connection.key);
            http.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            http.setDoOutput(true);
            byte[] body = (hash(connection.key, connection.player.toString()) + "\t" + activity.name()).getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(body.length);
            try (var output = http.getOutputStream()) { output.write(body); }
            if (http.getResponseCode() != 200) throw new IOException("Presence request rejected");
            byte[] data;
            try (var input = http.getInputStream()) { data = input.readNBytes(MAX_RESPONSE + 1); }
            if (data.length > MAX_RESPONSE) throw new IOException("Oversize presence response");
            var result = new HashMap<String, Activity>();
            for (String line : new String(data, StandardCharsets.UTF_8).split("\n")) {
                String[] parts = line.split("\t", 2);
                if (parts.length != 2 || !parts[0].matches("[0-9a-f]{64}")) continue;
                try { result.put(parts[0], Activity.valueOf(parts[1])); }
                catch (IllegalArgumentException ignored) {}
            }
            return result;
        } finally { http.disconnect(); }
    }

    static String hash(String key, String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(key.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}
