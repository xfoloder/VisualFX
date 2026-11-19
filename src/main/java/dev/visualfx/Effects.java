package dev.visualfx;

public final class Effects {
    public interface Effect { void draw(Geometry g,double h); }
    public static final String[] NAMES={"Orbital Rift","DNA Helix","Digital Glitch","Runic Orbit"};
    public static final Effect[] MODES={new OrbitalRift(),new DnaHelix(),new DigitalGlitch(),new RunicOrbit()};
    static final class OrbitalRift implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            g.ring(h*.5,r,.55*Math.sin(t),t,.018);g.ring(h*.5,r,-.65*Math.cos(t*.8),-t,.018);
            for(int i=0;i<g.c.shards;i++){double a=i*2.39996+t;g.shard(a,h*(.5+.4*Math.sin(a+t)),r*(1+.12*Math.sin(a*3)),.08);}
            g.ring(.05,r*(.8+.2*Math.sin(t*2)),0,t,.009);
        }
    }
    static final class DnaHelix implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed;
            for(int strand=0;strand<2;strand++) for(int i=0;i<100;i++) {
                double y=i*h/100,Y=(i+1)*h/100,a=i*.13+t+strand*Math.PI,b=(i+1)*.13+t+strand*Math.PI,r=g.c.radius*.8;
                g.line(Math.cos(a)*r,y,Math.sin(a)*r,Math.cos(b)*r,Y,Math.sin(b)*r,.016,i*.02+strand);
                if(strand==0&&i%10==0)g.line(Math.cos(a)*r,y,Math.sin(a)*r,-Math.cos(a)*r,y,-Math.sin(a)*r,.008,i*.02);
            }
        }
    }
    static final class DigitalGlitch implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed;
            for(int i=0;i<g.c.shards+8;i++){double seed=i*7.319,flash=Math.sin(Math.floor(t*12+i)*seed);
                if(flash<-.25)continue;
                double a=seed+t*.2,r=g.c.radius*(.8+.2*flash),y=h*(.5+.45*Math.sin(seed));
                double x=Math.cos(a)*r,z=Math.sin(a)*r,w=.12+.18*Math.abs(flash);
                g.line(x-w,y,z,x+w,y,z,.012,seed);g.line(x+w,y,z,x+w,y+.12,z,.01,seed);
                if(flash>.7)g.line(x-w,y+.08,z,x+w+.15,y+.08,z,.022,seed+1);
            }
        }
    }
    static final class RunicOrbit implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            g.ring(.12,r,0,t,.012);g.ring(h*.85,r,.15,-t,.012);
            for(int i=0;i<10;i++){double a=i*Math.PI/5+t*.4,x=Math.cos(a)*r,z=Math.sin(a)*r,y=h*.5+.12*Math.sin(t*2+i),s=.13;
                g.line(x,y-s,z,x,y+s,z,.012,i*.3);g.line(x,y+s,z,x+s,y,z,.012,i*.3);
                g.line(x+s,y,z,x-s,y,z,.012,i*.3);g.line(x-s,y,z,x,y-s,z,.012,i*.3);
                double b=a+Math.PI*4/5;g.line(Math.cos(a)*r,.12,Math.sin(a)*r,Math.cos(b)*r,.12,Math.sin(b)*r,.005,i*.3);
            }
        }
    }
    static void impact(Geometry g,double h,double life){double age=1-life;
        g.ring(.08,g.c.radius+age*2,0,0,.035*life);
        for(int i=0;i<24;i++)g.shard(i*2.39996,h*.5+Math.sin(i*4.1)*age*1.6,g.c.radius+age*2,.13*life);
    }
}
