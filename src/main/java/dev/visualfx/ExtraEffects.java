package dev.visualfx;

/** Additional bounded geometric styles, sharing the main palette and animation controls. */
final class ExtraEffects {
    static final class StarCage implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            for(int layer=0;layer<2;layer++)for(int i=0;i<4;i++){
                double a=i*Math.PI/2+t*(layer==0?.6:-.4)+layer*.5,b=a+Math.PI/2,R=r*(1-layer*.25),y=h*.5;
                double x=Math.cos(a)*R,z=Math.sin(a)*R;
                g.line(x,y,z,0,h*1.1,0,.015,i*.4+layer);g.line(x,y,z,0,-.1,0,.015,i*.4+layer);
                g.line(x,y,z,Math.cos(b)*R,y,Math.sin(b)*R,.009,i*.4);
            }
        }
    }
    static final class Radar implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            g.ring(.03,r,0,0,.012);g.ring(.04,r*.55,0,0,.007);
            for(int i=0;i<12;i++){double a=i*Math.PI/6;
                g.line(Math.cos(a)*r*.91,.04,Math.sin(a)*r*.91,Math.cos(a)*r*1.05,.04,Math.sin(a)*r*1.05,.01,i*.3);
            }
            for(int i=0;i<12;i++){double a=t*1.5-i*.035;
                g.line(0,.06,0,Math.cos(a)*r,.06,Math.sin(a)*r,.014*(1-i/14.0),i*.05);
            }
            g.ring(h*(.5+.45*Math.sin(t*1.5)),r*.65,0,t,.009);
        }
    }
    static final class Lotus implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;int n=g.c.detailSegments(160);
            for(int layer=0;layer<2;layer++)for(int i=0;i<n;i++){
                double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n,spin=t*.35*(layer==0?1:-1);
                double R=r*(.65+.35*Math.cos(a*5)),S=r*(.65+.35*Math.cos(b*5));
                double y=h*.45+Math.sin(a*5+t)*h*.32,Y=h*.45+Math.sin(b*5+t)*h*.32;
                g.line(Math.cos(a+spin)*R,y,Math.sin(a+spin)*R,Math.cos(b+spin)*S,Y,Math.sin(b+spin)*S,.014,layer+i/(double)n);
            }
        }
    }
    static final class Comets implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;int n=g.c.detailSegments(48);
            for(int k=0;k<3;k++)for(int i=0;i<n;i++){
                double a=t*(k==1?-1.3:1)+k*Math.PI*2/3-i*.9/n,b=a-.9/n;
                double y=h*.5+Math.sin(a+k)*h*.5,Y=h*.5+Math.sin(b+k)*h*.5;
                g.line(Math.cos(a)*r,y,Math.sin(a)*r,Math.cos(b)*r,Y,Math.sin(b)*r,.032*(1-i/(double)n)+.003,k+i/(double)n);
                if(i==0)g.shard(a,y,r,.11);
            }
        }
    }
    static final class QuantumCubes implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            for(int k=0;k<3;k++){
                double a=t*.7+k*Math.PI*2/3,cx=Math.cos(a)*r,cz=Math.sin(a)*r,cy=h*(.5+.3*Math.sin(a*1.3));
                double s=r*.22,ca=Math.cos(t+k),sa=Math.sin(t+k);
                for(int v=0;v<8;v++)for(int bit=0;bit<3;bit++)if((v&(1<<bit))==0){
                    int w=v|(1<<bit);double x=((v&1)==0?-s:s),z=((v&4)==0?-s:s),X=((w&1)==0?-s:s),Z=((w&4)==0?-s:s);
                    g.line(cx+x*ca-z*sa,cy+((v&2)==0?-s:s),cz+x*sa+z*ca,
                        cx+X*ca-Z*sa,cy+((w&2)==0?-s:s),cz+X*sa+Z*ca,.012,k*.7);
                }
                double b=a+Math.PI*2/3;
                g.line(cx,cy,cz,Math.cos(b)*r,h*(.5+.3*Math.sin(b*1.3)),Math.sin(b)*r,.004,k);
            }
        }
    }
    static final class Storm implements Effects.Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius,frame=Math.floor(t*14);int n=g.c.detailSegments(48);
            for(int k=0;k<3;k++)for(int i=0;i<n;i++){
                double f=i/(double)n,F=(i+1)/(double)n,a=f*5+t+k*Math.PI*2/3,b=F*5+t+k*Math.PI*2/3;
                double R=r*(.8+.16*Math.sin(i*12.31+k+frame*3.17)),S=r*(.8+.16*Math.sin((i+1)*12.31+k+frame*3.17));
                g.line(Math.cos(a)*R,f*h,Math.sin(a)*R,Math.cos(b)*S,F*h,Math.sin(b)*S,.013,k+f);
            }
        }
    }
}
