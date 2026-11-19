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
        HUD("TargetHUD", "Показывает имя и здоровье игрока под прицелом."),
        HUD_COLOR("Палитра TargetHUD", "Нажмите цветной образец. Последний образец — переливающийся градиент."),
        HUD_SCALE("Размер TargetHUD", "Масштаб панели цели от 50% до 200%.",.5,2),
        HUD_POSITION("Смещение от цели", "Панель следует за игроком. В редакторе задайте её смещение от головы цели."),
        WATERMARK("Watermark", "Показывает название VisualFX и версию Minecraft."),
        WATERMARK_COLOR("Палитра Watermark", "Выберите цвет логотипа и рамки."),
        WATERMARK_SCALE("Размер Watermark", "Масштаб логотипа от 50% до 200%.",.5,2),
        WATERMARK_POSITION("Положение логотипа", "Откройте редактор и перетащите Watermark мышью."),
        OPT_LIGHT("Лёгкие эффекты", "Уменьшает детализацию и число осколков VisualFX. Выключение возвращает полное качество."),
        OPT_PRESET("Пресет", "Выберите баланс качества, больше FPS или минимум нагрузки. Настройки Minecraft меняются кнопкой ниже."),
        OPT_APPLY("Применить к игре", "Снижает дальность, облака и частицы. Исходные значения сохраняются для восстановления."),
        OPT_RESTORE("Вернуть настройки", "Восстанавливает значения Minecraft до первого применения пресета и отключает лёгкие эффекты."),
        MUSIC_OPEN("Плеер SoundCloud", "Открывает управление. Звук воспроизводится официальным виджетом в браузере."),
        MUSIC_VOLUME("Громкость музыки", "Громкость SoundCloud, независимо от звуков Minecraft.",0,100),
        MUSIC_DISCONNECT("Отключить плеер", "Закрывает локальное соединение. Виджет в браузере остановит воспроизведение."),
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
            case HUD_SCALE->c.hudScale; case WATERMARK_SCALE->c.watermarkScale;
            case MUSIC_VOLUME->c.musicVolume;
            case GLOW->c.glow; case SHARDS->c.shards; case SMOOTHING->c.smoothing; case OPACITY->c.menuOpacity;
            default->0;
        };}
        void set(Config c,double value){switch(this){
            case HIT_STRENGTH->c.hitStrength=value; case SPEED->c.speed=value; case RADIUS->c.radius=value;
            case HUD_SCALE->c.hudScale=value; case WATERMARK_SCALE->c.watermarkScale=value;
            case MUSIC_VOLUME->{c.musicVolume=(int)Math.round(value);SoundCloudPlayer.INSTANCE.command("volume","",c.musicVolume);}
            case GLOW->c.glow=value; case SHARDS->c.shards=(int)Math.round(value);
            case SMOOTHING->c.smoothing=value; case OPACITY->c.menuOpacity=value;
            default->{}
        }}
    }
    private enum Feature {
        TARGET_ESP("Target ESP",new Setting[]{Setting.TARGET,Setting.MODE,Setting.COLOR,Setting.SPEED,Setting.RADIUS,Setting.GLOW,Setting.SHARDS,Setting.SMOOTHING}),
        TARGET_HUD("TargetHUD",new Setting[]{Setting.HUD,Setting.HUD_COLOR,Setting.HUD_SCALE,Setting.HUD_POSITION}),
        WATERMARK("Watermark",new Setting[]{Setting.WATERMARK,Setting.WATERMARK_COLOR,Setting.WATERMARK_SCALE,Setting.WATERMARK_POSITION}),
        OPTIMIZATION("Оптимизация",new Setting[]{Setting.OPT_LIGHT,Setting.OPT_PRESET,Setting.OPT_APPLY,Setting.OPT_RESTORE}),
        SOUND_CLOUD("SoundCloud",new Setting[]{Setting.MUSIC_OPEN,Setting.MUSIC_VOLUME,Setting.MUSIC_DISCONNECT}),
        DEATH_EFFECT("Death Effect",new Setting[]{Setting.DEATH,Setting.DEATH_MODE}),
        HIT_EFFECT("Hit Effect",new Setting[]{Setting.HIT,Setting.HIT_STRENGTH}),
        MENU("Меню",new Setting[]{Setting.OPACITY,Setting.ANIMATIONS,Setting.RESET});
        final String title; final Setting[] settings;
        Feature(String title,Setting[] settings){this.title=title;this.settings=settings;}
    }
    private enum Category {
        COMBAT("Combat",0xFF00CC,new Feature[]{Feature.HIT_EFFECT}),
        MISC("Misc",0x39FF14,new Feature[]{Feature.WATERMARK,Feature.OPTIMIZATION,Feature.SOUND_CLOUD,Feature.MENU}),
        VISUAL("Visual",0x00F5FF,new Feature[]{Feature.TARGET_ESP,Feature.TARGET_HUD,Feature.DEATH_EFFECT});
        final String title; final int color; final Feature[] features;
        Category(String title,int color,Feature[] features){this.title=title;this.color=color;this.features=features;}
    }
    private static final String[] PALETTES={"Зелёный","Розовый","Голубой","Фиолетовый","Перелив"};
    private static final Style FONT=Style.EMPTY;
    private static Category lastCategory=Category.VISUAL;
    private static Feature lastFeature=Feature.TARGET_ESP;
    private Category category=lastCategory;
    private Feature feature=lastFeature;
    private Setting dragging;
    private long previous=System.nanoTime(),resetDeadline;
    private double visibility;
    private boolean closing;
    private int scrollOffset;
    private int left,top,panelWidth,rowHeight,panelHeight,settingsWidth,visibleRows;
    private final EffectPreview preview=new EffectPreview();
    private String optimizationMessage="";
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
    private void palette(GuiGraphicsExtractor g,int x,int y,int available,int selected,double time){
        int cell=available/5;
        for(int i=0;i<5;i++){
            int start=x+i*cell,w=cell-4;
            if(i<4)g.fill(start,y,start+w,y+12,tint(Config.COLORS[i],1));
            else for(int j=0;j<w;j+=2)g.fill(start+j,y,start+Math.min(w,j+2),y+12,tint(Config.paletteColor(4,time*.45+j/(double)w*3),1));
            if(i==selected)g.outline(start-1,y-1,w+2,14,tint(0xFFFFFF,1));
        }
    }
    private void layout(){
        panelWidth=Math.min(740,width-12);
        settingsWidth=panelWidth>=400?panelWidth-160:panelWidth;
        rowHeight=24;
        visibleRows=Math.clamp((height-135)/rowHeight,3,8);
        scrollOffset=Math.clamp(scrollOffset,0,Math.max(0,feature.settings.length-visibleRows));
        panelHeight=127+visibleRows*rowHeight;
        left=(width-panelWidth)/2;top=Math.max(2,(height-panelHeight)/2)+(int)((1-visibility)*12);
    }
    private String value(Setting s,Config c){return switch(s){
        case HUD->c.targetHud?"Вкл.":"Выкл."; case HUD_COLOR->PALETTES[c.hudPalette];
        case OPT_LIGHT->c.optimization?"Вкл.":"Выкл."; case OPT_PRESET->Optimizer.NAMES[c.optimizationPreset];
        case OPT_APPLY->"Применить"; case OPT_RESTORE->c.optimizationBackup==null?"Нет копии":"Восстановить";
        case MUSIC_OPEN->"Открыть"; case MUSIC_DISCONNECT->"Отключить";case MUSIC_VOLUME->c.musicVolume+"%";
        case WATERMARK->c.watermark?"Вкл.":"Выкл."; case WATERMARK_COLOR->PALETTES[c.watermarkPalette];
        case HUD_POSITION,WATERMARK_POSITION->"Переместить";
        case HUD_SCALE,WATERMARK_SCALE->Math.round(s.value(c)*100)+"%";
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
        fitted(g,"VisualFX / "+feature.title,left+10,top+9,0x39FF14,panelWidth-90);
        text(g,"[RSHIFT]",left+panelWidth-64,top+9,0xFF00CC);
        int tabWidth=(panelWidth-16)/3;
        for(Category tab:Category.values()){
            int x=left+8+tab.ordinal()*tabWidth;
            boolean hover=mx>=x&&mx<x+tabWidth-3&&my>=top+32&&my<top+53;
            g.fill(x,top+32,x+tabWidth-3,top+53,tint(tab==category?0x20333E:hover?0x18242E:0x0C141C,c.menuOpacity));
            if(tab==category)g.fill(x,top+52,x+tabWidth-3,top+54,tint(tab.color,1));
            text(g,tab.title,x+9,top+38,tab==category?tab.color:0x91A5AC);
        }
        int featureWidth=(panelWidth-16)/category.features.length;
        for(int i=0;i<category.features.length;i++){
            Feature item=category.features[i];int x=left+8+i*featureWidth;
            boolean hover=mx>=x&&mx<x+featureWidth-3&&my>=top+57&&my<top+78;
            g.fill(x,top+57,x+featureWidth-3,top+78,tint(item==feature?0x20333E:hover?0x18242E:0x0C141C,c.menuOpacity));
            if(item==feature)g.fill(x,top+77,x+featureWidth-3,top+79,tint(category.color,1));
            fitted(g,item.title,x+9,top+63,item==feature?0xFFFFFF:0x91A5AC,featureWidth-16);
        }
        Setting hovered=null;
        for(int i=scrollOffset;i<Math.min(feature.settings.length,scrollOffset+visibleRows);i++){
            Setting s=feature.settings[i]; int y=top+88+(i-scrollOffset)*rowHeight;
            boolean hover=mx>=left+8&&mx<left+settingsWidth-8&&my>=y-3&&my<y+rowHeight-4;
            if(hover)hovered=s;
            g.fill(left+8,y-3,left+settingsWidth-8,y+rowHeight-4,tint(hover?0x172A34:0x0C141C,c.menuOpacity));
            if(hover||s==dragging)g.fill(left+8,y-3,left+10,y+rowHeight-4,tint(category.color,1));
            fitted(g,s.label,left+18,y,0xD0DCE3,settingsWidth/2-26);
            if(s==Setting.HUD_COLOR||s==Setting.WATERMARK_COLOR)palette(g,left+settingsWidth/2,y-1,settingsWidth/2-16,s==Setting.HUD_COLOR?c.hudPalette:c.watermarkPalette,now/1e9);
            else fitted(g,value(s,c),left+settingsWidth/2,y,s==Setting.COLOR?c.color(0):category.color,settingsWidth/2-16);
            if(s.slider()){
                int x=left+settingsWidth/2,end=left+settingsWidth-18;
                double f=(s.value(c)-s.min)/(s.max-s.min);
                g.fill(x,y+11,end,y+13,tint(0x25353F,1));
                g.fill(x,y+11,x+(int)((end-x)*f),y+13,tint(category.color,1));
            }
        }
        if(settingsWidth<panelWidth)drawPreview(g,c,left+settingsWidth+2,top+85,150,panelHeight-126,now/1e9);
        else {
            int used=Math.min(feature.settings.length,visibleRows);
            int y=top+88+used*rowHeight;
            drawPreview(g,c,left+10,y,panelWidth-20,top+panelHeight-37-y,now/1e9);
        }
        String hint=feature==Feature.OPTIMIZATION&&!optimizationMessage.isEmpty()?optimizationMessage:hovered==null?"Колесо мыши — прокрутка. Настройки сохраняются автоматически.":hovered.help;
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
    private void selectFeature(Feature selected){
        dragging=null;scrollOffset=0;resetDeadline=0;
        lastFeature=feature=selected;VisualFX.config.save();
    }
    private void drawPreview(GuiGraphicsExtractor g,Config c,int x,int y,int w,int h,double time){
        if(h<48)return;
        switch(feature){
            case SOUND_CLOUD->{
                text(g,"SoundCloud",x+6,y+8,0xFF7733);
                String info=SoundCloudPlayer.INSTANCE.connected()?"Браузер подключён. Откройте плеер для управления.":"Звук через браузер. Откройте плеер и вставьте ссылку SoundCloud.";
                var lines=font.split(Component.literal(info),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.text(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
            case TARGET_HUD->{if(h>=90)TargetHud.preview(g,font,x,y,w,c,time,visibility);}
            case WATERMARK->{
                text(g,"Watermark",x+6,y+8,0x39FF14);
                Watermark.draw(g,font,x+4,y+30,Math.min(1,(w-8)/172.0),c.watermarkPalette,time);
            }
            case OPTIMIZATION->{
                text(g,Optimizer.NAMES[c.optimizationPreset],x+6,y+8,0x39FF14);
                var lines=font.split(Component.literal(Optimizer.DETAILS[c.optimizationPreset]),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.text(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
            case TARGET_ESP,HIT_EFFECT,DEATH_EFFECT->preview.draw(g,font,x,y,w,h,c,time,visibility,feature==Feature.DEATH_EFFECT);
            case MENU->{
                text(g,"Настройки меню",x+6,y+8,0x39FF14);
                var lines=font.split(Component.literal("Параметры сохраняются автоматически. Right Shift — закрыть меню."),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.text(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){
        if(closing||event.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;
        double x=event.x(),y=event.y();
        if(y>=top+32&&y<top+54){
            int tabWidth=(panelWidth-16)/3;
            for(Category tab:Category.values()){
                int start=left+8+tab.ordinal()*tabWidth;
                if(x>=start&&x<start+tabWidth-3){lastCategory=category=tab;selectFeature(tab.features[0]);return true;}
            }
        }
        if(y>=top+57&&y<top+79){
            int featureWidth=(panelWidth-16)/category.features.length;
            for(int i=0;i<category.features.length;i++){
                int start=left+8+i*featureWidth;
                if(x>=start&&x<start+featureWidth-3){selectFeature(category.features[i]);return true;}
            }
        }
        for(int i=scrollOffset;i<Math.min(feature.settings.length,scrollOffset+visibleRows);i++){
            Setting s=feature.settings[i];int rowTop=top+85+(i-scrollOffset)*rowHeight;
            if(x<left+8||x>=left+settingsWidth-8||y<rowTop||y>=rowTop+rowHeight-1)continue;
            Config c=VisualFX.config;
            if(s!=Setting.RESET)resetDeadline=0;
            if(s==Setting.MUSIC_OPEN){VisualFX.config.save();minecraft.gui.setScreen(new SoundCloudScreen(this));return true;}
            if(s==Setting.HUD_POSITION||s==Setting.WATERMARK_POSITION){
                VisualFX.config.save();minecraft.gui.setScreen(new HudEditorScreen(this,s==Setting.WATERMARK_POSITION));return true;
            }


            if(s.slider()){dragging=s;slider(s,x);}
            else switch(s){
                case MUSIC_DISCONNECT->SoundCloudPlayer.INSTANCE.stop();
                case OPT_LIGHT->{c.optimization=!c.optimization;optimizationMessage="";}
                case OPT_PRESET->{c.optimizationPreset=(c.optimizationPreset+1)%3;optimizationMessage="";}
                case OPT_APPLY->optimizationMessage=Optimizer.apply();
                case OPT_RESTORE->optimizationMessage=Optimizer.restore();
                                case HUD->c.targetHud=!c.targetHud;
                case WATERMARK->c.watermark=!c.watermark;
                case HUD_COLOR,WATERMARK_COLOR->{
                    int start=left+settingsWidth/2,swatchWidth=(settingsWidth/2-16)/5;
                    int current=s==Setting.HUD_COLOR?c.hudPalette:c.watermarkPalette;
                    int selected=x>=start?Math.clamp((int)((x-start)/Math.max(1,swatchWidth)),0,4):(current+1)%5;
                    if(s==Setting.HUD_COLOR)c.hudPalette=selected;else c.watermarkPalette=selected;
                }
                case DEATH->c.deathEffect=!c.deathEffect; case DEATH_MODE->c.deathMode=(c.deathMode+1)%2;
                case TARGET->c.enabled=!c.enabled; case HIT->c.hitEffect=!c.hitEffect;
                case MODE->c.mode=(c.mode+1)%Effects.MODES.length; case COLOR->c.palette=(c.palette+1)%5;
                case ANIMATIONS->c.menuAnimations=!c.menuAnimations;
                case RESET->{if(System.nanoTime()<resetDeadline){Config defaults=new Config();defaults.optimizationBackup=c.optimizationBackup;VisualFX.config=defaults;resetDeadline=0;}else resetDeadline=System.nanoTime()+3_000_000_000L;}
                default->{}
            }
            VisualFX.config.save();return true;
        }
        return false;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        if(closing||dragging!=null||x<left||x>=left+settingsWidth||y<top+81||y>=top+panelHeight-35||vertical==0)return false;
        scrollOffset=Math.clamp(scrollOffset+(vertical<0?1:-1),0,Math.max(0,feature.settings.length-visibleRows));
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





