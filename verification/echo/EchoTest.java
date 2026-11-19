package dev.visualfx;
public final class EchoTest {
 public static void main(String[] args){
  EchoHistory history=new EchoHistory();
  for(int i=0;i<100;i++)history.push(1,i*.1,0,0,1.8,0,i*.05);
  assert history.size()==32;assert history.target()==1;
  var snapshot=history.snapshot(5,1.5);assert snapshot.size()<=30&&snapshot.size()>1;
  double newest=snapshot.points()[0].x();history.push(1,10,0,0,1.8,0,5);
  assert snapshot.points()[0].x()==newest : "Published snapshot mutated";
  assert history.snapshot(20,1.5).size()==0;
  history.push(2,0,0,0,1.8,0,21);assert history.size()==1&&history.target()==2;
  history.push(2,100,0,0,1.8,0,21.05);assert history.size()==1 : "Teleport connected across map";
  history.push(2,100,0,0,1.8,0,21.1);assert history.size()==1 : "Stationary duplicate";
  history.push(2,100,0,0,1.8,.2,21.15);assert history.size()==2 : "Rotation sample missing";
  history.push(2,Double.NaN,0,0,1.8,0,22);assert history.size()==2;
  history.clear();assert history.size()==0&&history.target()==-1;
  assert EchoHistory.demo(2,1).size()==24;
  System.out.println("PASS: 32-sample cap, expiration, snapshot independence, target switch, teleport reset, stationary suppression, yaw capture, finite input and clear");
 }
}
