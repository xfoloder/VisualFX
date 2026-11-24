package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;

final class Watermark {
    static final int WIDTH=172;
    private static final int TITLE_HEIGHT = 24;
    private static final int ROW_HEIGHT = 13;
    private static final BlockPos PREVIEW_POSITION = new BlockPos(128, 64, -256);
    private final HudPlacement placement=new HudPlacement();
    static int height(Config config) {
        int rows = config.watermarkShowFps || config.watermarkShowPing ? 1 : 0;
        if (config.watermarkShowCoordinates) rows++;
        return rows == 0 ? TITLE_HEIGHT : TITLE_HEIGHT - 1 + rows * ROW_HEIGHT;
    }
    void render(GuiGraphics g,DeltaTracker tracker){
        Minecraft c=Minecraft.getInstance();Config cfg=VisualFX.config;
        if(!cfg.watermark||c.level==null||c.screen!=null)return;
        placement.update(g.guiWidth(),g.guiHeight(),true);
        drawLive(g,c,placement,1);
    }
    static void drawLive(GuiGraphics g, Minecraft c, HudPlacement position, double opacity) {
        if (opacity < .04) return;
        Config config = VisualFX.config;
        int latency = -1;
        if (config.watermarkShowPing && !c.hasSingleplayerServer() && c.player != null) {
            var connection = c.getConnection();
            var playerInfo = connection == null ? null : connection.getPlayerInfo(c.player.getUUID());
            if (playerInfo != null) latency = playerInfo.getLatency();
        }
        int fps = config.watermarkShowFps ? c.getFps() : 0;
        BlockPos coordinates = config.watermarkShowCoordinates && c.player != null ? c.player.blockPosition() : null;
        draw(g,c.font,position.x,position.y,position.scale,config.watermarkPalette,System.nanoTime()/1e9,fps,latency,coordinates,opacity);
    }
    static void preview(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time){
        draw(g,font,x,y,scale,palette,time,144,38,PREVIEW_POSITION,1);
    }
    private static void draw(GuiGraphics g,Font font,double x,double y,double scale,int palette,double time,int fps,int latency,BlockPos position,double opacity){
        Config config = VisualFX.config;
        int height = height(config);
        int accent=fade(0xFF000000|config.watermarkStyle.sample(palette,time*.45),opacity);
        int text=fade(0xFFD0DCE3,opacity);
        g.pose().pushMatrix();g.pose().translate((float)x,(float)y).scale((float)scale,(float)scale);
        g.fill(0,0,WIDTH,height,fade(0xEE080F19,opacity));g.renderOutline(0,0,WIDTH,height,accent);
        g.fill(0,0,3,height,accent);
        g.drawString(font,"VisualFX",10,7,accent,false);
        g.drawString(font,"| Minecraft 1.21.11",65,7,text,false);
        boolean hasMetrics = config.watermarkShowFps || config.watermarkShowPing;
        if (hasMetrics || config.watermarkShowCoordinates) g.fill(10,18,WIDTH-10,19,fade(0xFF233746,opacity));
        if (config.watermarkShowFps) g.drawString(font,"FPS " + Math.max(0,fps),10,TITLE_HEIGHT,text,false);
        if (config.watermarkShowPing) {
            int xPing = config.watermarkShowFps ? 88 : 10;
            g.drawString(font,"Пинг " + (latency<0?"—":latency + " мс"),xPing,TITLE_HEIGHT,text,false);
        }
        if (config.watermarkShowCoordinates) {
            String coordinates = coordinates(position);
            int available = WIDTH - 20;
            float fit = Math.min(1, available / (float) font.width(coordinates));
            g.pose().translate(10, TITLE_HEIGHT + (hasMetrics ? ROW_HEIGHT : 0)).scale(fit, 1);
            g.drawString(font,coordinates,0,0,text,false);
        }
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


