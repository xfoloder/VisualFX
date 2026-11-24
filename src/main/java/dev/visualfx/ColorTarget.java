package dev.visualfx;

enum ColorTarget {
    ESP("Target ESP"),HUD("TargetHUD"),WATERMARK("Watermark"),ECHO("Neon Echo"), COLOR_HIT("Color Hit");
    final String title;
    ColorTarget(String title){this.title=title;}
    int palette(Config c){return switch(this){case ESP->c.palette;case HUD->c.hudPalette;case WATERMARK->c.watermarkPalette;case ECHO->c.echoPalette;case COLOR_HIT->c.colorHitPalette;};}
    ColorStyle style(Config c){return switch(this){case ESP->c.espStyle;case HUD->c.hudStyle;case WATERMARK->c.watermarkStyle;case ECHO->c.echoStyle;case COLOR_HIT->c.colorHitStyle;};}
    void apply(Config c,int palette,ColorStyle style){
        style.clamp();switch(this){
            case ESP->{c.palette=palette;c.espStyle=style.copy();}
            case HUD->{c.hudPalette=palette;c.hudStyle=style.copy();}
            case WATERMARK->{c.watermarkPalette=palette;c.watermarkStyle=style.copy();}
            case ECHO->{c.echoPalette=palette;c.echoStyle=style.copy();}
            case COLOR_HIT->{c.colorHitPalette=palette;c.colorHitStyle=style.copy();}
        }
    }
}

