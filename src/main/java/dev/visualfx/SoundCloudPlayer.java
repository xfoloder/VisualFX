package dev.visualfx;

import com.google.gson.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Lazy loopback-only bridge to the official SoundCloud widget. No SoundCloud credentials or audio extraction. */
final class SoundCloudPlayer {
    static final SoundCloudPlayer INSTANCE=new SoundCloudPlayer();
    private final Gson json=new Gson();
    private HttpServer server;
    private ExecutorService executor;
    private String base,origin,client="";
    private long sequence,lastStatus;
    private final ArrayDeque<Command> commands=new ArrayDeque<>();
    private record Command(long id,String action,String text,double value){}
    record Status(boolean ready,boolean paused,String title,String artist,long position,long duration,String error){}
    private static final Status EMPTY=new Status(false,true,"","",0,0,"");
    private volatile Status status=EMPTY;
    synchronized String localUrl(){return server==null?"":origin+base+"/";}
    synchronized boolean connected(){return server!=null&&System.currentTimeMillis()-lastStatus<5000;}
    Status status(){return status;}
    static String validate(String input){
        try{
            URI uri=URI.create(input.trim());String host=uri.getHost();
            if(!"https".equalsIgnoreCase(uri.getScheme())||host==null||!(host.equalsIgnoreCase("soundcloud.com")||host.equalsIgnoreCase("www.soundcloud.com"))||uri.getUserInfo()!=null||uri.getPort()!=-1||uri.getPath()==null||uri.getPath().split("/").length<3)
                throw new IllegalArgumentException();
            if(input.length()>2048)throw new IllegalArgumentException();
            return uri.toASCIIString();
        }catch(RuntimeException e){throw new IllegalArgumentException("Нужна HTTPS-ссылка soundcloud.com/автор/трек или плейлист.");}
    }
    synchronized void start() throws IOException {
        if(server!=null)return;
        HttpServer created=HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),0),8);
        executor=Executors.newFixedThreadPool(2,r->{Thread t=new Thread(r,"VisualFX-SoundCloud");t.setDaemon(true);return t;});
        base="/session/"+UUID.randomUUID();origin="http://127.0.0.1:"+created.getAddress().getPort();
        created.createContext("/",this::handle);created.setExecutor(executor);created.start();server=created;
        if(!VisualFX.config.soundCloudUrl.isBlank()){
            try{command("load",validate(VisualFX.config.soundCloudUrl),VisualFX.config.musicVolume);}
            catch(IllegalArgumentException ignored){/* An invalid saved URL can be replaced in the player screen. */}
        }
    }
    synchronized void command(String action,String text,double value){
        if(server==null)return;
        commands.addLast(new Command(++sequence,action,text,value));
        while(commands.size()>64)commands.removeFirst();
    }
    void load(String input){
        String url=validate(input);VisualFX.config.soundCloudUrl=url;VisualFX.config.save();
        command("load",url,VisualFX.config.musicVolume);
    }
    synchronized void stop(){
        if(server!=null)server.stop(0);server=null;
        if(executor!=null)executor.shutdownNow();executor=null;
        commands.clear();client="";lastStatus=0;sequence=0;status=EMPTY;
    }
    private void handle(HttpExchange exchange) throws IOException {
        try{
            String path=exchange.getRequestURI().getPath(),expectedOrigin;
            synchronized(this){expectedOrigin=origin;}
            String requestOrigin=exchange.getRequestHeaders().getFirst("Origin");
            if(!exchange.getRemoteAddress().getAddress().isLoopbackAddress()||!exchange.getRequestHeaders().getFirst("Host").equals(URI.create(expectedOrigin).getAuthority())||(requestOrigin!=null&&!requestOrigin.equals(expectedOrigin))){reply(exchange,403,"text/plain","Forbidden");return;}
            if(!path.startsWith(base+"/")){reply(exchange,404,"text/plain","Not found");return;}
            String endpoint=path.substring(base.length());
            if(endpoint.equals("/")&&exchange.getRequestMethod().equals("GET")){
                String page;
                try(InputStream in=SoundCloudPlayer.class.getResourceAsStream("/assets/visualfx/soundcloud.html")){
                    if(in==null)throw new IOException("Missing SoundCloud page");page=new String(in.readAllBytes(),StandardCharsets.UTF_8);
                }
                reply(exchange,200,"text/html",page.replace("__BASE_JSON__",json.toJson(base)));return;
            }
            if(endpoint.equals("/poll")&&exchange.getRequestMethod().equals("GET")){
                Map<String,String> query=new HashMap<>();String raw=exchange.getRequestURI().getRawQuery();
                if(raw!=null)for(String part:raw.split("&")){String[] pair=part.split("=",2);if(pair.length==2)query.put(pair[0],URLDecoder.decode(pair[1],StandardCharsets.UTF_8));}
                String id=query.getOrDefault("client","");long after=Long.parseLong(query.getOrDefault("after","0"));
                JsonObject response=new JsonObject();JsonArray list=new JsonArray();
                synchronized(this){
                    if(!id.equals(client)){response.addProperty("superseded",true);}
                    else for(Command command:commands)if(command.id>after)list.add(json.toJsonTree(command));
                }
                response.add("commands",list);reply(exchange,200,"application/json",json.toJson(response));return;
            }
            if((endpoint.equals("/claim")||endpoint.equals("/status"))&&exchange.getRequestMethod().equals("POST")){
                byte[] bytes=exchange.getRequestBody().readNBytes(8193);
                if(bytes.length>8192){reply(exchange,413,"text/plain","Too large");return;}
                JsonObject o=JsonParser.parseString(new String(bytes,StandardCharsets.UTF_8)).getAsJsonObject();
                String id=string(o,"client",80);
                synchronized(this){
                    if(endpoint.equals("/claim")){client=id;status=EMPTY;}
                    if(!id.isEmpty()&&id.equals(client)){
                        lastStatus=System.currentTimeMillis();
                        if(endpoint.equals("/status"))status=new Status(bool(o,"ready"),bool(o,"paused"),string(o,"title",160),string(o,"artist",100),number(o,"position"),number(o,"duration"),string(o,"error",240));
                    }
                }
                reply(exchange,200,"application/json","{}");return;
            }
            reply(exchange,405,"text/plain","Method not allowed");
        }catch(RuntimeException e){reply(exchange,400,"text/plain","Bad request");}
        finally{exchange.close();}
    }
    private static String string(JsonObject o,String key,int max){String s=o.has(key)?o.get(key).getAsString():"";return s.substring(0,Math.min(s.length(),max)).replaceAll("[\\p{Cntrl}]"," ");}
    private static boolean bool(JsonObject o,String key){return o.has(key)&&o.get(key).getAsBoolean();}
    private static long number(JsonObject o,String key){return o.has(key)?Math.clamp(o.get(key).getAsLong(),0,86_400_000):0;}
    private static void reply(HttpExchange e,int code,String mime,String body)throws IOException{
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
        var headers=e.getResponseHeaders();headers.set("Content-Type",mime+"; charset=utf-8");headers.set("Cache-Control","no-store");
        headers.set("X-Content-Type-Options","nosniff");headers.set("Referrer-Policy","no-referrer");headers.set("X-Frame-Options","DENY");
        headers.set("Content-Security-Policy","default-src 'none'; script-src 'unsafe-inline' https://w.soundcloud.com; style-src 'unsafe-inline'; frame-src https://w.soundcloud.com; connect-src 'self'; base-uri 'none'; form-action 'none'");
        e.sendResponseHeaders(code,bytes.length);e.getResponseBody().write(bytes);
    }
}
