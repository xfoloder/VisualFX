package dev.visualfx;

/** Shared screen bounds for rendering and editor hit testing, in GUI pixels. */
final class HudPlacement {
    double x,y,scale,w,h,travelX,travelY;
    void update(int width,int height,boolean watermark){
        Config c=VisualFX.config;
        double baseW=watermark?Watermark.WIDTH:180,baseH=watermark?Watermark.HEIGHT:56;
        updateScreen(width,height,baseW,baseH,watermark?c.watermarkScale:c.hudScale,
            watermark?c.watermarkX:c.hudX,watermark?c.watermarkY:c.hudY);
        if(!watermark)follow(width*.5,height*.55,width,height);
    }
    void updateScreen(int width,int height,double baseW,double baseH,double requestedScale,double normalizedX,double normalizedY){
        scale=Math.min(requestedScale,Math.min((width-8)/baseW,(height-8)/baseH));
        scale=Math.max(.1,scale);w=baseW*scale;h=baseH*scale;
        travelX=Math.max(0,width-8-w);travelY=Math.max(0,height-8-h);
        x=4+normalizedX*travelX;
        y=4+normalizedY*travelY;
    }
    void follow(double anchorX,double anchorY,int width,int height){
        Config c=VisualFX.config;
        x=Math.clamp(anchorX-w*.5+c.hudOffsetX,4,Math.max(4,width-w-4));
        y=Math.clamp(anchorY-h+c.hudOffsetY,4,Math.max(4,height-h-4));
    }
    boolean contains(double px,double py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}
    void moveEffectStatus(double px,double py){
        var c=VisualFX.config;
        if(travelX>0)c.effectStatusX=Math.clamp((px-4)/travelX,0,1);
        if(travelY>0)c.effectStatusY=Math.clamp((py-4)/travelY,0,1);
    }
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

