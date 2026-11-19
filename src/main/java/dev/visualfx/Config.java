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
    public static final int[] COLORS = {0x39FF14, 0xFF00CC, 0x00F5FF, 0xB026FF};
    public void clamp() {
        mode = Math.floorMod(mode,Effects.MODES.length); palette = Math.floorMod(palette,5);
        shards = Math.clamp(shards,0,48);
        speed = limit(speed,.1,3); radius = limit(radius,.4,1.6);
        glow = limit(glow,0,1); smoothing = limit(smoothing,.05,.7);
        menuOpacity = limit(menuOpacity,.25,1); hitStrength = limit(hitStrength,.1,2);
    }
    private static double limit(double n,double a,double b) { return Double.isFinite(n)?Math.clamp(n,a,b):a; }
    public static Config load() {
        try { if (Files.exists(FILE)) { Config c = JSON.fromJson(Files.readString(FILE),Config.class); if(c!=null) { c.clamp(); return c; } } }
        catch(Exception e) { VisualFX.LOG.warn("Cannot read VisualFX configuration", e); }
        return new Config();
    }
    public void save() {
        clamp();
        try { Files.createDirectories(FILE.getParent()); Path tmp=FILE.resolveSibling("visualfx.json.tmp"); Files.writeString(tmp,JSON.toJson(this)); Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING); }
        catch(Exception e) { VisualFX.LOG.warn("Cannot save VisualFX configuration", e); }
    }
    public int color(double phase) {
        if(palette<4) return COLORS[palette];
        double x=((phase%4)+4)%4; int a=COLORS[(int)x],b=COLORS[((int)x+1)%4]; double f=x-(int)x;
        return ((int)(((a>>16)&255)*(1-f)+((b>>16)&255)*f)<<16)|((int)(((a>>8)&255)*(1-f)+((b>>8)&255)*f)<<8)|(int)((a&255)*(1-f)+(b&255)*f);
    }
}



