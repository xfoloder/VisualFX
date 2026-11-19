package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import java.util.Locale;

/** HUD receives only name/health snapshots; it does not retain the target entity. */
final class TargetHud {
    private ClientLevel world;
    private String name="",healthText="";
    private int entityId=-1;
    private double health,shownHealth,visibility;
    private boolean active;
    private long last=System.nanoTime();
    void tick(Minecraft c){
        if(world!=c.level){world=c.level;active=false;visibility=0;entityId=-1;name="";health=shownHealth=0;}
        active=false;
        if(c.level==null||!VisualFX.config.targetHud){visibility=0;return;}
        if(c.gui.screen()==null&&c.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof Player p&&p!=c.player&&p.isAlive()){
            name=p.getName().getString();
            float hp=p.getHealth(),max=p.getMaxHealth();
            health=max>0&&Float.isFinite(max)&&Float.isFinite(hp)?Math.clamp(hp/max,0,1):0;
            healthText=String.format(Locale.ROOT,"%.1f / %.1f HP",hp,max);
            if(entityId!=p.getId()){shownHealth=health;entityId=p.getId();}
            active=true;
        }
    }
    void render(GuiGraphicsExtractor g,DeltaTracker tracker){
        long now=System.nanoTime();double dt=Math.min(.1,(now-last)/1e9);last=now;
        visibility+=((active?1:0)-visibility)*(1-Math.exp(-dt/.18));
        shownHealth+=(health-shownHealth)*(1-Math.exp(-dt/.15));
        Minecraft c=Minecraft.getInstance();
        if(c.level==null||!VisualFX.config.targetHud||visibility<.01||c.gui.screen()!=null)return;
        int w=Math.min(180,g.guiWidth()-16);
        int x=Math.clamp(g.guiWidth()/2+24,8,g.guiWidth()-w-8);
        int y=Math.clamp(g.guiHeight()/2+30,8,g.guiHeight()-64);
        drawCard(g,c.font,x,y,w,name,healthText,shownHealth,VisualFX.config.hudPalette,now/1e9,visibility);
    }
    private static int tint(int rgb,double alpha){return ((int)(Math.clamp(alpha,0,1)*255)<<24)|rgb;}
    static void drawCard(GuiGraphicsExtractor g,Font font,int x,int y,int w,String name,String hp,double health,int palette,double time,double alpha){
        int accent=Config.paletteColor(palette,time*.45);
        g.fill(x-2,y-2,x+w+2,y+58,tint(accent,.08*alpha));
        g.fill(x,y,x+w,y+56,tint(0x080F19,.94*alpha));
        g.outline(x,y,w,56,tint(accent,.6*alpha));
        g.fill(x,y,x+3,y+56,tint(accent,alpha));
        String clipped=font.width(name)>w-20?font.plainSubstrByWidth(name,Math.max(0,w-28))+"…":name;
        g.text(font,clipped,x+10,y+8,tint(0xE3EDF3,alpha),false);
        g.text(font,hp,x+10,y+23,tint(0xA4B6C3,alpha),false);
        int start=x+10,end=x+w-10;
        g.fill(start,y+40,end,y+46,tint(0x23313D,alpha));
        int filled=(int)Math.round((end-start)*Math.clamp(health,0,1));
        if(palette<4)g.fill(start,y+40,start+filled,y+46,tint(accent,alpha));
        else for(int i=0;i<filled;i+=4){
            int color=Config.paletteColor(palette,time*.45+i/(double)Math.max(1,end-start)*2);
            g.fill(start+i,y+40,start+Math.min(filled,i+4),y+46,tint(color,alpha));
        }
    }
    static void preview(GuiGraphicsExtractor g,Font font,int x,int y,int width,Config c,double seconds,double alpha){
        g.text(font,"TargetHUD",x+7,y+8,tint(0xC5D8E3,alpha),false);
        drawCard(g,font,x+4,y+30,width-8,"Игрок", "16.0 / 20.0 HP",.8,c.hudPalette,seconds,alpha);
    }
}
