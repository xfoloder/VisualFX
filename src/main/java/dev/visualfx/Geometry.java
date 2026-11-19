package dev.visualfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** Emissive colored tube geometry; outer shells provide a cheap bounded glow. */
public class Geometry {
    VertexConsumer v; Matrix4f matrix; Config c;
    double t,alpha,hit;
    int forcedColor=-1;
    private static final ThreadLocal<RustGeometry.Batch> RUST_BATCH=ThreadLocal.withInitial(RustGeometry.Batch::new);
    private RustGeometry.Batch batch;
    void begin(VertexConsumer v,Matrix4f matrix,Config c,double t,double alpha,double hit){
        this.v=v;this.matrix=matrix;this.c=c;this.t=t;this.alpha=alpha;this.hit=hit*c.hitStrength;
        if(c.rustGeometry&&RustGeometry.available())batch=RUST_BATCH.get();
    }
    void end(){
        try { if(batch!=null)batch.flush(this); }
        finally { batch=null;v=null;matrix=null;c=null;forcedColor=-1; }
    }
    void vertex(double x,double y,double z,int color,int a){v.addVertex(matrix,(float)x,(float)y,(float)z).setColor((a<<24)|color);}
    void line(double x,double y,double z,double X,double Y,double Z,double width,double phase) {
        int color=forcedColor>=0?forcedColor:c.color(t*.45+phase);
        int passes=c.glowPasses();
        if(batch!=null){
            batch.add(x,y,z,X,Y,Z,width,alpha,c.glow,hit,color,passes,this);
            return;
        }
        for(int pass=0;pass<passes;pass++) {
            double w=width*(pass==0?1:pass==1?2.2:4.8)*(1+hit*.5);
            int a=(int)(Math.clamp(alpha*(pass==0?.92:pass==1?.2*c.glow:.055*c.glow)*(1+hit),0,1)*255);
            // Two perpendicular ribbons form a small volumetric cross section.
            double dx=X-x,dy=Y-y,dz=Z-z, len=Math.sqrt(dx*dx+dz*dz);
            double nx=len>1e-7?-dz/len*w:w,nz=len>1e-7?dx/len*w:0;
            vertex(x+nx,y,z+nz,color,a);vertex(X+nx,Y,Z+nz,color,a);vertex(X-nx,Y,Z-nz,color,a);vertex(x-nx,y,z-nz,color,a);
            vertex(x,y+w,z,color,a);vertex(X,Y+w,Z,color,a);vertex(X,Y-w,Z,color,a);vertex(x,y-w,z,color,a);
        }
    }
    void ring(double y,double radius,double tilt,double rotation,double width) {
        int segments=c.detailSegments(96);
        for(int i=0;i<segments;i++) {
            double a=i*Math.PI*2/segments+rotation,b=(i+1)*Math.PI*2/segments+rotation;
            double pulse=.65+.35*Math.sin(a*3-t*3);
            line(Math.cos(a)*radius,y+Math.sin(a)*radius*Math.sin(tilt),Math.sin(a)*radius*Math.cos(tilt),Math.cos(b)*radius,y+Math.sin(b)*radius*Math.sin(tilt),Math.sin(b)*radius*Math.cos(tilt),width*pulse,i/(double)segments);
        }
    }
    void shard(double a,double y,double r,double size) {
        double x=Math.cos(a)*r,z=Math.sin(a)*r;
        line(x,y-size,z,x+Math.cos(a+.8)*size,y+size,z+Math.sin(a+.8)*size,.014,a);
        line(x,y-size,z,x-Math.cos(a+.8)*size,y,z-Math.sin(a+.8)*size,.01,a+.5);
    }
}








