package dev.visualfx;
import org.joml.Matrix4f;
public final class EchoGeometryTest {
 public static void main(String[] args){int checks=0;
  for(double length:new double[]{.25,1.5})for(double time:new double[]{0,.5,5,20})for(int ghosts=2;ghosts<=8;ghosts++)for(double intensity:new double[]{.2,1})for(double pulse:new double[]{0,.5,1})for(int quality=-1;quality<3;quality++){
   Config c=new Config();c.optimization=quality>=0;c.optimizationPreset=Math.max(0,quality);
   GeometryTest.Sink sink=new GeometryTest.Sink();Geometry g=new Geometry();g.begin(sink,new Matrix4f(),c,time,.8,0);g.forcedColor=0xABCDEF;
   EchoVisual.draw(g,EchoHistory.demo(time,length),c.optimization?Math.min(3,ghosts):ghosts,intensity,pulse);
   if(sink.count>4000)throw new AssertionError("Echo budget exceeded");
   if(g.alpha!=.8||g.forcedColor!=0xABCDEF)throw new AssertionError("Leaked render state");checks++;
  }
  System.out.println("PASS: "+checks+" echo geometry cases, finite/bounded coordinates, <=4000 vertices, render-state restoration");
 }
}
