package dev.visualfx;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import com.google.gson.*;
public final class BridgeTest {
 public static void main(String[] args)throws Exception{
  for(String invalid:new String[]{"http://soundcloud.com/a/b","https://evil.example/a/b","https://soundcloud.com.evil.example/a/b","https://user@soundcloud.com/a/b","https://soundcloud.com:443/a/b","javascript:alert(1)","https://soundcloud.com/a"}){
   try{SoundCloudPlayer.validate(invalid);throw new AssertionError("Accepted invalid URL");}catch(IllegalArgumentException expected){}
  }
  assert SoundCloudPlayer.validate("https://soundcloud.com/artist/sets/playlist").contains("/sets/");
  var p=SoundCloudPlayer.INSTANCE;var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
  try{
   p.start();String base=p.localUrl();
   var page=http.send(HttpRequest.newBuilder(URI.create(base)).GET().build(),HttpResponse.BodyHandlers.ofString());
   assert page.statusCode()==200;assert page.body().contains("SC.Widget");assert !page.body().contains("__BASE_JSON__");
   var bad=http.send(HttpRequest.newBuilder(URI.create(base)).header("Origin","https://evil.example").GET().build(),HttpResponse.BodyHandlers.ofString());assert bad.statusCode()==403;
   var notFound=http.send(HttpRequest.newBuilder(URI.create(base).resolve("/wrong-session/")).GET().build(),HttpResponse.BodyHandlers.ofString());assert notFound.statusCode()==404;
   String cid="test-client";
   var claim=http.send(HttpRequest.newBuilder(URI.create(base+"claim")).POST(HttpRequest.BodyPublishers.ofString("{\"client\":\""+cid+"\"}")).build(),HttpResponse.BodyHandlers.ofString());assert claim.statusCode()==200;
   p.load("https://soundcloud.com/artist/track");p.command("volume","",42);
   var poll=http.send(HttpRequest.newBuilder(URI.create(base+"poll?client="+cid+"&after=0")).GET().build(),HttpResponse.BodyHandlers.ofString());
   var data=JsonParser.parseString(poll.body()).getAsJsonObject().getAsJsonArray("commands");assert data.size()==2;assert data.get(0).getAsJsonObject().get("action").getAsString().equals("load");
   var status=http.send(HttpRequest.newBuilder(URI.create(base+"status")).POST(HttpRequest.BodyPublishers.ofString("{\"client\":\"test-client\",\"ready\":true,\"paused\":false,\"title\":\"Тест\",\"position\":1234,\"duration\":9999}")).build(),HttpResponse.BodyHandlers.ofString());
   assert status.statusCode()==200;assert p.connected();assert p.status().ready();assert p.status().title().equals("Тест");assert p.status().position()==1234;
   var other=http.send(HttpRequest.newBuilder(URI.create(base+"poll?client=other&after=0")).GET().build(),HttpResponse.BodyHandlers.ofString());assert other.body().contains("superseded");
   for(int i=0;i<80;i++)p.command("volume","",i);
   var bounded=http.send(HttpRequest.newBuilder(URI.create(base+"poll?client="+cid+"&after=0")).GET().build(),HttpResponse.BodyHandlers.ofString());assert JsonParser.parseString(bounded.body()).getAsJsonObject().getAsJsonArray("commands").size()==64;
   p.stop();assert !p.connected();assert p.localUrl().isEmpty();assert !p.status().ready();
   System.out.println("PASS: URL validation, page resource, origin/session isolation, command order, status sync, bounded queue, shutdown");
  }finally{p.stop();}
 }
}
