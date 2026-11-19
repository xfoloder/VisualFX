package dev.visualfx;

/** Bounded movement recording; data snapshots are immutable across extraction/render phases. */
final class EchoHistory {
    static final int CAPACITY=32;
    record Point(double x,double y,double z,double height,double yaw,double time){}
    record Snapshot(Point[] points,double time,double lifetime){int size(){return points.length;}}
    private final Point[] ring=new Point[CAPACITY];
    private int head,count,target=-1;
    void clear(){java.util.Arrays.fill(ring,null);head=count=0;target=-1;}
    int target(){return target;}
    int size(){return count;}
    void push(int id,double x,double y,double z,double height,double yaw,double time){
        if(!Double.isFinite(x+y+z+height+yaw+time))return;
        if(target!=id){clear();target=id;}
        Point last=count==0?null:ring[(head-1+CAPACITY)%CAPACITY];
        if(last!=null){
            double dx=x-last.x,dy=y-last.y,dz=z-last.z,distance=dx*dx+dy*dy+dz*dz;
            if(distance>36){clear();target=id;last=null;}
            else if(distance<.0004&&Math.abs(Math.IEEEremainder(yaw-last.yaw,Math.PI*2))<.07)return;
        }
        ring[head]=new Point(x,y,z,height,yaw,time);head=(head+1)%CAPACITY;count=Math.min(CAPACITY,count+1);
    }
    Snapshot snapshot(double time,double lifetime){
        int valid=0;
        for(int i=0;i<count;i++){Point p=ring[(head-1-i+CAPACITY)%CAPACITY];if(time-p.time<=lifetime)valid++;else break;}
        Point[] points=new Point[valid];
        for(int i=0;i<valid;i++)points[i]=ring[(head-1-i+CAPACITY)%CAPACITY];
        return new Snapshot(points,time,lifetime);
    }
    static Snapshot demo(double time,double lifetime){
        Point[] p=new Point[24];
        for(int i=0;i<p.length;i++){double age=i*lifetime/p.length;
            p[i]=new Point(-age*1.4,0,Math.sin(time*1.4-age*2)*.5,1.8,Math.sin(time-age)*.4,time-age);
        }
        return new Snapshot(p,time,lifetime);
    }
}

