package dev.visualfx;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import java.util.HashSet;
public final class GeometryTest {
 static final class Sink implements VertexConsumer {
  long count,hash=1;
  public VertexConsumer addVertex(Matrix4f m,float x,float y,float z){
   if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z)||Math.abs(x)>32||Math.abs(y)>32||Math.abs(z)>32)throw new AssertionError("Invalid vertex");
   count++;hash=hash*31+Float.floatToIntBits(x);hash=hash*31+Float.floatToIntBits(y);hash=hash*31+Float.floatToIntBits(z);return this;
  }
  public VertexConsumer setColor(int color){return this;}
 }
 static long run(Config c,int mode,boolean hit,double time,double height,double life){
  Sink sink=new Sink();Geometry g=new Geometry();g.begin(sink,new Matrix4f(),c,time,1,0);
  if(hit)HitEffects.draw(g,height,life,mode);else Effects.MODES[mode].draw(g,height);
  g.end();if(sink.count>30000)throw new AssertionError("Geometry budget exceeded");return sink.count;
 }
 public static void main(String[] args){
  if(Effects.MODES.length!=Effects.NAMES.length||Effects.MODES.length!=13||HitEffects.NAMES.length!=6)throw new AssertionError("Catalog mismatch");
  int checks=0;
  for(double radius:new double[]{.4,1.6})for(double speed:new double[]{.1,3})for(int shards:new int[]{0,48})for(double time:new double[]{0,.123,20})for(double height:new double[]{.6,1.8,3}){
   Config c=new Config();c.radius=radius;c.speed=speed;c.shards=shards;
   for(int mode=0;mode<Effects.MODES.length;mode++){
    c.optimization=false;long full=run(c,mode,false,time,height,1);
    for(int p=0;p<3;p++){c.optimization=true;c.optimizationPreset=p;if(run(c,mode,false,time,height,1)>full)throw new AssertionError("LOD increases vertex count");checks++;}
   }
   for(int mode=0;mode<6;mode++)for(double life:new double[]{0,.01,.5,1}){c.optimization=false;run(c,mode,true,time,height,life);checks++;}
  }
  Config c=new Config();HashSet<Long> shapes=new HashSet<>();
  for(var effect:Effects.MODES){Sink sink=new Sink();Geometry g=new Geometry();g.begin(sink,new Matrix4f(),c,.57,1,0);effect.draw(g,1.8);if(!shapes.add(sink.hash))throw new AssertionError("Duplicate ESP geometry");}
  shapes.clear();for(int i=0;i<6;i++){Sink sink=new Sink();Geometry g=new Geometry();g.begin(sink,new Matrix4f(),c,.57,1,0);HitEffects.draw(g,1.8,.57,i);if(!shapes.add(sink.hash))throw new AssertionError("Duplicate hit geometry");}
  System.out.println("PASS: "+checks+" geometry cases; finite/bounded vertices, budget <= 30000 vertices per effect, ESP LOD budgets, 13 distinct ESP and 6 distinct hit shapes");
 }
}
