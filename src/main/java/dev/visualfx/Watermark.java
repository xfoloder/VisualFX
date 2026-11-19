package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

final class Watermark {
    private final HudPlacement placement=new HudPlacement();
    void render(GuiGraphicsExtractor g,DeltaTracker tracker){
        Minecraft c=Minecraft.getInstance();Config cfg=VisualFX.config;
        if(!cfg.watermark||c.level==null||c.gui.screen()!=null)return;
        placement.update(g.guiWidth(),g.guiHeight(),true);
        draw(g,c.font,placement.x,placement.y,placement.scale,cfg.watermarkPalette,System.nanoTime()/1e9);
    }
    static void draw(GuiGraphicsExtractor g,Font font,double x,double y,double scale,int palette,double time){
        int accent=0xFF000000|VisualFX.config.watermarkStyle.sample(palette,time*.45);
        g.pose().pushMatrix();g.pose().translate((float)x,(float)y).scale((float)scale,(float)scale);
        g.fill(0,0,172,24,0xEE080F19);g.outline(0,0,172,24,accent);
        g.fill(0,0,3,24,accent);
        g.text(font,"VisualFX",10,8,accent,false);
        g.text(font,"| Minecraft 26.3",65,8,0xFFD0DCE3,false);
        g.pose().popMatrix();
    }
}

