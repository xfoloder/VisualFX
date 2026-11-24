package dev.visualfx;

final class MenuTheme {
    private MenuTheme() {}

    static int current() {
        Config config = VisualFX.config;
        return config.menuStyle.sample(config.menuPalette, System.nanoTime() / 1e9 * .45);
    }

    static int accent(int color) {
        return ColorStyle.mix(color, 0xFFFFFF, .58);
    }

    static int surface(int base, int color, double strength) {
        return ColorStyle.mix(base, color, strength);
    }
}
