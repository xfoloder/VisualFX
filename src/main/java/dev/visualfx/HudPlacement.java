package dev.visualfx;

/** Shared screen bounds for rendering and editor hit testing, in GUI pixels. */
final class HudPlacement {
    double x,y,scale,w,h,travelX,travelY;
    void update(int width,int height,boolean watermark){
        Config c=VisualFX.config;
        double baseW=watermark?172:180,baseH=watermark?24:56;
        scale=Math.min(watermark?c.watermarkScale:c.hudScale,Math.min((width-8)/baseW,(height-8)/baseH));
        scale=Math.max(.1,scale);w=baseW*scale;h=baseH*scale;
        travelX=Math.max(0,width-8-w);travelY=Math.max(0,height-8-h);
        x=4+(watermark?c.watermarkX:c.hudX)*travelX;
        y=4+(watermark?c.watermarkY:c.hudY)*travelY;
        if(!watermark)follow(width*.5,height*.55,width,height);
    }
    void follow(double anchorX,double anchorY,int width,int height){
        Config c=VisualFX.config;
        x=Math.clamp(anchorX-w*.5+c.hudOffsetX,4,Math.max(4,width-w-4));
        y=Math.clamp(anchorY-h+c.hudOffsetY,4,Math.max(4,height-h-4));
    }
    boolean contains(double px,double py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}
    void move(double px,double py,boolean watermark){
        double nx=travelX>0?Math.clamp((px-4)/travelX,0,1):0,ny=travelY>0?Math.clamp((py-4)/travelY,0,1):0;
        if(watermark){VisualFX.config.watermarkX=nx;VisualFX.config.watermarkY=ny;}
        else {
            double width=travelX+w+8,height=travelY+h+8;
            VisualFX.config.hudOffsetX=4+nx*travelX-width*.5+w*.5;
            VisualFX.config.hudOffsetY=4+ny*travelY-height*.55+h;
        }
    }
}


