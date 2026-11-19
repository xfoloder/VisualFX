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
        TARGET("Подсветка цели", "Выделяет только игрока, на которого направлен прицел."),
        HIT("Эффект удара", "Вспышка и ударная волна при вашей атаке."),
        HIT_STRENGTH("Сила вспышки", "Яркость вспышки и размер осколков при ударе.", .1, 2),
        DEATH("Death Effect", "Эффект при смерти цели, выбранной сейчас или до двух секунд назад."),
        DEATH_MODE("Эффект смерти", "Ascend — восхождение света. Exit Hell — огненный разлом."),
        MODE("Вид эффекта", "Нажмите, чтобы выбрать один из семи эффектов."),
        COLOR("Цвет", "Четыре неоновых цвета и переливающийся градиент."),
        SPEED("Скорость", "Скорость вращения и движения эффекта.", .1, 3),
        RADIUS("Размер колец", "Радиус эффекта вокруг выбранного игрока.", .4, 1.6),
        GLOW("Свечение", "Интенсивность мягкого свечения вокруг линий.", 0, 1),
        SHARDS("Осколки", "Количество энергетических осколков: от 0 до 48.", 0, 48),
        SMOOTHING("Плавность", "Чем выше значение, тем медленнее появление и исчезновение.", .05, .7),
        OPACITY("Непрозрачность", "Непрозрачность фона меню.", .25, 1),
        ANIMATIONS("Анимации меню", "Плавное открытие и закрытие меню."),
        RESET("Сброс настроек", "Нажмите дважды за три секунды для сброса всех настроек.");
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
        VISUAL("Visual",0x00F5FF,new Setting[]{Setting.DEATH,Setting.DEATH_MODE,Setting.MODE,Setting.COLOR,Setting.SPEED,Setting.RADIUS,Setting.GLOW,Setting.SHARDS,Setting.SMOOTHING});
        final String title; final int color; final Setting[] settings;
        Category(String title,int color,Setting[] settings){this.title=title;this.color=color;this.settings=settings;}
    }
    private static final String[] PALETTES={"Зелёный","Розовый","Голубой","Фиолетовый","Перелив"};
    private static final Style FONT=Style.EMPTY;
    private static Category lastCategory=Category.VISUAL;
    private Category category=lastCategory;
    private Setting dragging;
    private long previous=System.nanoTime(),resetDeadline;
    private double visibility;
    private boolean closing,previewDeath;
    private int scrollOffset;
    private int left,top,panelWidth,rowHeight,panelHeight,settingsWidth;
    private final EffectPreview preview=new EffectPreview();
    public TerminalScreen(){super(Component.literal("VisualFX terminal"));}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){dragging=null;VisualFX.config.save();closing=true;}
    @Override public void removed(){VisualFX.config.save();}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
    private int tint(int rgb,double opacity){return ((int)(Math.clamp(opacity*visibility,0,1)*255)<<24)|rgb;}
    private void text(GuiGraphicsExtractor g,String value,int x,int y,int color){g.text(font,Component.literal(value).setStyle(FONT),x,y,tint(color,1),false);}
    private void fitted(GuiGraphicsExtractor g,String value,int x,int y,int color,int available){
        if(font.width(value)>available)value=font.plainSubstrByWidth(value,Math.max(0,available-font.width("…")))+"…";
        text(g,value,x,y,color);
    }
    private void layout(){
        panelWidth=Math.min(740,width-12);
        settingsWidth=panelWidth>=400?panelWidth-160:panelWidth;
        rowHeight=Math.min(27,Math.max(18,(height-103)/7));
        panelHeight=103+7*rowHeight;
        left=(width-panelWidth)/2;top=Math.max(2,(height-panelHeight)/2)+(int)((1-visibility)*12);
    }
    private String value(Setting s,Config c){return switch(s){
        case DEATH->c.deathEffect?"Вкл.":"Выкл."; case DEATH_MODE->DeathEffectSystem.NAMES[c.deathMode];
        case TARGET->c.enabled?"Вкл.":"Выкл."; case HIT->c.hitEffect?"Вкл.":"Выкл.";
        case MODE->Effects.NAMES[c.mode]; case COLOR->PALETTES[c.palette];
        case SHARDS->Integer.toString(c.shards); case SMOOTHING->Math.round(c.smoothing*1000)+" мс";
        case GLOW,OPACITY->Math.round(s.value(c)*100)+"%";
        case ANIMATIONS->c.menuAnimations?"Вкл.":"Выкл.";
        case RESET->System.nanoTime()<resetDeadline?"Подтвердить":"Сбросить";
        case RADIUS->String.format(Locale.ROOT,"%.2f м",c.radius);
        default->String.format(Locale.ROOT,"%.2f×",s.value(c));
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
        text(g,"VisualFX / Настройки",left+10,top+9,0x39FF14);
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
        for(int i=scrollOffset;i<Math.min(category.settings.length,scrollOffset+7);i++){
            Setting s=category.settings[i]; int y=top+64+(i-scrollOffset)*rowHeight;
            boolean hover=mx>=left+8&&mx<left+settingsWidth-8&&my>=y-3&&my<y+rowHeight-4;
            if(hover)hovered=s;
            g.fill(left+8,y-3,left+settingsWidth-8,y+rowHeight-4,tint(hover?0x172A34:0x0C141C,c.menuOpacity));
            if(hover||s==dragging)g.fill(left+8,y-3,left+10,y+rowHeight-4,tint(category.color,1));
            fitted(g,s.label,left+18,y,0xD0DCE3,settingsWidth/2-26);
            fitted(g,value(s,c),left+settingsWidth/2,y,s==Setting.COLOR?c.color(0):category.color,settingsWidth/2-16);
            if(s.slider()){
                int x=left+settingsWidth/2,end=left+settingsWidth-18;
                double f=(s.value(c)-s.min)/(s.max-s.min);
                g.fill(x,y+11,end,y+13,tint(0x25353F,1));
                g.fill(x,y+11,x+(int)((end-x)*f),y+13,tint(category.color,1));
            }
        }
        if(settingsWidth<panelWidth)preview.draw(g,font,left+settingsWidth+2,top+61,150,panelHeight-102,c,now/1e9,visibility,previewDeath||hovered==Setting.DEATH||hovered==Setting.DEATH_MODE);
        else if(category!=Category.VISUAL)preview.draw(g,font,left+10,top+65+3*rowHeight,panelWidth-20,panelHeight-108-3*rowHeight,c,now/1e9,visibility,previewDeath||hovered==Setting.DEATH||hovered==Setting.DEATH_MODE);
        String hint=hovered==null?"Колесо мыши — прокрутка. Настройки сохраняются автоматически.":hovered.help;
        if(hovered!=null)g.setTooltipForNextFrame(Component.literal(hovered.label+": "+value(hovered,c)),mx,my);
        // Wrap the help text using the same font as the panel.
        var lines=font.split(Component.literal(hint).setStyle(FONT),panelWidth-24);
        int y=top+panelHeight-31;
        for(int i=0;i<Math.min(2,lines.size());i++)g.text(font,lines.get(i),left+12,y+i*10,tint(0x829BA4,1),false);
    }
    private void slider(Setting s,double x){
        double f=Math.clamp((x-(left+settingsWidth/2))/(settingsWidth/2.-18),0,1);
        s.set(VisualFX.config,s.min+f*(s.max-s.min));
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){
        if(closing||event.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;
        double x=event.x(),y=event.y();
        if(y>=top+32&&y<top+54){
            int tabWidth=(panelWidth-16)/3;
            for(Category tab:Category.values()){
                int start=left+8+tab.ordinal()*tabWidth;
                if(x>=start&&x<start+tabWidth-3){dragging=null;scrollOffset=0;previewDeath=false;resetDeadline=0;lastCategory=category=tab;VisualFX.config.save();return true;}
            }
        }
        for(int i=scrollOffset;i<Math.min(category.settings.length,scrollOffset+7);i++){
            Setting s=category.settings[i];int rowTop=top+61+(i-scrollOffset)*rowHeight;
            if(x<left+8||x>=left+settingsWidth-8||y<rowTop||y>=rowTop+rowHeight-1)continue;
            Config c=VisualFX.config;
            if(s!=Setting.RESET)resetDeadline=0;
            previewDeath=s==Setting.DEATH||s==Setting.DEATH_MODE;
            if(s.slider()){dragging=s;slider(s,x);}
            else switch(s){
                case DEATH->c.deathEffect=!c.deathEffect; case DEATH_MODE->c.deathMode=(c.deathMode+1)%2;
                case TARGET->c.enabled=!c.enabled; case HIT->c.hitEffect=!c.hitEffect;
                case MODE->c.mode=(c.mode+1)%Effects.MODES.length; case COLOR->c.palette=(c.palette+1)%5;
                case ANIMATIONS->c.menuAnimations=!c.menuAnimations;
                case RESET->{if(System.nanoTime()<resetDeadline){VisualFX.config=new Config();resetDeadline=0;}else resetDeadline=System.nanoTime()+3_000_000_000L;}
                default->{}
            }
            VisualFX.config.save();return true;
        }
        return false;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(closing||dragging!=null||x<left||x>=left+settingsWidth||y<top+57||y>=top+panelHeight-35||vertical==0)return false;
        scrollOffset=Math.clamp(scrollOffset+(vertical<0?1:-1),0,Math.max(0,category.settings.length-7));
        return true;
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



