package dev.visualfx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public final class Config {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("visualfx.json");
    public boolean enabled = true, hitEffect = true;
    public int mode = 0, palette = 0, shards = 18;
    public double speed = 1, radius = .85, glow = .7, smoothing = .22;
    public boolean menuAnimations = true;
    public double menuOpacity = .85, hitStrength = 1;
    public boolean deathEffect = true;
    public int deathMode = 0;
    public boolean targetHud = true;
    public int hudPalette = 2;
    public double hudScale=1,hudX=.65,hudY=.6;
    public double hudOffsetX=0,hudOffsetY=-12;
    public boolean watermark=true;
    public int watermarkPalette=0;
    public double watermarkScale=1,watermarkX=0,watermarkY=0;
    public boolean optimization=false;
    public int optimizationPreset=0;
    public Optimizer.Snapshot optimizationBackup;
    public String soundCloudUrl="";
    public int musicVolume=60;
    public static final int[] COLORS = {0x39FF14, 0xFF00CC, 0x00F5FF, 0xB026FF};
    public void clamp() {
        mode = Math.floorMod(mode,Effects.MODES.length); palette = Math.floorMod(palette,5);
        shards = Math.clamp(shards,0,48);
        speed = limit(speed,.1,3); radius = limit(radius,.4,1.6);
        glow = limit(glow,0,1); smoothing = limit(smoothing,.05,.7);
        menuOpacity = limit(menuOpacity,.25,1); hitStrength = limit(hitStrength,.1,2);
        deathMode = Math.floorMod(deathMode,2);
        hudPalette = Math.floorMod(hudPalette,5);
        hudScale=limit(hudScale,.5,2);hudX=limit(hudX,0,1);hudY=limit(hudY,0,1);
        hudOffsetX=limit(hudOffsetX,-2048,2048);hudOffsetY=limit(hudOffsetY,-2048,2048);
        watermarkPalette=Math.floorMod(watermarkPalette,5);
        watermarkScale=limit(watermarkScale,.5,2);watermarkX=limit(watermarkX,0,1);watermarkY=limit(watermarkY,0,1);
        optimizationPreset=Math.floorMod(optimizationPreset,3);
        musicVolume=Math.clamp(musicVolume,0,100);
        if(soundCloudUrl==null||soundCloudUrl.length()>2048)soundCloudUrl="";
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
        return paletteColor(palette,phase);
    }
    public int effectShards(){return optimization?Math.min(shards,switch(optimizationPreset){case 0->24;case 1->12;default->6;}):shards;}
    public int detailSegments(int original){return optimization?Math.max(12,original/(optimizationPreset+2)):original;}
    public int glowPasses(){return optimization?(optimizationPreset==0?2:1):3;}
    public static int paletteColor(int palette,double phase) {
        if(palette<4) return COLORS[palette];
        double x=((phase%4)+4)%4; int a=COLORS[(int)x],b=COLORS[((int)x+1)%4]; double f=x-(int)x;
        return ((int)(((a>>16)&255)*(1-f)+((b>>16)&255)*f)<<16)|((int)(((a>>8)&255)*(1-f)+((b>>8)&255)*f)<<8)|(int)((a&255)*(1-f)+(b&255)*f);
    }
}





