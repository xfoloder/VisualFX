package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;

final class Watermark {
    static final int WIDTH=172, HEIGHT=49;
    private static final BlockPos PREVIEW_POSITION = new BlockPos(128, 64, -256);
    private final HudPlacement placement=new HudPlacement();
    void render(GuiGraphics g,DeltaTracker tracker){
        Minecraft c=Minecraft.getInstance();Config cfg=VisualFX.config;
        if(!cfg.watermark||c.level==null||c.screen!=null)return;
        placement.update(g.guiWidth(),g.guiHeight(),true);
        drawLive(g,c,placement,1);
    }
    static void drawLive(GuiGraphics g, Minecraft c, HudPlacement position, double opacity) {
        if (opacity < .04) return;
        var connection=c.getConnection();
        var playerInfo=c.hasSingleplayerServer()||connection==null||c.player==null
            ? null : connection.getPlayerInfo(c.player.getUUID());
        int latency=playerInfo==null?-1:playerInfo.getLatency();
        draw(g,c.font,position.x,position.y,position.scale,VisualFX.config.watermarkPalette,System.nanoTime()/1e9,c.getFps(),latency,c.player==null?null:c.player.blockPosition(),opacity);
    }
    static void preview(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time){
        draw(g,font,x,y,scale,palette,time,144,38,PREVIEW_POSITION,1);
    }
    private static void draw(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time,int fps,int latency,BlockPos position,double opacity){
        int accent=fade(0xFF000000|VisualFX.config.watermarkStyle.sample(palette,time*.45),opacity);
        int text=fade(0xFFD0DCE3,opacity);
        g.pose().pushMatrix();g.pose().translate((float)x,(float)y).scale((float)scale,(float)scale);
        g.fill(0,0,WIDTH,HEIGHT,fade(0xEE080F19,opacity));g.renderOutline(0,0,WIDTH,HEIGHT,accent);
        g.fill(0,0,3,HEIGHT,accent);
        g.drawString(font,"VisualFX",10,7,accent,false);
        g.drawString(font,"| Minecraft 1.21.11",65,7,text,false);
        g.fill(10,18,WIDTH-10,19,fade(0xFF233746,opacity));
        g.drawString(font,"FPS " + Math.max(0,fps),10,24,text,false);
        g.drawString(font,"Пинг " + (latency<0?"—":latency + " мс"),88,24,text,false);
        String coordinates = coordinates(position);
        int available = WIDTH - 20;
        float fit = Math.min(1, available / (float) font.width(coordinates));
        g.pose().translate(10, 37).scale(fit, 1);
        g.drawString(font,coordinates,0,0,text,false);
        g.pose().popMatrix();
    }
    static String coordinates(BlockPos position) {
        if (position == null) return "XYZ —";
        return "XYZ " + position.getX() + "  " + position.getY() + "  " + position.getZ();
    }
    private static int fade(int color,double opacity) {
        return ((int) ((color >>> 24) * opacity) << 24) | (color & 0xFFFFFF);
    }
}


