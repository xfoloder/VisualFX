package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

final class Watermark {
    static final int WIDTH=172, HEIGHT=36;
    private final HudPlacement placement=new HudPlacement();
    void render(GuiGraphics g,DeltaTracker tracker){
        Minecraft c=Minecraft.getInstance();Config cfg=VisualFX.config;
        if(!cfg.watermark||c.level==null||c.screen!=null)return;
        placement.update(g.guiWidth(),g.guiHeight(),true);
        var connection=c.getConnection();
        var playerInfo=c.hasSingleplayerServer()||connection==null||c.player==null
            ? null : connection.getPlayerInfo(c.player.getUUID());
        int latency=playerInfo==null?-1:playerInfo.getLatency();
        draw(g,c.font,placement.x,placement.y,placement.scale,cfg.watermarkPalette,System.nanoTime()/1e9,c.getFps(),latency);
    }
    static void preview(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time){
        draw(g,font,x,y,scale,palette,time,144,38);
    }
    private static void draw(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time,int fps,int latency){
        int accent=0xFF000000|VisualFX.config.watermarkStyle.sample(palette,time*.45);
        g.pose().pushMatrix();g.pose().translate((float)x,(float)y).scale((float)scale,(float)scale);
        g.fill(0,0,WIDTH,HEIGHT,0xEE080F19);g.renderOutline(0,0,WIDTH,HEIGHT,accent);
        g.fill(0,0,3,HEIGHT,accent);
        g.drawString(font,"VisualFX",10,7,accent,false);
        g.drawString(font,"| Minecraft 1.21.11",65,7,0xFFD0DCE3,false);
        g.fill(10,18,WIDTH-10,19,0xFF233746);
        g.drawString(font,"FPS " + Math.max(0,fps),10,24,0xFFD0DCE3,false);
        g.drawString(font,"Пинг " + (latency<0?"—":latency + " мс"),88,24,0xFFD0DCE3,false);
        g.pose().popMatrix();
    }
}


