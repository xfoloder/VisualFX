package dev.visualfx;

import java.awt.Desktop;
import java.net.URI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

final class SoundCloudScreen extends Screen {
    private final Screen parent;
    private final SoundCloudPlayer player=SoundCloudPlayer.INSTANCE;
    private EditBox url;
    private Button toggle,prev,next,stop;
    private String message="Звук воспроизводится в открытой вкладке браузера.";
    private int left,top,panelWidth;
    private boolean seeking;
    SoundCloudScreen(Screen parent){super(Component.literal("VisualFX / SoundCloud"));this.parent=parent;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        panelWidth=Math.min(560,width-16);left=(width-panelWidth)/2;top=Math.max(4,(height-230)/2);
        String previous=url==null?VisualFX.config.soundCloudUrl:url.getValue();
        url=addRenderableWidget(new EditBox(font,left+10,top+30,panelWidth-20,20,Component.literal("Ссылка SoundCloud")));
        url.setMaxLength(2048);url.setValue(previous);
        int half=(panelWidth-24)/2;
        button("Открыть браузер",left+10,top+54,half,()->{
            try{
                String text=url.getValue().trim();if(!text.isEmpty())player.load(text);
                player.start();
                if(!Desktop.isDesktopSupported()||!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))throw new IllegalStateException();
                Desktop.getDesktop().browse(URI.create(player.localUrl()));
                message="Один раз нажмите Play в виджете браузера.";
            }catch(IllegalArgumentException e){message=e.getMessage();}
            catch(Exception e){message="Браузер не открылся. Нажмите «Копировать ссылку».";}
        });
        button("Загрузить трек / плейлист",left+14+half,top+54,half,()->{
            try{if(player.localUrl().isEmpty()){message="Сначала нажмите «Открыть браузер».";return;}player.load(url.getValue());message="Трек отправлен в SoundCloud. Дождитесь загрузки.";}
            catch(IllegalArgumentException e){message=e.getMessage();}
        });
        int cell=(panelWidth-20)/4;
        prev=button("Предыдущий",left+10,top+146,cell-3,()->player.command("prev","",0));
        toggle=button("Play / Pause",left+10+cell,top+146,cell-3,()->player.command("toggle","",0));
        next=button("Следующий",left+10+cell*2,top+146,cell-3,()->player.command("next","",0));
        stop=button("Стоп",left+10+cell*3,top+146,cell-3,()->player.command("stop","",0));
        addRenderableWidget(new AbstractSliderButton(left+10,top+172,panelWidth-20,20,Component.literal("Громкость: "+VisualFX.config.musicVolume+"%"),VisualFX.config.musicVolume/100.0){
            @Override protected void updateMessage(){setMessage(Component.literal("Громкость: "+(int)Math.round(value*100)+"%"));}
            @Override protected void applyValue(){VisualFX.config.musicVolume=(int)Math.round(value*100);player.command("volume","",VisualFX.config.musicVolume);}
        });
        int third=(panelWidth-20)/3;
        button("Назад",left+10,top+200,third-3,this::onClose);
        button("Копировать ссылку",left+10+third,top+200,third-3,()->{
            try{player.start();minecraft.keyboardHandler.setClipboard(player.localUrl());message="Локальная ссылка скопирована. Откройте её в браузере.";}
            catch(Exception e){message="Не удалось запустить локальный плеер.";}
        });
        button("Отключить",left+10+third*2,top+200,third-3,()->{player.stop();message="Плеер отключён. Звук в браузере остановится после потери связи.";});
        setInitialFocus(url);
    }
    private Button button(String title,int x,int y,int w,Runnable action){return addRenderableWidget(Button.builder(Component.literal(title),b->action.run()).bounds(x,y,w,20).build());}
    private void text(GuiGraphics g,String value,int x,int y,int color,int available){
        if(font.width(value)>available)value=font.plainSubstrByWidth(value,Math.max(0,available-8))+"…";
        g.drawString(font,value,x,y,color,false);
    }
    private static String time(long ms){long s=Math.max(0,ms/1000);return (s/60)+":"+String.format(java.util.Locale.ROOT,"%02d",s%60);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        int theme = MenuTheme.current();
        int accent = 0xFF000000 | MenuTheme.accent(theme);
        g.fill(0,0,width,height,0xB0000000 | MenuTheme.surface(0x020409,theme,.035));g.fill(left,top,left+panelWidth,top+230,0xF0000000 | MenuTheme.surface(0x070B10,theme,.10));g.renderOutline(left,top,panelWidth,230,accent);
        text(g,"VisualFX / SoundCloud",left+10,top+10,accent,panelWidth-20);
        var status=player.status();boolean connected=player.connected(),ready=connected&&status.ready();
        toggle.active=prev.active=next.active=stop.active=ready;
        toggle.setMessage(Component.literal(status.paused()?"Воспроизвести":"Пауза"));
        String line=!status.error().isBlank()?status.error():message;
        text(g,line,left+10,top+80,0xFF91A5AC,panelWidth-20);
        if(mx>=left+10&&mx<left+panelWidth-10&&my>=top+78&&my<top+91)g.setTooltipForNextFrame(Component.literal(line),mx,my);
        text(g,status.title().isBlank()?(connected?"Вкладка подключена — загрузите ссылку":"Браузер не подключён"):status.title(),left+10,top+95,0xFFE3EDF3,panelWidth-20);
        text(g,status.artist().isBlank()?"Источник: SoundCloud":status.artist()+" · SoundCloud",left+10,top+108,accent,panelWidth-20);
        int start=left+10,end=left+panelWidth-90;
        g.fill(start,top+127,end,top+137,0xFF000000 | MenuTheme.surface(0x20333E,theme,.22));
        double ratio=status.duration()>0?Math.clamp(status.position()/(double)status.duration(),0,1):0;
        g.fill(start,top+127,start+(int)((end-start)*ratio),top+137,accent);
        text(g,time(status.position())+" / "+time(status.duration()),end+5,top+128,0xFFA4B6C3,80);
        super.render(g,mx,my,delta);
    }
    private void seek(double x){var s=player.status();if(player.connected()&&s.ready()&&s.duration()>0)player.command("seek","",Math.clamp((x-left-10)/(panelWidth-100.0),0,1)*s.duration());}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubled){
        if(e.button()==InputConstants.MOUSE_BUTTON_LEFT&&e.x()>=left+10&&e.x()<=left+panelWidth-90&&e.y()>=top+124&&e.y()<=top+140){seeking=true;seek(e.x());return true;}
        return super.mouseClicked(e,doubled);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(seeking){seek(e.x());return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(seeking&&e.button()==InputConstants.MOUSE_BUTTON_LEFT){seeking=false;return true;}return super.mouseReleased(e);}
    @Override public void onClose(){VisualFX.config.save();minecraft.setScreen(parent);}
    @Override public void removed(){VisualFX.config.save();}
}

