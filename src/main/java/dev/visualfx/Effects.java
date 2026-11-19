package dev.visualfx;

public final class Effects {
    public interface Effect { void draw(Geometry g,double h); }
    public static final String[] NAMES={"Орбитальный разлом","Двойная спираль","Цифровой сбой","Руническая орбита",
        "Энергоклетка","Неоновая корона","Вихрь осколков","Звёздная клетка","Радар","Неоновый лотос",
        "Орбитальные кометы","Квантовые кубы","Грозовой контур"};
    public static final Effect[] MODES={new OrbitalRift(),new DnaHelix(),new DigitalGlitch(),new RunicOrbit(),
        new EnergyCage(),new NeonCrown(),new ShardVortex(),new ExtraEffects.StarCage(),new ExtraEffects.Radar(),
        new ExtraEffects.Lotus(),new ExtraEffects.Comets(),new ExtraEffects.QuantumCubes(),new ExtraEffects.Storm()};
    static final class OrbitalRift implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed,r=g.c.radius;
            g.ring(h*.5,r,.55*Math.sin(t),t,.018);g.ring(h*.5,r,-.65*Math.cos(t*.8),-t,.018);
            for(int i=0;i<g.c.effectShards();i++){double a=i*2.39996+t;g.shard(a,h*(.5+.4*Math.sin(a+t)),r*(1+.12*Math.sin(a*3)),.08);}
            g.ring(.05,r*(.8+.2*Math.sin(t*2)),0,t,.009);
        }
    }
    static final class DnaHelix implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed;
            int segments=g.c.detailSegments(100);
            for(int strand=0;strand<2;strand++) for(int i=0;i<segments;i++) {
                double y=i*h/segments,Y=(i+1)*h/segments,a=i*13.0/segments+t+strand*Math.PI,b=(i+1)*13.0/segments+t+strand*Math.PI,r=g.c.radius*.8;
                g.line(Math.cos(a)*r,y,Math.sin(a)*r,Math.cos(b)*r,Y,Math.sin(b)*r,.016,i*2.0/segments+strand);
                if(strand==0&&i%Math.max(1,segments/10)==0)g.line(Math.cos(a)*r,y,Math.sin(a)*r,-Math.cos(a)*r,y,-Math.sin(a)*r,.008,i*2.0/segments);
            }
        }
    }
    static final class DigitalGlitch implements Effect {
        public void draw(Geometry g,double h){double t=g.t*g.c.speed;
            for(int i=0;i<g.c.effectShards()+8;i++){double seed=i*7.319,flash=Math.sin(Math.floor(t*12+i)*seed);
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
    /** Rotating ellipsoid meridians with a scanning latitude. */
    static final class EnergyCage implements Effect {
        public void draw(Geometry g,double h){
            double t=g.t*g.c.speed,r=g.c.radius,center=h*.5,vertical=h*.63;
            int segments=g.c.detailSegments(48);
            for(int band=0;band<6;band++){
                double azimuth=band*Math.PI/6+t*.35;
                double ca=Math.cos(azimuth),sa=Math.sin(azimuth);
                for(int i=0;i<segments;i++){
                    double a=i*Math.PI*2/segments,b=(i+1)*Math.PI*2/segments;
                    g.line(ca*Math.cos(a)*r,center+Math.sin(a)*vertical,sa*Math.cos(a)*r,
                        ca*Math.cos(b)*r,center+Math.sin(b)*vertical,sa*Math.cos(b)*r,.009,band*.3+i/(double)segments);
                }
            }
            double scan=Math.sin(t*1.4)*.94;
            g.ring(center+scan*vertical,r*Math.sqrt(1-scan*scan),0,t,.025);
            g.ring(center,r,0,-t,.009);
        }
    }
    /** A floating crown above the head with alternating tall and short teeth. */
    static final class NeonCrown implements Effect {
        public void draw(Geometry g,double h){
            double t=g.t*g.c.speed,r=g.c.radius*.72,y=h+.18+.07*Math.sin(t*2);
            g.ring(y,r,0,t,.018);
            g.ring(y-.09,r*.93,0,-t,.009);
            for(int i=0;i<12;i++){
                double a=i*Math.PI/6+t*.5,b=a+Math.PI/12,end=a+Math.PI/6;
                double peak=y+(i%2==0?.32:.2),outer=r*1.08;
                double x=Math.cos(b)*outer,z=Math.sin(b)*outer;
                g.line(Math.cos(a)*r,y,Math.sin(a)*r,x,peak,z,.016,i*.3);
                g.line(x,peak,z,Math.cos(end)*r,y,Math.sin(end)*r,.016,i*.3+.15);
                g.line(x,peak,z,x,peak+.05,z,.025,i*.3);
            }
            for(int i=0;i<g.c.effectShards();i++){
                double a=i*2.39996-t*.7;
                g.shard(a,y-.15+.1*Math.sin(a+t),r*1.35,.045);
            }
        }
    }
    /** Rising shards with curved comet trails; at most 48 shards and six trail segments each. */
    static final class ShardVortex implements Effect {
        public void draw(Geometry g,double h){
            double t=g.t*g.c.speed,r=g.c.radius;
            for(int i=0;i<g.c.effectShards();i++){
                double phase=i*.61803398875+t*.23;
                double rise=phase-Math.floor(phase),a=i*2.39996+t*2.1;
                double radius=r*(.55+.45*rise),y=rise*h;
                g.shard(a,y,radius,.055+.045*rise);
                for(int j=0;j<6;j++){
                    double f=j/6.0,F=(j+1)/6.0;
                    double b=a-f*.6,B=a-F*.6;
                    g.line(Math.cos(b)*radius,y-f*.18,Math.sin(b)*radius,
                        Math.cos(B)*radius,y-F*.18,Math.sin(B)*radius,.012*(1-f*.85),i*.2-f*.4);
                }
            }
            g.ring(.03,r*.7,0,-t,.012);
        }
    }
}








