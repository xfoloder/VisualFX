package dev.visualfx;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Draft-based RGB editor: changes reach live settings only after Apply. */
final class ColorEditorScreen extends Screen {
    private final Screen parent;
    private final ColorTarget target;
    private final ColorStyle draft;
    private int mode,endpoint;
    private boolean syncing;
    private EditBox hex;
    private RgbSlider[] channels;
    private Button startButton,endButton,solidButton,gradientButton;
    private AbstractSliderButton speedSlider;
    private int left,top,panelWidth;
    private String message="HEX: #RRGGBB. Применить — сохранить; Escape — отменить.";
    ColorEditorScreen(Screen parent,ColorTarget target){
        super(Component.literal("Редактор цветов"));this.parent=parent;this.target=target;
        Config c=VisualFX.config;draft=target.style(c).copy();int palette=target.palette(c);
        mode=palette==4||palette==6?6:5;
        if(palette<4)draft.start=ColorStyle.PRESETS[palette];
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){
        panelWidth=Math.min(500,width-16);left=(width-panelWidth)/2;top=Math.max(4,(height-228)/2);
        int half=(panelWidth-24)/2;
        solidButton=button("Один цвет",left+10,top+60,half,20,()->{mode=5;endpoint=0;syncWidgets();});
        gradientButton=button("Свой градиент",left+14+half,top+60,half,20,()->{mode=6;syncWidgets();});
        startButton=button("Начало",left+10,top+84,half,20,()->{endpoint=0;syncWidgets();});
        endButton=button("Конец",left+14+half,top+84,half,20,()->{endpoint=1;syncWidgets();});
        hex=addRenderableWidget(new EditBox(font,left+10,top+108,panelWidth-20,20,Component.literal("HEX-код цвета")));
        hex.setMaxLength(7);hex.setResponder(value->{
            if(syncing)return;
            try{setRgb(ColorStyle.parseHex(value));syncSliders();message="Цвет обновлён в предпросмотре.";}
            catch(IllegalArgumentException e){message=e.getMessage();}
        });
        channels=new RgbSlider[3];int cell=(panelWidth-20)/3;
        for(int i=0;i<3;i++)channels[i]=addRenderableWidget(new RgbSlider(left+10+i*cell,top+132,cell-3,i));
        speedSlider=addRenderableWidget(new AbstractSliderButton(left+10,top+154,panelWidth-20,18,Component.literal("Скорость"),(draft.speed-.1)/1.9){
            @Override protected void updateMessage(){setMessage(Component.literal(String.format(java.util.Locale.ROOT,"Скорость градиента: %.2f",.1+value*1.9)));}
            @Override protected void applyValue(){draft.speed=.1+value*1.9;}
        });
        int[] colors={0x39FF14,0xFF00CC,0x00F5FF,0xB026FF,0xFFFFFF,0};
        String[] names={"Зелёный","Розовый","Голубой","Фиолет.","Белый","Чёрный"};
        int presetWidth=(panelWidth-20)/6;
        for(int i=0;i<colors.length;i++){final int color=colors[i];button(names[i],left+10+i*presetWidth,top+176,presetWidth-2,16,()->{setRgb(color);syncWidgets();});}
        button("Отмена",left+10,top+196,half,20,this::onClose);
        button("Применить",left+14+half,top+196,half,20,()->{
            try{setRgb(ColorStyle.parseHex(hex.getValue()));}
            catch(IllegalArgumentException e){message=e.getMessage();return;}
            Config c=VisualFX.config;int beforePalette=target.palette(c);ColorStyle before=target.style(c).copy();
            target.apply(c,mode,draft);
            if(!c.saveChecked()){target.apply(c,beforePalette,before);message="Не удалось сохранить конфигурацию. Попробуйте ещё раз.";return;}
            minecraft.setScreen(parent);
        });
        syncWidgets();setInitialFocus(hex);
    }
    private Button button(String title,int x,int y,int w,int h,Runnable action){return addRenderableWidget(Button.builder(Component.literal(title),b->action.run()).bounds(x,y,w,h).build());}
    private int rgb(){return endpoint==0?draft.start:draft.end;}
    private void setRgb(int value){if(endpoint==0)draft.start=value;else draft.end=value;}
    private void syncSliders(){if(channels!=null)for(RgbSlider slider:channels)slider.sync();}
    private void syncWidgets(){
        syncing=true;hex.setValue(ColorStyle.hex(rgb()));syncing=false;syncSliders();
        startButton.active=endButton.active=speedSlider.active=mode==6;
        startButton.setMessage(Component.literal(endpoint==0?"[ Начало ]":"Начало"));
        endButton.setMessage(Component.literal(endpoint==1?"[ Конец ]":"Конец"));
        solidButton.setMessage(Component.literal(mode==5?"[ Один цвет ]":"Один цвет"));
        gradientButton.setMessage(Component.literal(mode==6?"[ Свой градиент ]":"Свой градиент"));
        speedSlider.setMessage(Component.literal(String.format(java.util.Locale.ROOT,"Скорость градиента: %.2f",draft.speed)));
        message="HEX: #RRGGBB. Применить — сохранить; Escape — отменить.";
    }
    private final class RgbSlider extends AbstractSliderButton {
        private final int channel;
        RgbSlider(int x,int y,int w,int channel){super(x,y,w,18,Component.empty(),0);this.channel=channel;sync();}
        void sync(){value=((rgb()>>(16-channel*8))&255)/255.0;updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal(new String[]{"R","G","B"}[channel]+": "+(int)Math.round(value*255)));}
        @Override protected void applyValue(){int shift=16-channel*8;setRgb((rgb()&~(255<<shift))|((int)Math.round(value*255)<<shift));syncWidgets();}
    }
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xBA020409);g.fill(left,top,left+panelWidth,top+228,0xF8070B10);g.renderOutline(left,top,panelWidth,228,0xFF00F5FF);
        g.drawString(font,"Редактор цветов / "+target.title,left+10,top+9,0xFFE3EDF3,false);
        int x=left+10,y=top+26,w=panelWidth-20;
        if(mode==6){for(int i=0;i<w;i+=3)g.fill(x+i,y,x+Math.min(w,i+3),y+30,0xFF000000|ColorStyle.mix(draft.start,draft.end,i/(double)Math.max(1,w-3)));}
        else g.fill(x,y,x+w,y+30,0xFF000000|draft.start);
        int animated=draft.sample(mode,System.nanoTime()/1e9*.45);
        g.fill(x+w-28,y+3,x+w-3,y+27,0xFF000000|animated);g.renderOutline(x+w-29,y+2,27,26,0xFFFFFFFF);
        String hint=message;if(font.width(hint)>panelWidth-20)hint=font.plainSubstrByWidth(hint,panelWidth-30)+"…";
        g.drawString(font,hint,left+10,top+218,0xFF91A5AC,false);
        if(my>=top+216&&my<top+228)g.setTooltipForNextFrame(Component.literal(message),mx,my);
        super.render(g,mx,my,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
}

