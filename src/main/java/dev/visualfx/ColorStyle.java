package dev.visualfx;

/** RGB-only model shared by effects, HUD, watermark and the editor. */
public final class ColorStyle {
    public static final int[] PRESETS={0x39FF14,0xFF00CC,0x00F5FF,0xB026FF};
    public int start=0x39FF14,end=0xB026FF;
    public double speed=.35;
    public ColorStyle(){}
    public ColorStyle(int start,int end){this.start=start;this.end=end;}
    public ColorStyle copy(){ColorStyle c=new ColorStyle(start,end);c.speed=speed;return c;}
    public void clamp(){start&=0xFFFFFF;end&=0xFFFFFF;speed=Double.isFinite(speed)?Math.clamp(speed,.1,2):.35;}
    public int sample(int palette,double phase){
        int mode=Math.floorMod(palette,7);
        if(mode==5)return start&0xFFFFFF;
        if(mode==6)return mix(start,end,.5-.5*Math.cos(phase*speed*Math.PI*2));
        return preset(mode,phase);
    }
    public static int preset(int palette,double phase){
        if(palette>=0&&palette<4)return PRESETS[palette];
        double x=((phase%4)+4)%4;
        if(!Double.isFinite(x))x=0;
        return mix(PRESETS[(int)x],PRESETS[((int)x+1)%4],x-(int)x);
    }
    public static int mix(int a,int b,double ratio){
        double f=Double.isFinite(ratio)?Math.clamp(ratio,0,1):0;
        return ((int)Math.round(((a>>16)&255)*(1-f)+((b>>16)&255)*f)<<16)
            |((int)Math.round(((a>>8)&255)*(1-f)+((b>>8)&255)*f)<<8)
            |(int)Math.round((a&255)*(1-f)+(b&255)*f);
    }
    public static int parseHex(String input){
        String s=input.trim();if(s.startsWith("#"))s=s.substring(1);
        if(!s.matches("[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Введите 6 HEX-символов: например #39FF14");
        return Integer.parseInt(s,16);
    }
    public static String hex(int rgb){return String.format(java.util.Locale.ROOT,"#%06X",rgb&0xFFFFFF);}
}
