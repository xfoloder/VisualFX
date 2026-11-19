package dev.visualfx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public final class Config {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualfx.json");
    public boolean enabled = true, hitEffect = true;
    public int hitMode=0;
    public double hitDuration=.6;
    public int mode = 0, palette = 0, shards = 18;
    public double speed = 1, radius = .85, glow = .7, smoothing = .22;
    public boolean menuAnimations = true;
    public double menuScale=1,menuX=.5,menuY=.5;
    public double menuOpacity = .85, hitStrength = 1;
    public boolean deathEffect = true;
    public int deathMode = 0;
    public boolean targetHud = true;
    public int hudPalette = 2;
    public double hudScale=1,hudX=.65,hudY=.6;
    public double hudOffsetX=0,hudOffsetY=-12;
    public boolean watermark=true;
    public boolean effectStatus=true;
    public double effectStatusScale=1,effectStatusX=1,effectStatusY=.22;
    public int watermarkPalette=0;
    public double watermarkScale=1,watermarkX=0,watermarkY=0;
    public boolean optimization=false;
    public int optimizationPreset=0;
    public Optimizer.Snapshot optimizationBackup;
    public String soundCloudUrl="";
    public int musicVolume=60;
    public ColorStyle espStyle=new ColorStyle(0x39FF14,0xB026FF);
    public ColorStyle hudStyle=new ColorStyle(0x00F5FF,0xFF00CC);
    public ColorStyle watermarkStyle=new ColorStyle(0x39FF14,0x00F5FF);
    public boolean neonEcho=true,echoPulse=true;
    public double echoLength=.9,echoIntensity=.7;
    public int echoGhosts=5,echoPalette=6;
    public ColorStyle echoStyle=new ColorStyle(0x39FF14,0x00F5FF);
    public static final int[] COLORS = ColorStyle.PRESETS;
    public void clamp() {
        mode = Math.floorMod(mode,Effects.MODES.length); palette = Math.floorMod(palette,7);
        hitMode=Math.floorMod(hitMode,HitEffects.NAMES.length);hitDuration=limit(hitDuration,.25,1.2);
        shards = Math.clamp(shards,0,48);
        speed = limit(speed,.1,3); radius = limit(radius,.4,1.6);
        glow = limit(glow,0,1); smoothing = limit(smoothing,.05,.7);
        menuOpacity = limit(menuOpacity,.25,1); hitStrength = limit(hitStrength,.1,2);
        menuScale=limit(menuScale,.65,1.5);menuX=limit(menuX,0,1);menuY=limit(menuY,0,1);
        deathMode = Math.floorMod(deathMode,2);
        hudPalette = Math.floorMod(hudPalette,7);
        hudScale=limit(hudScale,.5,2);hudX=limit(hudX,0,1);hudY=limit(hudY,0,1);
        effectStatusScale=limit(effectStatusScale,.5,2);
        effectStatusX=limit(effectStatusX,0,1);effectStatusY=limit(effectStatusY,0,1);
        hudOffsetX=limit(hudOffsetX,-2048,2048);hudOffsetY=limit(hudOffsetY,-2048,2048);
        watermarkPalette=Math.floorMod(watermarkPalette,7);
        watermarkScale=limit(watermarkScale,.5,2);watermarkX=limit(watermarkX,0,1);watermarkY=limit(watermarkY,0,1);
        optimizationPreset=Math.floorMod(optimizationPreset,3);
        musicVolume=Math.clamp(musicVolume,0,100);
        if(soundCloudUrl==null||soundCloudUrl.length()>2048)soundCloudUrl="";
        if(espStyle==null)espStyle=new ColorStyle(0x39FF14,0xB026FF);
        if(hudStyle==null)hudStyle=new ColorStyle(0x00F5FF,0xFF00CC);
        if(watermarkStyle==null)watermarkStyle=new ColorStyle(0x39FF14,0x00F5FF);
        espStyle.clamp();hudStyle.clamp();watermarkStyle.clamp();
        echoLength=limit(echoLength,.25,1.5);echoIntensity=limit(echoIntensity,.2,1);
        echoGhosts=Math.clamp(echoGhosts,2,8);echoPalette=Math.floorMod(echoPalette,7);
        if(echoStyle==null)echoStyle=new ColorStyle(0x39FF14,0x00F5FF);
        echoStyle.clamp();
    }
    private static double limit(double n,double a,double b) { return Double.isFinite(n)?Math.clamp(n,a,b):a; }
    public static Config load() {
        try { if (Files.exists(FILE)) { Config c = JSON.fromJson(Files.readString(FILE),Config.class); if(c!=null) { c.clamp(); return c; } } }
        catch(Exception e) { VisualFX.LOG.warn("Cannot read VisualFX configuration", e); }
        return new Config();
    }
    public void save() {
        saveChecked();
    }
    public boolean saveChecked() {
        clamp();
        try { Files.createDirectories(FILE.getParent()); Path tmp=FILE.resolveSibling("visualfx.json.tmp"); Files.writeString(tmp,JSON.toJson(this)); Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING);return true; }
        catch(Exception e) { VisualFX.LOG.warn("Cannot save VisualFX configuration", e);return false; }
    }
    public int color(double phase) {
        return espStyle.sample(palette,phase);
    }
    public int effectShards(){return optimization?Math.min(shards,switch(optimizationPreset){case 0->24;case 1->12;default->6;}):shards;}
    public int detailSegments(int original){return optimization?Math.max(12,original/(optimizationPreset+2)):original;}
    public int glowPasses(){return optimization?(optimizationPreset==0?2:1):3;}
    public static int paletteColor(int palette,double phase) {
        return ColorStyle.preset(palette,phase);
    }
}




