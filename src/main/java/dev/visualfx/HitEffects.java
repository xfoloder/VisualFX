package dev.visualfx;

final class HitEffects {
    static final String[] NAMES={"Ударная волна","Крестовой разрез","Кристальный взрыв","Электроразряд","Плазменная сфера","Руническая печать"};
    static void draw(Geometry g,double h,double life,int mode){
        double age=1-life,r=g.c.radius,s=g.c.hitStrength,old=g.alpha;
        g.alpha*=Math.min(1,life*3);
        switch(mode){
            case 0->{
                g.ring(.08,r+age*2,0,0,.035*life*s);
                int count=g.c.optimization?Math.min(24,g.c.effectShards()):24;
                for(int i=0;i<count;i++)g.shard(i*2.39996,h*.5+Math.sin(i*4.1)*age*1.6,r+age*2,.13*life*s);
            }
            case 1->{
                double length=r*(.4+age*1.5),y=h*.5;
                for(int k=0;k<2;k++){
                    double z=(k-.5)*.25;
                    g.line(-length,y-length,z,length,y+length,z,.035*life*s,k);
                    g.line(-length,y+length,z,length,y-length,z,.035*life*s,k+.5);
                }
            }
            case 2->{
                int count=g.c.effectShards();
                for(int i=0;i<count;i++){
                    double a=i*2.39996,R=r*.25+age*(1.5+(i%3)*.3),y=h*.5+Math.sin(i*3.1)*age*1.7;
                    g.shard(a,y,R,.18*life*s);
                    g.line(Math.cos(a)*R,y,Math.sin(a)*R,Math.cos(a)*(R-.2*life),y-.15*life,Math.sin(a)*(R-.2*life),.009*life*s,i*.3);
                }
            }
            case 3->{
                int rays=g.c.optimization?6:12;
                for(int k=0;k<rays;k++)for(int j=0;j<6;j++){
                    double a=k*Math.PI*2/rays,f=j/6.0,F=(j+1)/6.0,R=(r+age*1.7)*f,S=(r+age*1.7)*F;
                    double y=h*.5+Math.sin(k*7+j*13+Math.floor(age*12))*Math.sin(f*Math.PI)*.3;
                    double Y=h*.5+Math.sin(k*7+(j+1)*13+Math.floor(age*12))*Math.sin(F*Math.PI)*.3;
                    g.line(Math.cos(a)*R,y,Math.sin(a)*R,Math.cos(a)*S,Y,Math.sin(a)*S,.018*life*s,k*.3);
                }
            }
            case 4->{
                double R=r*(.2+age*1.8),y=h*.5;
                g.ring(y,R,0,age*3,.022*life*s);g.ring(y,R,Math.PI/2,age*3,.022*life*s);
                g.ring(y,R,Math.PI/4,-age*3,.014*life*s);
            }
            case 5->{
                double R=r*(.5+age*.9),y=.04;
                g.ring(y,R,0,age,.018*life*s);
                for(int i=0;i<8;i++){
                    double a=i*Math.PI/4+age*.8,b=a+Math.PI*3/4;
                    g.line(Math.cos(a)*R,y,Math.sin(a)*R,Math.cos(b)*R,y,Math.sin(b)*R,.012*life*s,i*.3);
                    g.line(Math.cos(a)*R,y,Math.sin(a)*R,Math.cos(a)*R,y+.4*life,Math.sin(a)*R,.015*life*s,i*.3);
                }
            }
        }
        g.alpha=old;
    }
}

