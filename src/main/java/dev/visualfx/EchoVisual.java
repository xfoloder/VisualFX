package dev.visualfx;

final class EchoVisual {
    static void draw(Geometry g,EchoHistory.Snapshot snapshot,int ghosts,double intensity,double pulse){
        if(snapshot.size()<2)return;
        var points=snapshot.points();var origin=points[0];double oldAlpha=g.alpha;
        double compression=1-.25*Math.sin(Math.PI*pulse);
        int oldColor=g.forcedColor;
        for(int i=1;i<points.length;i++){
            var a=points[i-1];var b=points[i];
            double fade=Math.max(0,1-(snapshot.time()-b.time())/snapshot.lifetime());
            g.alpha=oldAlpha*intensity*fade*.65;
            double flare=pulse>0&&Math.abs(i/(double)points.length-(1-pulse))<.18?pulse:0;
            g.forcedColor=g.c.echoStyle.sample(g.c.echoPalette,g.t*.45+i*.04);
            for(int side=-1;side<=1;side+=2){
                double ox=Math.cos(a.yaw())*.22*side,oz=Math.sin(a.yaw())*.22*side;
                double OX=Math.cos(b.yaw())*.22*side,OZ=Math.sin(b.yaw())*.22*side;
                g.line((a.x()-origin.x())*compression+ox,(a.y()-origin.y())*compression+.06,(a.z()-origin.z())*compression+oz,
                    (b.x()-origin.x())*compression+OX,(b.y()-origin.y())*compression+.06,(b.z()-origin.z())*compression+OZ,.012+flare*.025,i*.1);
            }
        }
        int count=Math.min(ghosts,points.length);
        for(int k=0;k<count;k++){
            int index=k*(points.length-1)/Math.max(1,count-1);var p=points[index];
            double fade=Math.max(0,1-(snapshot.time()-p.time())/snapshot.lifetime());
            g.alpha=oldAlpha*intensity*fade*.6;
            g.forcedColor=g.c.echoStyle.sample(g.c.echoPalette,g.t*.45+k*.12);
            double x=(p.x()-origin.x())*compression,y=(p.y()-origin.y())*compression,z=(p.z()-origin.z())*compression,h=p.height();
            double dx=Math.cos(p.yaw()),dz=Math.sin(p.yaw()),w=.23;
            g.line(x-dx*w,y+h*.7,z-dz*w,x+dx*w,y+h*.7,z+dz*w,.012,k);
            g.line(x-dx*w,y+h*.7,z-dz*w,x-dx*w,y+h*.3,z-dz*w,.01,k);
            g.line(x+dx*w,y+h*.7,z+dz*w,x+dx*w,y+h*.3,z+dz*w,.01,k);
            g.line(x,y+h*.7,z,x,y+h*.32,z,.01,k);
            g.line(x,y+h*.32,z,x-dx*.16,y,z-dz*.16,.01,k);
            g.line(x,y+h*.32,z,x+dx*.16,y,z+dz*.16,.01,k);
            for(int edge=0;edge<4;edge++){
                double A=edge*Math.PI/2+Math.PI/4,B=A+Math.PI/2;
                g.line(x+dx*Math.cos(A)*.23,y+h*.88+Math.sin(A)*.23,z+dz*Math.cos(A)*.23,
                    x+dx*Math.cos(B)*.23,y+h*.88+Math.sin(B)*.23,z+dz*Math.cos(B)*.23,.011,k);
            }
        }
        g.alpha=oldAlpha;g.forcedColor=oldColor;
    }
}

