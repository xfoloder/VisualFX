package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import java.util.Locale;

public final class TerminalScreen extends Screen {
    private static final String[] SEASONS = {"Выключены", "Снежинки", "Осенние листья"};

    private enum Setting {
        ITEM_PHYSICS("Физика предметов", "Предметы кувыркаются в воздухе и остаются лежать на земле."),
        ITEM_SCROLLER("Перенос колесом", "Удерживайте Shift и прокрутите над стопкой, чтобы быстро перенести её."),
        SEASON_MODE("Вид частиц", "Нажмите, чтобы выбрать снежинки, осенние листья или выключить частицы."),
        SEASON_DENSITY("Количество", "Плотность сезонных частиц под открытым небом.", 1, 5),
        PRESENCE_ENABLED("Автоподключение", "Подключает сохранённую комнату при входе на сервер Minecraft."),
        PRESENCE_SHARE("Показывать меню", "Передаёт вкладку и факт изменения настроек."),
        PRESENCE_OPEN("Настроить комнату", "Введите общий адрес ретранслятора и приватный ключ комнаты."),
        TARGET("Подсветка цели", "Выделяет только игрока, на которого направлен прицел."),
        COLOR_HIT("Окраска при ударе", "Модель игрока меняет цвет при вашем ударе и плавно возвращает обычный оттенок."),
        COLOR_HIT_PALETTE("Палитра Color Hit", "Выберите образец или настройте цвет в редакторе."),
        HIT("Эффект удара", "Вспышка и ударная волна при вашей атаке."),
        HIT_MODE("Вид удара", "Выберите один из шести разных эффектов удара."),
        HIT_DURATION("Длительность", "Время затухания эффекта удара в секундах.",.25,1.2),
        HIT_STRENGTH("Сила вспышки", "Яркость вспышки и размер осколков при ударе.", .1, 2),
        HUD("TargetHUD", "Показывает имя и здоровье игрока под прицелом."),
        HUD_COLOR("Палитра TargetHUD", "Нажмите образец: готовый цвет, перелив, свой цвет или свой градиент."),
        COLOR_EDIT("Редактор цветов", "Полоса оттенков, HEX и собственный градиент. Изменения сохраняются кнопкой «Применить»."),
        ECHO("Neon Echo", "Неоновая запись движения выбранного игрока. Работает отдельно от Target ESP."),
        ECHO_COLOR("Цвет отголосков", "Собственная палитра и градиент Neon Echo."),
        ECHO_LENGTH("Длина записи", "Сколько секунд движения хранится в световом следе.",.25,1.5),
        ECHO_GHOSTS("Отголоски", "Количество геометрических силуэтов. В оптимизации — не больше трёх.",2,8),
        ECHO_INTENSITY("Интенсивность", "Яркость силуэтов и соединяющих их световых лент.",.2,1),
        ECHO_PULSE("Импульс удара", "При атаке вдоль записи движения проходит световой импульс."),
        HUD_SCALE("Размер TargetHUD", "Масштаб панели цели от 50% до 200%.",.5,2),
        HUD_POSITION("Смещение от цели", "Панель следует за игроком. В редакторе задайте её смещение от головы цели."),
        EFFECT_STATUS("Effect Status", "Показывает активные эффекты, их уровни и оставшееся время."),
        EFFECT_STATUS_SCALE("Размер панели", "Масштаб окна эффектов от 50% до 200%.",.5,2),
        EFFECT_STATUS_POSITION("Положение панели", "Откройте редактор и перетащите окно эффектов мышью."),
        WATERMARK("Watermark", "Показывает FPS, пинг и координаты XYZ. По Right Shift меню раскрывается из Watermark."),
        WATERMARK_COLOR("Палитра Watermark", "Выберите цвет логотипа и рамки."),
        WATERMARK_SCALE("Размер Watermark", "Масштаб логотипа от 50% до 200%.",.5,2),
        WATERMARK_POSITION("Положение логотипа", "Откройте редактор и перетащите Watermark мышью."),
        RUST_GEOMETRY("Расчёт геометрии", "Переключает Java и Rust для сравнения FPS при одинаковых эффектах. Rust доступен на Windows x64."),
        OPT_LIGHT("Лёгкие эффекты", "Уменьшает детализацию и число осколков VisualFX. Выключение возвращает полное качество."),
        OPT_PRESET("Пресет", "Выберите баланс качества, больше FPS или минимум нагрузки. Настройки Minecraft меняются кнопкой ниже."),
        OPT_APPLY("Применить к игре", "Снижает дальность, облака и частицы. Исходные значения сохраняются для восстановления."),
        OPT_RESTORE("Вернуть настройки", "Восстанавливает значения Minecraft до первого применения пресета и отключает лёгкие эффекты."),
        MUSIC_OPEN("Плеер SoundCloud", "Открывает управление. Звук воспроизводится официальным виджетом в браузере."),
        MUSIC_VOLUME("Громкость музыки", "Громкость SoundCloud, независимо от звуков Minecraft.",0,100),
        MUSIC_DISCONNECT("Отключить плеер", "Закрывает локальное соединение. Виджет в браузере остановит воспроизведение."),
        DEATH("Death Effect", "Эффект при смерти цели, выбранной сейчас или до двух секунд назад."),
        DEATH_MODE("Эффект смерти", "Ascend — восхождение света. Exit Hell — огненный разлом."),
        MODE("Вид эффекта", "Нажмите, чтобы выбрать один из тринадцати эффектов."),
        COLOR("Цвет", "Готовые цвета, перелив и два пользовательских варианта из редактора."),
        SPEED("Скорость", "Скорость вращения и движения эффекта.", .1, 3),
        RADIUS("Размер колец", "Радиус эффекта вокруг выбранного игрока.", .4, 1.6),
        GLOW("Свечение", "Интенсивность мягкого свечения вокруг линий.", 0, 1),
        SHARDS("Осколки", "Количество энергетических осколков: от 0 до 48.", 0, 48),
        SMOOTHING("Плавность", "Чем выше значение, тем медленнее появление и исчезновение.", .05, .7),
        OPACITY("Непрозрачность", "Непрозрачность фона меню.", .25, 1),
        MENU_COLOR("Цвет всего меню", "Палитра фона, рамок, вкладок и элементов управления."),
        MENU_SCALE("Размер меню", "Масштаб 65–150%. Применяется после отпускания. В маленьком окне ограничивается доступным местом.",.65,1.5),
        MENU_CENTER("Положение меню", "Возвращает окно меню в центр экрана."),
        ANIMATIONS("Анимации меню", "Плавное открытие и закрытие меню."),
        RESET("Сброс настроек", "Нажмите дважды за три секунды для сброса всех настроек.");
        final String label,help; final double min,max;
        Setting(String label,String help){this(label,help,0,0);}
        Setting(String label,String help,double min,double max){this.label=label;this.help=help;this.min=min;this.max=max;}
        boolean slider(){return max>min;}
        double value(Config c){return switch(this){
            case HIT_STRENGTH->c.hitStrength; case SPEED->c.speed; case RADIUS->c.radius;
            case HIT_DURATION->c.hitDuration;
            case ECHO_LENGTH->c.echoLength;case ECHO_GHOSTS->c.echoGhosts;case ECHO_INTENSITY->c.echoIntensity;
            case HUD_SCALE->c.hudScale; case WATERMARK_SCALE->c.watermarkScale;
            case EFFECT_STATUS_SCALE->c.effectStatusScale;
            case MUSIC_VOLUME->c.musicVolume;
            case SEASON_DENSITY->c.seasonDensity;
            case MENU_SCALE->c.menuScale;
            case GLOW->c.glow; case SHARDS->c.shards; case SMOOTHING->c.smoothing; case OPACITY->c.menuOpacity;
            default->0;
        };}
        void set(Config c,double value){switch(this){
            case HIT_STRENGTH->c.hitStrength=value; case SPEED->c.speed=value; case RADIUS->c.radius=value;
            case HIT_DURATION->c.hitDuration=value;
            case ECHO_LENGTH->c.echoLength=value;case ECHO_GHOSTS->c.echoGhosts=(int)Math.round(value);case ECHO_INTENSITY->c.echoIntensity=value;
            case HUD_SCALE->c.hudScale=value; case WATERMARK_SCALE->c.watermarkScale=value;
            case EFFECT_STATUS_SCALE->c.effectStatusScale=value;
            case MUSIC_VOLUME->{c.musicVolume=(int)Math.round(value);SoundCloudPlayer.INSTANCE.command("volume","",c.musicVolume);}
            case MENU_SCALE->c.menuScale=value;
            case SEASON_DENSITY->c.seasonDensity=Math.round(value);
            case GLOW->c.glow=value; case SHARDS->c.shards=(int)Math.round(value);
            case SMOOTHING->c.smoothing=value; case OPACITY->c.menuOpacity=value;
            default->{}
        }}
    }
    private enum Feature {
        NEON_ECHO("Neon Echo",new Setting[]{Setting.ECHO,Setting.ECHO_COLOR,Setting.COLOR_EDIT,Setting.ECHO_LENGTH,Setting.ECHO_GHOSTS,Setting.ECHO_INTENSITY,Setting.ECHO_PULSE}),
        TARGET_ESP("Target ESP",new Setting[]{Setting.TARGET,Setting.MODE,Setting.COLOR,Setting.COLOR_EDIT,Setting.SPEED,Setting.RADIUS,Setting.GLOW,Setting.SHARDS,Setting.SMOOTHING}),
        TARGET_HUD("TargetHUD",new Setting[]{Setting.HUD,Setting.HUD_COLOR,Setting.COLOR_EDIT,Setting.HUD_SCALE,Setting.HUD_POSITION}),
        EFFECT_STATUS("Effect Status",new Setting[]{Setting.EFFECT_STATUS,Setting.EFFECT_STATUS_SCALE,Setting.EFFECT_STATUS_POSITION}),
        WATERMARK("Watermark",new Setting[]{Setting.WATERMARK,Setting.WATERMARK_COLOR,Setting.COLOR_EDIT,Setting.WATERMARK_SCALE,Setting.WATERMARK_POSITION}),
        OPTIMIZATION("Оптимизация",new Setting[]{Setting.RUST_GEOMETRY,Setting.OPT_LIGHT,Setting.OPT_PRESET,Setting.OPT_APPLY,Setting.OPT_RESTORE}),
        SOUND_CLOUD("SoundCloud",new Setting[]{Setting.MUSIC_OPEN,Setting.MUSIC_VOLUME,Setting.MUSIC_DISCONNECT}),
        DEATH_EFFECT("Death Effect",new Setting[]{Setting.DEATH,Setting.DEATH_MODE}),
        COLOR_HIT("Color Hit",new Setting[]{Setting.COLOR_HIT,Setting.COLOR_HIT_PALETTE,Setting.COLOR_EDIT}),
        HIT_EFFECT("Hit Effect",new Setting[]{Setting.HIT,Setting.HIT_MODE,Setting.HIT_STRENGTH,Setting.HIT_DURATION}),
        ITEM_PHYSICS("Item Physics", new Setting[]{Setting.ITEM_PHYSICS}),
        ITEM_SCROLLER("Item Scroller", new Setting[]{Setting.ITEM_SCROLLER}),
        SEASONAL("Частицы", new Setting[]{Setting.SEASON_MODE, Setting.SEASON_DENSITY}),
        PRESENCE("Связь", new Setting[]{Setting.PRESENCE_ENABLED, Setting.PRESENCE_SHARE, Setting.PRESENCE_OPEN}),
        MENU("Меню",new Setting[]{Setting.MENU_COLOR,Setting.COLOR_EDIT,Setting.MENU_SCALE,Setting.MENU_CENTER,Setting.OPACITY,Setting.ANIMATIONS,Setting.RESET});
        final String title; final Setting[] settings;
        Feature(String title,Setting[] settings){this.title=title;this.settings=settings;}
    }
    private enum Category {
        COMBAT("Combat",new Feature[]{Feature.HIT_EFFECT,Feature.COLOR_HIT}),
        MISC("Misc",new Feature[]{Feature.WATERMARK,Feature.OPTIMIZATION,Feature.SOUND_CLOUD,Feature.MENU,Feature.ITEM_SCROLLER,Feature.PRESENCE}),
        VISUAL("Visual",new Feature[]{Feature.NEON_ECHO,Feature.TARGET_ESP,Feature.TARGET_HUD,Feature.EFFECT_STATUS,Feature.DEATH_EFFECT,Feature.ITEM_PHYSICS,Feature.SEASONAL});
        final String title;final Feature[] features;
        Category(String title,Feature[] features){this.title=title;this.features=features;}
    }
    private static final String[] PALETTES={"Зелёный","Розовый","Голубой","Фиолетовый","Перелив","Свой цвет","Свой градиент"};
    private static final Style FONT=Style.EMPTY;
    private static Category lastCategory=Category.VISUAL;
    private static Feature lastFeature=Feature.NEON_ECHO;
    private Category category=lastCategory;
    private Feature feature=lastFeature;
    private Setting dragging;
    private long previous=System.nanoTime(),resetDeadline, lastAdjustment;
    private static final double TRANSITION_SECONDS = .32;
    private final HudPlacement watermarkAnchor = new HudPlacement();
    private double progress, visibility;
    private boolean closing;
    private boolean draggingWindow;
    private double uiScale=1,grabX,grabY;
    private int canvasWidth,canvasHeight;
    private int scrollOffset;
    private int left,top,panelWidth,rowHeight,panelHeight,settingsWidth,visibleRows;
    private final EffectPreview preview=new EffectPreview();
    private String optimizationMessage="";
    public TerminalScreen(){super(Component.literal("VisualFX terminal"));}
    @Override public boolean isPauseScreen(){return false;}

    Presence.Activity presenceActivity() {
        if (closing) return Presence.Activity.IN_GAME;
        if (dragging != null || System.nanoTime() < lastAdjustment) return Presence.Activity.ADJUSTING;
        return Presence.Activity.valueOf(feature.name());
    }
    @Override public void onClose(){dragging=null;draggingWindow=false;VisualFX.config.save();closing=true;}
    void toggleFromKey() {
        if (closing) closing = false;
        else onClose();
    }
    @Override public void removed(){VisualFX.config.save();}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    private int tint(int rgb,double opacity){return ((int)(Math.clamp(opacity*visibility,0,1)*255)<<24)|rgb;}
    private void text(GuiGraphics g,String value,int x,int y,int color){g.drawString(font,Component.literal(value).setStyle(FONT),x,y,tint(color,1),false);}
    private void fitted(GuiGraphics g,String value,int x,int y,int color,int available){
        if(font.width(value)>available)value=font.plainSubstrByWidth(value,Math.max(0,available-font.width("…")))+"…";
        text(g,value,x,y,color);
    }
    private void palette(GuiGraphics g,int x,int y,int available,int selected,double time,ColorStyle style){
        int cell=available/7;
        for(int i=0;i<7;i++){
            int start=x+i*cell,w=cell-4;
            if(i<4||i==5)g.fill(start,y,start+w,y+12,tint(i==5?style.start:Config.COLORS[i],1));
            else for(int j=0;j<w;j+=2)g.fill(start+j,y,start+Math.min(w,j+2),y+12,tint(i==6?ColorStyle.mix(style.start,style.end,j/(double)Math.max(1,w-2)):Config.paletteColor(4,time*.45+j/(double)w*3),1));
            if(i==selected)g.renderOutline(start-1,y-1,w+2,14,tint(0xFFFFFF,1));
        }
    }
    private void layout(){
        Config c=VisualFX.config;
        panelWidth=Math.min(620,Math.max(300,width-40));
        rowHeight=24;
        visibleRows=Math.clamp((height-175)/rowHeight,3,7);
        panelHeight=127+visibleRows*rowHeight;
        double fit=Math.min(width/(panelWidth+12.0),height/(panelHeight+12.0));
        uiScale=Math.max(.1,Math.min(dragging==Setting.MENU_SCALE?uiScale:c.menuScale,fit));
        canvasWidth=(int)(width/uiScale);canvasHeight=(int)(height/uiScale);
        settingsWidth=panelWidth>=360?panelWidth-160:panelWidth;
        scrollOffset=Math.clamp(scrollOffset,0,Math.max(0,feature.settings.length-visibleRows));
        left=6+(int)(Math.max(0,canvasWidth-panelWidth-12)*c.menuX);
        top=6+(int)(Math.max(0,canvasHeight-panelHeight-12)*c.menuY);
    }
    private static ColorTarget colorTarget(Setting setting) {
        return switch (setting) {
            case COLOR -> ColorTarget.ESP;
            case HUD_COLOR -> ColorTarget.HUD;
            case ECHO_COLOR -> ColorTarget.ECHO;
            case COLOR_HIT_PALETTE -> ColorTarget.COLOR_HIT;
            case WATERMARK_COLOR -> ColorTarget.WATERMARK;
            case MENU_COLOR -> ColorTarget.MENU;
            default -> null;
        };
    }
    private String value(Setting s,Config c){return switch(s){
        case HUD->c.targetHud?"Вкл.":"Выкл."; case HUD_COLOR->PALETTES[c.hudPalette];
        case ITEM_PHYSICS->c.itemPhysics?"Вкл.":"Выкл.";
        case ITEM_SCROLLER->c.itemScroller?"Вкл.":"Выкл.";
        case SEASON_MODE->SEASONS[c.seasonParticles];
        case SEASON_DENSITY->Long.toString(Math.round(c.seasonDensity));
        case PRESENCE_ENABLED->c.presenceEnabled?"Вкл.":"Выкл.";
        case PRESENCE_SHARE->c.presenceShareMenu?"Вкл.":"Выкл.";
        case PRESENCE_OPEN->"Открыть";
        case ECHO->c.neonEcho?"Вкл.":"Выкл.";case ECHO_PULSE->c.echoPulse?"Вкл.":"Выкл.";
        case ECHO_COLOR->PALETTES[c.echoPalette];case ECHO_GHOSTS->Integer.toString(c.echoGhosts);
        case ECHO_LENGTH->String.format(Locale.ROOT,"%.2f с",c.echoLength);case ECHO_INTENSITY->Math.round(c.echoIntensity*100)+"%";
        case RUST_GEOMETRY->c.rustGeometry?"Rust":"Java";
        case OPT_LIGHT->c.optimization?"Вкл.":"Выкл."; case OPT_PRESET->Optimizer.NAMES[c.optimizationPreset];
        case OPT_APPLY->"Применить"; case OPT_RESTORE->c.optimizationBackup==null?"Нет копии":"Восстановить";
        case MUSIC_OPEN->"Открыть"; case MUSIC_DISCONNECT->"Отключить";case MUSIC_VOLUME->c.musicVolume+"%";
        case COLOR_EDIT->"Открыть";
        case MENU_COLOR->PALETTES[c.menuPalette];
        case COLOR_HIT->c.colorHit?"Вкл.":"Выкл.";
        case COLOR_HIT_PALETTE->PALETTES[c.colorHitPalette];
        case MENU_SCALE->Math.round(c.menuScale*100)+"%";case MENU_CENTER->"По центру";
        case WATERMARK->c.watermark?"Вкл.":"Выкл."; case WATERMARK_COLOR->PALETTES[c.watermarkPalette];
        case EFFECT_STATUS->c.effectStatus?"Вкл.":"Выкл.";
        case HUD_POSITION,WATERMARK_POSITION,EFFECT_STATUS_POSITION->"Переместить";
        case HUD_SCALE,WATERMARK_SCALE,EFFECT_STATUS_SCALE->Math.round(s.value(c)*100)+"%";
        case DEATH->c.deathEffect?"Вкл.":"Выкл."; case DEATH_MODE->DeathEffectSystem.NAMES[c.deathMode];
        case TARGET->c.enabled?"Вкл.":"Выкл."; case HIT->c.hitEffect?"Вкл.":"Выкл.";
        case HIT_MODE->HitEffects.NAMES[c.hitMode];case HIT_DURATION->String.format(Locale.ROOT,"%.2f с",c.hitDuration);
        case MODE->Effects.NAMES[c.mode]; case COLOR->PALETTES[c.palette];
        case SHARDS->Integer.toString(c.shards); case SMOOTHING->Math.round(c.smoothing*1000)+" мс";
        case GLOW,OPACITY->Math.round(s.value(c)*100)+"%";
        case ANIMATIONS->c.menuAnimations?"Вкл.":"Выкл.";
        case RESET->System.nanoTime()<resetDeadline?"Подтвердить":"Сбросить";
        case RADIUS->String.format(Locale.ROOT,"%.2f м",c.radius);
        default->String.format(Locale.ROOT,"%.2f×",s.value(c));
    };}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        Config c=VisualFX.config;
        long now=System.nanoTime();double dt=Math.min(.1,(now-previous)/1e9);previous=now;
        progress = c.menuAnimations ? Math.clamp(progress + (closing ? -dt : dt) / TRANSITION_SECONDS, 0, 1) : (closing ? 0 : 1);
        visibility = progress * progress * (3 - 2 * progress);
        if(closing&&progress==0){minecraft.setScreen(null);return;}
        layout();
        int theme = MenuTheme.current();
        int accent = MenuTheme.accent(theme);
        int selectedSurface = MenuTheme.surface(0x141A22, theme, .23);
        int hoverSurface = MenuTheme.surface(0x141A22, theme, .15);
        int idleSurface = MenuTheme.surface(0x0C1119, theme, .07);
        g.fill(0,0,width,height,tint(MenuTheme.surface(0x020409,theme,.035),.62));
        int rawMx=mx,rawMy=my;
        mx=(int)(mx/uiScale);my=(int)(my/uiScale);
        g.pose().pushMatrix();
        if (progress < 1) {
            if (c.watermark && !minecraft.options.hideGui && minecraft.level != null) {
                watermarkAnchor.update(width, height, true);
                Watermark.drawLive(g, minecraft, watermarkAnchor, 1 - visibility);
                double sx = net.minecraft.util.Mth.lerp(visibility, watermarkAnchor.w / panelWidth, uiScale);
                double sy = net.minecraft.util.Mth.lerp(visibility, watermarkAnchor.h / panelHeight, uiScale);
                double x = net.minecraft.util.Mth.lerp(visibility, watermarkAnchor.x, left * uiScale);
                double y = net.minecraft.util.Mth.lerp(visibility, watermarkAnchor.y, top * uiScale);
                g.pose().translate((float) (x - left * sx), (float) (y - top * sy)).scale((float) sx, (float) sy);
            } else {
                g.pose().translate(0, (float) ((1 - visibility) * 12)).scale((float) uiScale, (float) uiScale);
            }
            mx = my = -1;
        } else {
            g.pose().scale((float) uiScale, (float) uiScale);
        }
        g.fill(left-1,top-1,left+panelWidth+1,top+panelHeight+1,tint(accent,.65));
        g.fill(left,top,left+panelWidth,top+panelHeight,tint(MenuTheme.surface(0x070B10,theme,.065),c.menuOpacity));
        g.fill(left,top,left+panelWidth,top+27,tint(MenuTheme.surface(0x101923,theme,.15),c.menuOpacity));
        fitted(g,"VisualFX / "+feature.title,left+10,top+9,accent,panelWidth-90);
        text(g,"[RSHIFT]",left+panelWidth-64,top+9,accent);
        int tabWidth=(panelWidth-16)/3;
        for(Category tab:Category.values()){
            int x=left+8+tab.ordinal()*tabWidth;
            boolean hover=mx>=x&&mx<x+tabWidth-3&&my>=top+32&&my<top+53;
            g.fill(x,top+32,x+tabWidth-3,top+53,tint(tab==category?selectedSurface:hover?hoverSurface:idleSurface,c.menuOpacity));
            if(tab==category)g.fill(x,top+52,x+tabWidth-3,top+54,tint(accent,1));
            text(g,tab.title,x+9,top+38,tab==category?accent:0x91A5AC);
        }
        int featureWidth=(panelWidth-16)/category.features.length;
        for(int i=0;i<category.features.length;i++){
            Feature item=category.features[i];int x=left+8+i*featureWidth;
            boolean hover=mx>=x&&mx<x+featureWidth-3&&my>=top+57&&my<top+78;
            g.fill(x,top+57,x+featureWidth-3,top+78,tint(item==feature?selectedSurface:hover?hoverSurface:idleSurface,c.menuOpacity));
            if(item==feature)g.fill(x,top+77,x+featureWidth-3,top+79,tint(accent,1));
            String tabTitle=item==Feature.EFFECT_STATUS&&font.width(item.title)>featureWidth-16?"Эффекты":item.title;
            fitted(g,tabTitle,x+9,top+63,item==feature?accent:0x91A5AC,featureWidth-16);
        }
        Setting hovered=null;
        for(int i=scrollOffset;i<Math.min(feature.settings.length,scrollOffset+visibleRows);i++){
            Setting s=feature.settings[i]; int y=top+88+(i-scrollOffset)*rowHeight;
            boolean hover=mx>=left+8&&mx<left+settingsWidth-8&&my>=y-3&&my<y+rowHeight-4;
            if(hover)hovered=s;
            g.fill(left+8,y-3,left+settingsWidth-8,y+rowHeight-4,tint(hover?hoverSurface:idleSurface,c.menuOpacity));
            if(hover||s==dragging)g.fill(left+8,y-3,left+10,y+rowHeight-4,tint(accent,1));
            fitted(g,s.label,left+18,y,0xD0DCE3,settingsWidth/2-26);
            ColorTarget target=colorTarget(s);
            if(target!=null){
                palette(g,left+settingsWidth/2,y-1,settingsWidth/2-16,target.palette(c),now/1e9,target.style(c));
            }
            else fitted(g,value(s,c),left+settingsWidth/2,y,accent,settingsWidth/2-16);
            if(s.slider()){
                int x=left+settingsWidth/2,end=left+settingsWidth-18;
                double f=(s.value(c)-s.min)/(s.max-s.min);
                g.fill(x,y+11,end,y+13,tint(MenuTheme.surface(0x25353F,theme,.12),1));
                g.fill(x,y+11,x+(int)((end-x)*f),y+13,tint(accent,1));
            }
        }
        if(settingsWidth<panelWidth)drawPreview(g,c,left+settingsWidth+2,top+85,150,panelHeight-126,now/1e9,accent);
        else {
            int used=Math.min(feature.settings.length,visibleRows);
            int y=top+88+used*rowHeight;
            drawPreview(g,c,left+10,y,panelWidth-20,top+panelHeight-37-y,now/1e9,accent);
        }
        String hint=feature==Feature.OPTIMIZATION&&!optimizationMessage.isEmpty()?optimizationMessage:hovered==null?"Перетащите верхнюю панель. Колесо мыши — прокрутка.":hovered.help;
        // Wrap the help text using the same font as the panel.
        var lines=font.split(Component.literal(hint).setStyle(FONT),panelWidth-24);
        int y=top+panelHeight-31;
        for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(font,lines.get(i),left+12,y+i*10,tint(0x829BA4,1),false);
        g.pose().popMatrix();
        if(hovered!=null)g.setTooltipForNextFrame(Component.literal(hovered.label+": "+value(hovered,c)),rawMx,rawMy);
    }
    private void slider(Setting s,double x){
        double f=Math.clamp((x-(left+settingsWidth/2))/(settingsWidth/2.-18),0,1);
        s.set(VisualFX.config,s.min+f*(s.max-s.min));
    }
    private void selectFeature(Feature selected){
        dragging=null;draggingWindow=false;scrollOffset=0;resetDeadline=0;
        lastAdjustment=0;
        lastFeature=feature=selected;VisualFX.config.save();
    }
    private void drawPreview(GuiGraphics g,Config c,int x,int y,int w,int h,double time,int accent){
        if(h<48)return;
        switch(feature){
            case SOUND_CLOUD->{
                text(g,"SoundCloud",x+6,y+8,accent);
                String info=SoundCloudPlayer.INSTANCE.connected()?"Браузер подключён. Откройте плеер для управления.":"Звук через браузер. Откройте плеер и вставьте ссылку SoundCloud.";
                var lines=font.split(Component.literal(info),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.drawString(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
            case TARGET_HUD->{if(h>=90)TargetHud.preview(g,font,x,y,w,c,time,visibility);}
            case EFFECT_STATUS->EffectStatus.previewInside(g,font,x+4,y+4,w-8,h-8,c.effectStatusScale);
            case WATERMARK->{
                text(g,"Watermark",x+6,y+8,accent);
                double scale=Math.min(1,Math.min((w-8)/(double)Watermark.WIDTH,(h-30)/(double)Watermark.HEIGHT));
                Watermark.preview(g,font,x+4,y+30,scale,c.watermarkPalette,time);
            }
            case OPTIMIZATION->{
                text(g,Optimizer.NAMES[c.optimizationPreset],x+6,y+8,accent);
                var lines=font.split(Component.literal(Optimizer.DETAILS[c.optimizationPreset]),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.drawString(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
            case TARGET_ESP,HIT_EFFECT,DEATH_EFFECT,NEON_ECHO,COLOR_HIT->preview.draw(g,font,x,y,w,h,c,time,visibility,feature==Feature.DEATH_EFFECT,feature==Feature.HIT_EFFECT,feature==Feature.NEON_ECHO,feature==Feature.COLOR_HIT);
            case ITEM_PHYSICS->fitted(g, "Анимация выпавших предметов", x+6,y+8,accent,w-12);
            case ITEM_SCROLLER->fitted(g, "Shift + колесо над стопкой", x+6,y+8,accent,w-12);
            case SEASONAL->fitted(g, SEASONS[c.seasonParticles], x+6,y+8,c.seasonParticles==2?0xE9A13D:0xDEF6FF,w-12);
            case PRESENCE->{
                fitted(g, Presence.status(), x+6,y+8,accent,w-12);
                var connection=minecraft.getConnection();
                if(connection!=null) {
                    int row=y+24;
                    for(var other:connection.getOnlinePlayers()) {
                        var activity=Presence.activity(other.getProfile().id());
                        if(activity==null)continue;
                        if(row+10>=y+h)break;
                        String name=font.plainSubstrByWidth(other.getProfile().name(),Math.max(0,w-22));
                        g.drawString(font,Presence.decorate(Component.literal(name),other.getProfile().id()),x+6,row,tint(0xCDEBD5,1),false);
                        row+=12;
                    }
                }
            }
            case MENU->{
                text(g,"Настройки меню",x+6,y+8,accent);
                var lines=font.split(Component.literal("Перетаскивайте окно за заголовок. Масштаб и палитра задаются слева. Right Shift закрывает меню."),w-12);
                for(int i=0;i<Math.min(lines.size(),(h-28)/11);i++)g.drawString(font,lines.get(i),x+6,y+27+i*11,tint(0xA4B6C3,1),false);
            }
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubled){
        if(progress<1||closing||event.button()!=InputConstants.MOUSE_BUTTON_LEFT)return false;
        double x=event.x()/uiScale,y=event.y()/uiScale;
        if(x>=left&&x<left+panelWidth&&y>=top&&y<top+27){
            dragging=null;draggingWindow=true;grabX=x-left;grabY=y-top;return true;
        }
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
            if(s==Setting.COLOR_EDIT){
                ColorTarget target=switch(feature){
                    case NEON_ECHO->ColorTarget.ECHO;
                    case TARGET_HUD->ColorTarget.HUD;
                    case WATERMARK->ColorTarget.WATERMARK;
                    case COLOR_HIT->ColorTarget.COLOR_HIT;
                    case MENU->ColorTarget.MENU;
                    default->ColorTarget.ESP;
                };
                VisualFX.config.save();minecraft.setScreen(new ColorEditorScreen(this,target));return true;
            }
            if(s==Setting.MUSIC_OPEN){VisualFX.config.save();minecraft.setScreen(new SoundCloudScreen(this));return true;}
            if(s==Setting.PRESENCE_OPEN){minecraft.setScreen(new PresenceScreen(this));return true;}
            if(s==Setting.HUD_POSITION||s==Setting.WATERMARK_POSITION||s==Setting.EFFECT_STATUS_POSITION){
                HudEditorScreen.Panel panel=s==Setting.EFFECT_STATUS_POSITION?HudEditorScreen.Panel.EFFECT_STATUS:
                    s==Setting.WATERMARK_POSITION?HudEditorScreen.Panel.WATERMARK:HudEditorScreen.Panel.TARGET_HUD;
                VisualFX.config.save();minecraft.setScreen(new HudEditorScreen(this,panel));return true;
            }


            if(s.slider()){dragging=s;slider(s,x);}
            else switch(s){
                case MUSIC_DISCONNECT->SoundCloudPlayer.INSTANCE.stop();
                case ITEM_PHYSICS->c.itemPhysics=!c.itemPhysics;
                case ITEM_SCROLLER->c.itemScroller=!c.itemScroller;
                case SEASON_MODE->c.seasonParticles=(c.seasonParticles+1)%SEASONS.length;
                case PRESENCE_ENABLED->c.presenceEnabled=!c.presenceEnabled;
                case PRESENCE_SHARE->c.presenceShareMenu=!c.presenceShareMenu;
                case MENU_CENTER->{c.menuX=.5;c.menuY=.5;}
                case RUST_GEOMETRY->{
                    if(c.rustGeometry){
                        c.rustGeometry=false;
                        optimizationMessage="Расчёт геометрии на Java.";
                    }else if(RustGeometry.available()){
                        c.rustGeometry=true;
                        optimizationMessage="Расчёт геометрии на Rust.";
                    }else{
                        optimizationMessage="Rust недоступен. Используется Java.";
                    }
                }
                case OPT_LIGHT->{c.optimization=!c.optimization;optimizationMessage="";}
                case OPT_PRESET->{c.optimizationPreset=(c.optimizationPreset+1)%3;optimizationMessage="";}
                case OPT_APPLY->optimizationMessage=Optimizer.apply();
                case OPT_RESTORE->optimizationMessage=Optimizer.restore();
                                case HUD->c.targetHud=!c.targetHud;
                case WATERMARK->c.watermark=!c.watermark;
                case EFFECT_STATUS->c.effectStatus=!c.effectStatus;
                case ECHO->c.neonEcho=!c.neonEcho;case ECHO_PULSE->c.echoPulse=!c.echoPulse;
                case COLOR,HUD_COLOR,WATERMARK_COLOR,ECHO_COLOR,COLOR_HIT_PALETTE,MENU_COLOR->{
                    int start=left+settingsWidth/2,swatchWidth=(settingsWidth/2-16)/7;
                    ColorTarget target=colorTarget(s);
                    int selected=x>=start?Math.clamp((int)((x-start)/Math.max(1,swatchWidth)),0,6):(target.palette(c)+1)%7;
                    target.apply(c,selected,target.style(c));
                }
                case DEATH->c.deathEffect=!c.deathEffect; case DEATH_MODE->c.deathMode=(c.deathMode+1)%2;
                case COLOR_HIT->c.colorHit=!c.colorHit;
                case TARGET->c.enabled=!c.enabled; case HIT->c.hitEffect=!c.hitEffect;
                case HIT_MODE->c.hitMode=(c.hitMode+1)%HitEffects.NAMES.length;
                case MODE->c.mode=(c.mode+1)%Effects.MODES.length;
                case ANIMATIONS->c.menuAnimations=!c.menuAnimations;
                case RESET->{if(System.nanoTime()<resetDeadline){Config defaults=new Config();defaults.optimizationBackup=c.optimizationBackup;defaults.cynthiaServerInitialized=c.cynthiaServerInitialized;VisualFX.config=defaults;resetDeadline=0;}else resetDeadline=System.nanoTime()+3_000_000_000L;}
                default->{}
            }
            lastAdjustment=System.nanoTime()+2_000_000_000L;
            VisualFX.config.save();return true;
        }
        return false;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        x/=uiScale;y/=uiScale;
        if(progress<1||closing||draggingWindow||dragging!=null||x<left||x>=left+settingsWidth||y<top+81||y>=top+panelHeight-35||vertical==0)return false;
        scrollOffset=Math.clamp(scrollOffset+(vertical<0?1:-1),0,Math.max(0,feature.settings.length-visibleRows));
        return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy){
        if(draggingWindow&&!closing){
            double travelX=Math.max(0,canvasWidth-panelWidth-12),travelY=Math.max(0,canvasHeight-panelHeight-12);
            Config c=VisualFX.config;
            c.menuX=travelX>0?Math.clamp((event.x()/uiScale-grabX-6)/travelX,0,1):.5;
            c.menuY=travelY>0?Math.clamp((event.y()/uiScale-grabY-6)/travelY,0,1):.5;
            return true;
        }
        if(dragging==null||closing)return false;
        slider(dragging,event.x()/uiScale);return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent event){
        if(event.button()==InputConstants.MOUSE_BUTTON_LEFT&&draggingWindow){draggingWindow=false;VisualFX.config.save();return true;}
        if(event.button()!=InputConstants.MOUSE_BUTTON_LEFT||dragging==null)return false;
        dragging=null;VisualFX.config.save();return true;
    }
}






