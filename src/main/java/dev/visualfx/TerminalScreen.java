package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import java.util.Locale;

public final class TerminalScreen extends Screen {
    private enum Setting {
        TARGET("Target ESP", "Highlight the player under your crosshair."),
        HIT("Hit Effect", "Flash and shockwave on your local attack."),
        HIT_STRENGTH("Hit intensity", "Strength of the hit flash and energy shards.", .1, 2),
        MODE("ESP mode", "Click to cycle through four effect styles."),
        COLOR("Color palette", "Four neon colors or an animated spectrum."),
        SPEED("Rotation speed", "Animation speed multiplier.", .1, 3),
        RADIUS("Ring size", "Radius of the effect around the target.", .4, 1.6),
        GLOW("Glow intensity", "Opacity of the outer neon shells.", 0, 1),
        SHARDS("Shard count", "Bounded particle geometry budget.", 0, 48),
        SMOOTHING("Smoothing", "Fade time: higher values produce slower transitions.", .05, .7),
        OPACITY("Menu opacity", "Opacity of this terminal panel.", .25, 1),
        ANIMATIONS("Menu animations", "Animate opening and closing the terminal."),
        RESET("Reset settings", "Click twice to restore all default settings.");
        final String label,help; final double min,max;
        Setting(String label,String help){this(label,help,0,0);}
        Setting(String label,String help,double min,double max){this.label=label;this.help=help;this.min=min;this.max=max;}
        boolean slider(){return max>min;}
        double value(Config c){return switch(this){
            case HIT_STRENGTH->c.hitStrength; case SPEED->c.speed; case RADIUS->c.radius;
            case GLOW->c.glow; case SHARDS->c.shards; case SMOOTHING->c.smoothing; case OPACITY->c.menuOpacity;
            default->0;
        };}
        void set(Config c,double value){switch(this){
            case HIT_STRENGTH->c.hitStrength=value; case SPEED->c.speed=value; case RADIUS->c.radius=value;
            case GLOW->c.glow=value; case SHARDS->c.shards=(int)Math.round(value);
            case SMOOTHING->c.smoothing=value; case OPACITY->c.menuOpacity=value;
            default->{}
        }}
    }
    private enum Category {
        COMBAT("Combat",0xFF00CC,new Setting[]{Setting.TARGET,Setting.HIT,Setting.HIT_STRENGTH}),
        MISC("Misc",0x39FF14,new Setting[]{Setting.OPACITY,Setting.ANIMATIONS,Setting.RESET}),
        VISUAL("Visual",0x00F5FF,new Setting[]{Setting.MODE,Setting.COLOR,Setting.SPEED,Setting.RADIUS,Setting.GLOW,Setting.SHARDS,Setting.SMOOTHING});
        final String title; final int color; final Setting[] settings;
        Category(String title,int color,Setting[] settings){this.title=title;this.color=color;this.settings=settings;}
    }
    private static final String[] PALETTES={"Acid Green","Neon Pink","Electric Cyan","Neon Purple","Spectrum"};
    private static final Style FONT=Style.EMPTY.withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("uniform")));
    private static Category lastCategory=Category.VISUAL;
    private Category category=lastCategory;
    private Setting dragging;
    private long previous=System.nanoTime(),resetDeadline;
    private double visibility;
    private boolean closing;
    private int left,top,panelWidth,rowHeight,panelHeight;
    public TerminalScreen(){super(Component.literal("VisualFX terminal"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){dragging=null;VisualFX.config.save();closing=true;}
    @Override public void removed(){VisualFX.config.save();}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
    private int tint(int rgb,double opacity){return ((int)(Math.clamp(opacity*visibility,0,1)*255)<<24)|rgb;}
    private void text(GuiGraphicsExtractor g,String value,int x,int y,int color){g.text(font,Component.literal(value).setStyle(FONT),x,y,tint(color,1),false);}
    private void layout(){
        panelWidth=Math.min(520,width-12);
        rowHeight=Math.min(27,Math.max(18,(height-103)/7));
        panelHeight=103+7*rowHeight;
        left=(width-panelWidth)/2;top=Math.max(2,(height-panelHeight)/2)+(int)((1-visibility)*12);
    }
    private String value(Setting s,Config c){return switch(s){
        case TARGET->c.enabled?"ON":"OFF"; case HIT->c.hitEffect?"ON":"OFF";
        case MODE->Effects.NAMES[c.mode]; case COLOR->PALETTES[c.palette];
        case SHARDS->Integer.toString(c.shards); case SMOOTHING->Math.round(c.smoothing*1000)+" ms";
        case GLOW,OPACITY->Math.round(s.value(c)*100)+"%";
        case ANIMATIONS->c.menuAnimations?"ON":"OFF";
        case RESET->System.nanoTime()<resetDeadline?"Click to confirm":"Restore defaults";
        default->String.format(Locale.ROOT,"%.2fx",s.value(c));
    };}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        Config c=VisualFX.config;
        long now=System.nanoTime();double dt=Math.min(.1,(now-previous)/1e9);previous=now;
        visibility=c.menuAnimations?visibility+((closing?0:1)-visibility)*(1-Math.exp(-dt*13)):(closing?0:1);
        if(closing&&visibility<.015){minecraft.gui.setScreen(null);return;}
        layout();
        g.fill(0,0,width,height,tint(0x020409,.62));
        g.fill(left-1,top-1,left+panelWidth+1,top+panelHeight+1,tint(category.color,.65));
        g.fill(left,top,left+panelWidth,top+panelHeight,tint(0x070B10,c.menuOpacity));
        g.fill(left,top,left+panelWidth,top+27,tint(0x101923,c.menuOpacity));
        text(g,"visualfx@neon:~$ ./control",left+10,top+9,0x39FF14);
        text(g,"[RSHIFT]",left+panelWidth-64,top+9,0xFF00CC);
        int tabWidth=(panelWidth-16)/3;
        for(Category tab:Category.values()){
            int x=left+8+tab.ordinal()*tabWidth;
            boolean hover=mx>=x&&mx<x+tabWidth-3&&my>=top+32&&my<top+53;
            g.fill(x,top+32,x+tabWidth-3,top+53,tint(tab==category?0x20333E:hover?0x18242E:0x0C141C,c.menuOpacity));
            if(tab==category)g.fill(x,top+52,x+tabWidth-3,top+54,tint(tab.color,1));
            text(g,tab.title,x+9,top+38,tab==category?tab.color:0x91A5AC);
        }
        Setting hovered=null;
        for(int i=0;i<category.settings.length;i++){
            Setting s=category.settings[i]; int y=top+64+i*rowHeight;
            boolean hover=mx>=left+8&&mx<left+panelWidth-8&&my>=y-3&&my<y+rowHeight-4;
            if(hover)hovered=s;
            g.fill(left+8,y-3,left+panelWidth-8,y+rowHeight-4,tint(hover?0x172A34:0x0C141C,c.menuOpacity));
            if(hover||s==dragging)g.fill(left+8,y-3,left+10,y+rowHeight-4,tint(category.color,1));
            text(g,s.label,left+18,y,0xA7BBC3);
            text(g,value(s,c),left+panelWidth/2,y,s==Setting.COLOR?c.color(0):category.color);
            if(s.slider()){
                int x=left+panelWidth/2,end=left+panelWidth-18;
                double f=(s.value(c)-s.min)/(s.max-s.min);
                g.fill(x,y+11,end,y+13,tint(0x25353F,1));
                g.fill(x,y+11,x+(int)((end-x)*f),y+13,tint(category.color,1));
            }
        }
        String hint=hovered==null?"Click a switch or drag a slider. Changes saved automatically.":hovered.help;
        // Wrap the help text using the same font as the panel.
        var lines=font.split(Component.literal(hint).setStyle(FONT),panelWidth-24);
        int y=top+panelHeight-31;
        for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),left+12,y+i*10,tint(0x829BA4,1),false);
    }
    private void slider(Setting s,double x){
        double f=Math.clamp((x-(left+panelWidth/2))/(panelWidth/2.-18),0,1);
        s.set(VisualFX.config,s.min+f*(s.max-s.min));
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){
        if(closing||event.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;
        double x=event.x(),y=event.y();
        if(y>=top+32&&y<top+54){
            int tabWidth=(panelWidth-16)/3;
            for(Category tab:Category.values()){
                int start=left+8+tab.ordinal()*tabWidth;
                if(x>=start&&x<start+tabWidth-3){dragging=null;resetDeadline=0;lastCategory=category=tab;VisualFX.config.save();return true;}
            }
        }
        for(int i=0;i<category.settings.length;i++){
            Setting s=category.settings[i];int rowTop=top+61+i*rowHeight;
            if(x<left+8||x>=left+panelWidth-8||y<rowTop||y>=rowTop+rowHeight-1)continue;
            Config c=VisualFX.config;
            if(s!=Setting.RESET)resetDeadline=0;
            if(s.slider()){dragging=s;slider(s,x);}
            else switch(s){
                case TARGET->c.enabled=!c.enabled; case HIT->c.hitEffect=!c.hitEffect;
                case MODE->c.mode=(c.mode+1)%4; case COLOR->c.palette=(c.palette+1)%5;
                case ANIMATIONS->c.menuAnimations=!c.menuAnimations;
                case RESET->{if(System.nanoTime()<resetDeadline){VisualFX.config=new Config();resetDeadline=0;}else resetDeadline=System.nanoTime()+3_000_000_000L;}
                default->{}
            }
            VisualFX.config.save();return true;
        }
        return false;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(dragging==null||closing)return false;
        slider(dragging,event.x());return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){
        if(event.button()!=InputConstants.MOUSE_BUTTON_LEFT||dragging==null)return false;
        dragging=null;VisualFX.config.save();return true;
    }
}
