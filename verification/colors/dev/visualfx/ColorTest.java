package dev.visualfx;
import com.google.gson.Gson;
public final class ColorTest {
 public static void main(String[] args){
  assert ColorStyle.parseHex("#39FF14")==0x39FF14;
  assert ColorStyle.parseHex("abcdef")==0xABCDEF;
  assert ColorStyle.parseHex(" #000000 ")==0;
  for(String s:new String[]{"#FFF","#12345678","12XX34","#","","GG0000","１２３４５６"}){
   try{ColorStyle.parseHex(s);throw new AssertionError("Invalid HEX accepted");}catch(IllegalArgumentException expected){}
  }
  assert ColorStyle.hex(0).equals("#000000");assert ColorStyle.hex(0x123).equals("#000123");
  assert ColorStyle.mix(0,0xFFFFFF,0)==0;assert ColorStyle.mix(0,0xFFFFFF,1)==0xFFFFFF;assert ColorStyle.mix(0,0xFFFFFF,.5)==0x808080;
  ColorStyle style=new ColorStyle(0xFF0000,0x0000FF);style.speed=.5;
  assert style.sample(5,0)==0xFF0000;assert style.sample(5,900)==0xFF0000;
  assert style.sample(6,0)==0xFF0000;assert style.sample(6,1)==0x0000FF;assert style.sample(6,2)==0xFF0000;
  int samples=0;for(int palette=0;palette<7;palette++)for(int step=-100;step<=100;step++){
   int c=style.sample(palette,step*.037);assert c>=0&&c<=0xFFFFFF;samples++;
  }
  style.speed=Double.NaN;style.start=-1;style.end=0xFF123456;style.clamp();assert style.speed==.35;assert style.start==0xFFFFFF;assert style.end==0x123456;
  Gson gson=new Gson();ColorStyle restored=gson.fromJson(gson.toJson(style),ColorStyle.class);assert restored.start==style.start&&restored.end==style.end&&restored.speed==style.speed;
  Config config=new Config();int originalEsp=config.espStyle.start;ColorStyle draft=new ColorStyle(0x123456,0xABCDEF);
  ColorTarget.HUD.apply(config,6,draft);draft.start=0;assert config.hudStyle.start==0x123456;assert config.espStyle.start==originalEsp;assert config.watermarkPalette==0;
  ColorTarget.ESP.apply(config,5,draft);assert config.hudPalette==6&&config.hudStyle.start==0x123456;
  ColorTarget.ECHO.apply(config,6,new ColorStyle(0x113355,0xFFCC00));assert config.echoStyle.start==0x113355&&config.hudStyle.start==0x123456;
  ColorTarget.COLOR_HIT.apply(config,5,new ColorStyle(0x00F5FF,0x39FF14));
  assert config.colorHitStyle.start==0x00F5FF&&config.echoStyle.start==0x113355;
  ColorTarget.MENU.apply(config,5,new ColorStyle(0xB026FF,0x00F5FF));
  assert config.menuStyle.start==0xB026FF&&config.colorHitStyle.start==0x00F5FF;
  ColorStyle legacy=gson.fromJson("{}",ColorStyle.class);assert legacy.start==0x39FF14&&legacy.speed==.35;
  System.out.println("PASS: HEX validation, RGB formatting, blend endpoints, "+samples+" palette samples, gradient cycle, invalid-value normalization, JSON round trip, independent targets and draft copy");
 }
}

