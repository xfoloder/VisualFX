package dev.visualfx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

/** Projects the actual effect geometry into the menu, without a world or target. */
final class EffectPreview extends Geometry {
    private GuiGraphicsExtractor gui;
    private double cx,cy,scale,fade;

    void draw(GuiGraphicsExtractor gui,Font font,int x,int y,int width,int height,Config config,double seconds,double fade,boolean deathPreview,boolean hitPreview){
        if(height<48)return;
        this.gui=gui;this.c=config;this.t=seconds;this.alpha=1;this.hit=0;this.fade=fade;
        gui.fill(x,y,x+width,y+height,argb(0x09131D,.85));
        gui.outline(x,y,width,height,argb(config.color(seconds*.45),.3));
        gui.text(font,deathPreview?"Смерть: "+DeathEffectSystem.NAMES[config.deathMode]:hitPreview?"Эффект удара":"Предпросмотр",x+8,y+7,argb(0xC5D8E3,1),false);
        gui.enableScissor(x+1,y+21,x+width-1,y+height-1);
        cx=x+width*.5;scale=Math.min((width-18)/4.2,(height-32)/3.3);cy=y+22+(height-32)*.5+scale*.6;
        if(deathPreview){scale=Math.min((width-18)/4.2,(height-32)/4.1);cy=y+height-12;}
        if(hitPreview){scale=Math.min((width-18)/7.2,(height-32)/4.2);cy=y+22+(height-32)*.5+scale*.7;}
        // A calm wireframe stage keeps the animated neon shape easy to read.
        for(int i=-2;i<=2;i++){
            plainLine(cx-scale*1.6,cy+scale*.17+i*3,cx+scale*1.6,cy+scale*.17+i*3,1,0x213341,.5);
        }
        int body=argb(0x263C4B,.95),edge=argb(0x728D9A,.8);
        int head=(int)(scale*.28),bx=(int)cx,feet=(int)cy;
        gui.fill(bx-head,feet-(int)(scale*1.8),bx+head,feet-(int)(scale*1.25),body);
        gui.outline(bx-head,feet-(int)(scale*1.8),head*2,(int)(scale*.55),edge);
        gui.fill(bx-head,feet-(int)(scale*1.22),bx+head,feet-(int)(scale*.48),body);
        gui.fill(bx-head-(int)(scale*.22),feet-(int)(scale*1.2),bx-head-1,feet-(int)(scale*.45),body);
        gui.fill(bx+head+1,feet-(int)(scale*1.2),bx+head+(int)(scale*.22),feet-(int)(scale*.45),body);
        gui.fill(bx-head,feet-(int)(scale*.45),bx-1,feet,body);
        gui.fill(bx+1,feet-(int)(scale*.45),bx+head,feet,body);
        if(deathPreview){
            double age=(seconds%3.2)/2.6;
            if(age<1){this.fade=fade*DeathEffectSystem.opacity(age);DeathEffectSystem.draw(this,1.8,age,config.deathMode);}
        }else if(hitPreview){
            double age=seconds%(config.hitDuration+.65);
            if(age<config.hitDuration)HitEffects.draw(this,1.8,1-age/config.hitDuration,config.hitMode);
        }else Effects.MODES[config.mode].draw(this,1.8);
        gui.disableScissor();
        this.gui=null;this.c=null;
    }
    private int argb(int color,double opacity){return ((int)(Math.clamp(opacity*fade,0,1)*255)<<24)|color;}
    private void plainLine(double x,double y,double X,double Y,double thickness,int color,double opacity){
        double dx=X-x,dy=Y-y;float length=(float)Math.sqrt(dx*dx+dy*dy);
        if(length<.01)return;
        gui.pose().pushMatrix();
        gui.pose().translate((float)x,(float)y).rotate((float)Math.atan2(dy,dx));
        int half=Math.max(1,(int)Math.ceil(thickness));
        gui.fill(0,-half,(int)Math.ceil(length),half,argb(color,opacity));
        gui.pose().popMatrix();
    }
    @Override void line(double x,double y,double z,double X,double Y,double Z,double width,double phase){
        double px=cx+(x+z*.35)*scale,py=cy-(y-z*.2)*scale;
        double qx=cx+(X+Z*.35)*scale,qy=cy-(Y-Z*.2)*scale;
        int color=forcedColor>=0?forcedColor:c.color(t*.45+phase);
        if(c.glow>.01&&c.glowPasses()>1)plainLine(px,py,qx,qy,2+width*scale*2,color,.1*c.glow*alpha);
        plainLine(px,py,qx,qy,Math.max(.5,width*scale),color,.8*alpha);
    }
}

