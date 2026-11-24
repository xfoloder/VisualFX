package dev.visualfx;

import java.awt.Color;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

/** Draft-based RGB editor: changes reach live settings only after Apply. */
final class ColorEditorScreen extends Screen {
    private final Screen parent;
    private final ColorTarget target;
    private final ColorStyle draft;
    private int mode,endpoint;
    private boolean syncing;
    private EditBox hex;
    private SpectrumSlider spectrum;
    private final float[] hsb = new float[3];
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
            try{setRgb(ColorStyle.parseHex(value));spectrum.sync();message="Цвет обновлён в предпросмотре.";}
            catch(IllegalArgumentException e){message=e.getMessage();}
        });
        spectrum=addRenderableWidget(new SpectrumSlider(left+10,top+132,panelWidth-20));
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
            minecraft.gui.setScreen(parent);
        });
        syncWidgets();setInitialFocus(hex);
    }
    private Button button(String title,int x,int y,int w,int h,Runnable action){return addRenderableWidget(Button.builder(Component.literal(title),b->action.run()).bounds(x,y,w,h).build());}
    private int rgb(){return endpoint==0?draft.start:draft.end;}
    private void setRgb(int value){if(endpoint==0)draft.start=value;else draft.end=value;}
    private void syncWidgets(){
        syncing=true;hex.setValue(ColorStyle.hex(rgb()));syncing=false;spectrum.sync();
        startButton.active=endButton.active=speedSlider.active=mode==6;
        startButton.setMessage(Component.literal(endpoint==0?"[ Начало ]":"Начало"));
        endButton.setMessage(Component.literal(endpoint==1?"[ Конец ]":"Конец"));
        solidButton.setMessage(Component.literal(mode==5?"[ Один цвет ]":"Один цвет"));
        gradientButton.setMessage(Component.literal(mode==6?"[ Свой градиент ]":"Свой градиент"));
        speedSlider.setMessage(Component.literal(String.format(java.util.Locale.ROOT,"Скорость градиента: %.2f",draft.speed)));
        message="HEX: #RRGGBB. Применить — сохранить; Escape — отменить.";
    }
    private final class SpectrumSlider extends AbstractSliderButton {
        private static final Component LABEL = Component.literal("Оттенок");
        SpectrumSlider(int x, int y, int width) {
            super(x, y, width, 18, LABEL, 0);
            sync();
        }
        void sync() {
            int color = rgb();
            Color.RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, hsb);
            value = hsb[0];
        }
        @Override public void onClick(MouseButtonEvent event, boolean doubleClick) {
            super.onClick(event, doubleClick);
            applyValue();
        }
        @Override protected void updateMessage() {}
        @Override protected void applyValue() {
            setRgb(Color.HSBtoRGB((float) value, 1, 1) & 0xFFFFFF);
            syncing = true;
            hex.setValue(ColorStyle.hex(rgb()));
            syncing = false;
            ColorEditorScreen.this.message = "Нажмите «Применить», чтобы сохранить цвет.";
        }
        @Override public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY(), width = getWidth();
            g.fill(x, y, x + width, y + 18, 0xFF17232E);
            int length = width - 8;
            for (int i = 0; i < length; i += 2) {
                int color = Color.HSBtoRGB(i / (float) Math.max(1, length - 2), 1, 1);
                g.fill(x + 4 + i, y + 4, x + 4 + Math.min(length, i + 2), y + 14, color);
            }
            int knob = x + 4 + (int) Math.round(value * length);
            g.fill(knob - 3, y + 1, knob + 3, y + 17, 0xFF0A1016);
            g.outline(knob - 3, y + 1, 6, 16, 0xFFFFFFFF);
            if (isFocused() || isHovered()) g.outline(x, y, width, 18, 0xFF00F5FF);
        }
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float delta){}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xBA020409);g.fill(left,top,left+panelWidth,top+228,0xF8070B10);g.outline(left,top,panelWidth,228,0xFF00F5FF);
        g.text(font,"Редактор цветов / "+target.title,left+10,top+9,0xFFE3EDF3,false);
        int x=left+10,y=top+26,w=panelWidth-20;
        if(mode==6){for(int i=0;i<w;i+=3)g.fill(x+i,y,x+Math.min(w,i+3),y+30,0xFF000000|ColorStyle.mix(draft.start,draft.end,i/(double)Math.max(1,w-3)));}
        else g.fill(x,y,x+w,y+30,0xFF000000|draft.start);
        int animated=draft.sample(mode,System.nanoTime()/1e9*.45);
        g.fill(x+w-28,y+3,x+w-3,y+27,0xFF000000|animated);g.outline(x+w-29,y+2,27,26,0xFFFFFFFF);
        String hint=message;if(font.width(hint)>panelWidth-20)hint=font.plainSubstrByWidth(hint,panelWidth-30)+"…";
        g.text(font,hint,left+10,top+218,0xFF91A5AC,false);
        if(my>=top+216&&my<top+228)g.setTooltipForNextFrame(Component.literal(message),mx,my);
        super.extractRenderState(g,mx,my,delta);
    }
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
}
