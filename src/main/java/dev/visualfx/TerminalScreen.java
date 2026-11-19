package dev.visualfx;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;

public final class TerminalScreen extends Screen {
    private long previous=System.nanoTime();
    private double visibility;
    private boolean closing;
    private int dragging=-1;
    private int left,top,panelWidth,rowHeight=24,panelHeight=290;
    private static final String[] PALETTES={"ACID GREEN","NEON PINK","ELECTRIC CYAN","NEON PURPLE","SPECTRUM"};
    public TerminalScreen(){super(Text.literal("VisualFX terminal"));}
    @Override public boolean shouldPause(){return false;}
    @Override public void close(){closing=true; VisualFX.config.save();}
    @Override public void removed(){VisualFX.config.save();}
    @Override public void renderBackground(DrawContext c,int x,int y,float delta){}
    private int tint(int rgb,double opacity){return ((int)(Math.clamp(opacity*visibility,0,1)*255)<<24)|rgb;}
    private static final net.minecraft.text.Style TERMINAL_FONT=net.minecraft.text.Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(net.minecraft.util.Identifier.ofVanilla("uniform")));
    private void text(DrawContext c,String s,int x,int y,int rgb){c.drawText(textRenderer,Text.literal(s).setStyle(TERMINAL_FONT),x,y,tint(rgb,1),false);}
    @Override public void render(DrawContext d,int mx,int my,float delta){
        long now=System.nanoTime();double dt=Math.min(.1,(now-previous)/1e9);previous=now;
        visibility+=( (closing?0:1)-visibility)*(1-Math.exp(-dt*13));
        if(closing&&visibility<.015){client.setScreen(null);return;}
        rowHeight=Math.min(24,Math.max(16,(height-72)/9)); panelHeight=74+rowHeight*9; panelWidth=Math.min(500,width-16);left=(width-panelWidth)/2;top=Math.max(2,(height-panelHeight)/2)+(int)((1-visibility)*14);
        d.fill(0,0,width,height,tint(0x020409,.72));
        d.fill(left-1,top-1,left+panelWidth+1,top+panelHeight+1,tint(0x00F5FF,.6));
        d.fill(left,top,left+panelWidth,top+panelHeight,tint(0x070B10,.98));
        d.fill(left,top,left+panelWidth,top+28,tint(0x101923,1));
        text(d,"visualfx@neon:~$ ./control",left+12,top+10,0x39FF14);
        text(d,"[RSHIFT]",left+panelWidth-64,top+10,0xFF00CC);
        Config c=VisualFX.config;
        String[] labels={"TARGET ESP", "RENDER MODE", "CHROMA", "ROTATION", "ORBIT RADIUS", "GLOW", "SHARDS", "SMOOTHING", "HIT EFFECT"};
        String[] values={c.enabled?"ONLINE":"OFFLINE",Effects.NAMES[c.mode],PALETTES[c.palette],String.format(java.util.Locale.ROOT,"%.2fx",c.speed),String.format(java.util.Locale.ROOT,"%.2f",c.radius),Math.round(c.glow*100)+"%",""+c.shards,Math.round(c.smoothing*1000)+" ms",c.hitEffect?"ARMED":"OFF"};
        for(int i=0;i<9;i++){
            int y=top+38+i*rowHeight;boolean hover=mx>=left+8&&mx<left+panelWidth-8&&my>=y-3&&my<y+rowHeight-5;
            d.fill(left+8,y-3,left+panelWidth-8,y+rowHeight-5,tint(hover?0x172A34:0x0C141C,1));
            if(hover)d.fill(left+8,y-3,left+10,y+rowHeight-5,tint(0x39FF14,1));
            text(d,labels[i],left+18,y,0x91A5AC);
            text(d,values[i],left+panelWidth/2,y,i==2?c.color(0):0x00F5FF);
            if(i>=3&&i<=7){double f=switch(i){case 3->(c.speed-.1)/2.9;case 4->(c.radius-.4)/1.2;case 5->c.glow;case 6->c.shards/48.;default->(c.smoothing-.05)/.65;};
                int x=left+panelWidth/2; d.fill(x,y+12,left+panelWidth-18,y+14,tint(0x25353F,1));d.fill(x,y+12,x+(int)((panelWidth/2-18)*f),y+14,tint(0xB026FF,1));}
        }
        text(d,"config: autosave  |  crosshair target only",left+12,top+45+9*rowHeight,0x657E87);
        for(int y=top+30;y<top+35+9*rowHeight;y+=3)d.fill(left,y,left+panelWidth,y+1,tint(0x000000,.08));
    }
    private void slider(int row,double x){Config c=VisualFX.config;double f=Math.clamp((x-(left+panelWidth/2))/(panelWidth/2.-18),0,1);
        switch(row){case 3->c.speed=.1+f*2.9;case 4->c.radius=.4+f*1.2;case 5->c.glow=f;case 6->c.shards=(int)Math.round(f*48);case 7->c.smoothing=.05+f*.65;} }
    @Override public boolean mouseClicked(Click click,boolean doubled){
        if(closing||click.button()!=0)return false;
        int row=(int)((click.y()-top-35)/rowHeight);
        if(click.x()<left+8||click.x()>left+panelWidth-8||click.y()<top+35||row>8)return false;
        Config c=VisualFX.config;
        switch(row){case 0->c.enabled=!c.enabled;case 1->c.mode=(c.mode+1)%4;case 2->c.palette=(c.palette+1)%5;case 8->c.hitEffect=!c.hitEffect;default->{dragging=row;slider(row,click.x());}}
        c.save();return true;
    }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(dragging>=0){slider(dragging,click.x());return true;}return false;}
    @Override public boolean mouseReleased(Click click){dragging=-1;VisualFX.config.save();return true;}
}

