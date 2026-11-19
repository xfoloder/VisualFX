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
    }
    boolean contains(double px,double py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}
    void move(double px,double py,boolean watermark){
        double nx=travelX>0?Math.clamp((px-4)/travelX,0,1):0,ny=travelY>0?Math.clamp((py-4)/travelY,0,1):0;
        if(watermark){VisualFX.config.watermarkX=nx;VisualFX.config.watermarkY=ny;}
        else {VisualFX.config.hudX=nx;VisualFX.config.hudY=ny;}
    }
}
