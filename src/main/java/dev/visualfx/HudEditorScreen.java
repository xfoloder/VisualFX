package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** A separate uncluttered screen edits the same scaled bounds used by the game HUD. */
final class HudEditorScreen extends Screen {
    private final Screen parent;
    enum Panel { TARGET_HUD, WATERMARK, EFFECT_STATUS }
    private final Panel panel;
    private final HudPlacement placement=new HudPlacement();
    private boolean dragging;
    private double grabX,grabY;
    HudEditorScreen(Screen parent,Panel panel){super(Component.literal("Расположение HUD"));this.parent=parent;this.panel=panel;}
    private void updatePlacement(){
        if(panel==Panel.EFFECT_STATUS)EffectStatus.placeDemo(placement,width,height);
        else placement.update(width,height,panel==Panel.WATERMARK);
    }
    private int toolbarY(){return placement.y+placement.h>height-42?4:height-37;}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
    @Override public void onClose(){dragging=false;VisualFX.config.save();minecraft.gui.setScreen(parent);}
    @Override public void removed(){VisualFX.config.save();}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        int theme = MenuTheme.current();
        int accent = 0xFF000000 | MenuTheme.accent(theme);
        g.fill(0,0,width,height,0x77000000 | MenuTheme.surface(0x020910,theme,.06));
        g.verticalLine(width/2,0,height,0x30486A7C);g.horizontalLine(0,width,height/2,0x30486A7C);
        updatePlacement();
        if(panel==Panel.TARGET_HUD){
            int ax=width/2,ay=(int)(height*.55);
            g.outline(ax-7,ay+6,14,14,0xFF728D9A);
            g.fill(ax-9,ay+22,ax+9,ay+50,0x88263C4B);
            g.horizontalLine(ax-5,ax+5,ay,accent);g.verticalLine(ax,ay-5,ay+5,accent);
        }
        if(panel==Panel.WATERMARK)Watermark.preview(g,font,placement.x,placement.y,placement.scale,VisualFX.config.watermarkPalette,System.nanoTime()/1e9);
        else if(panel==Panel.EFFECT_STATUS)EffectStatus.preview(g,font,placement.x,placement.y,placement.scale);
        else {
            g.pose().pushMatrix();g.pose().translate((float)placement.x,(float)placement.y).scale((float)placement.scale,(float)placement.scale);
            TargetHud.drawCard(g,font,0,0,180,"Игрок","16.0 / 20.0 HP",.8,VisualFX.config.hudPalette,System.nanoTime()/1e9,1);
            g.pose().popMatrix();
        }
        if(placement.contains(mx,my)||dragging)g.outline((int)placement.x-2,(int)placement.y-2,(int)Math.ceil(placement.w)+4,(int)Math.ceil(placement.h)+4,0xFFFFFFFF);
        int bar=toolbarY();
        g.fill(4,bar,width-4,bar+33,0xED000000 | MenuTheme.surface(0x080F19,theme,.10));
        String hint=switch(panel){
            case WATERMARK -> "Watermark: перетащите мышью. Колесо — размер.";
            case EFFECT_STATUS -> "Effect Status: перетащите мышью. Колесо — размер.";
            case TARGET_HUD -> "TargetHUD: смещение от головы цели. Колесо — размер.";
        };
        var lines=font.split(Component.literal(hint),width-88);
        for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),10,bar+6+i*10,0xFFE3EDF3,false);
        g.fill(width-76,bar+6,width-10,bar+26,0xFF000000 | MenuTheme.surface(0x20333E,theme,.22));
        g.text(font,"Готово",width-65,bar+12,accent,false);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubled){
        if(e.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;
        updatePlacement();int bar=toolbarY();
        if(e.x()>=width-76&&e.x()<width-10&&e.y()>=bar+6&&e.y()<bar+26){onClose();return true;}
        if(!placement.contains(e.x(),e.y()))return false;
        dragging=true;grabX=e.x()-placement.x;grabY=e.y()-placement.y;return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(!dragging)return false;
        updatePlacement();
        if(panel==Panel.EFFECT_STATUS)placement.moveEffectStatus(e.x()-grabX,e.y()-grabY);
        else placement.move(e.x()-grabX,e.y()-grabY,panel==Panel.WATERMARK);
        return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){
        if(e.button()!=InputConstants.MOUSE_BUTTON_LEFT||!dragging)return false;
        dragging=false;VisualFX.config.save();return true;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(dragging||vertical==0)return false;
        updatePlacement();if(!placement.contains(x,y))return false;
        Config c=VisualFX.config;double change=Math.signum(vertical)*.05;
        switch(panel){
            case WATERMARK -> c.watermarkScale=Math.clamp(c.watermarkScale+change,.5,2);
            case EFFECT_STATUS -> c.effectStatusScale=Math.clamp(c.effectStatusScale+change,.5,2);
            case TARGET_HUD -> c.hudScale=Math.clamp(c.hudScale+change,.5,2);
        }
        c.save();return true;
    }
}


