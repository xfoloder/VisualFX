package dev.visualfx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

final class EffectStatus {
    static final int WIDTH = 188;
    private static final int HEADER = 23, ROW = 16, MAX_ROWS = 8;
    private static final int GOOD = 0xFF39FF14, BAD = 0xFFFF588B, NEUTRAL = 0xFF00F5FF;
    private record Entry(String name, String duration, int color, int order) {}
    private static final List<Entry> DEMO = List.of(
        new Entry("Скорость II", "2:45", GOOD, 1),
        new Entry("Сопротивление", "1:12", GOOD, 1),
        new Entry("Отравление", "0:08", BAD, 0));
    private List<Entry> entries = List.of();
    private final HudPlacement placement = new HudPlacement();

    void tick(Minecraft client) {
        if (!VisualFX.config.effectStatus || client.player == null || client.level == null || client.player.getActiveEffects().isEmpty()) {
            entries = List.of();
            return;
        }
        List<Entry> next = new ArrayList<>();
        float tickRate = client.level.tickRateManager().tickrate();
        for (MobEffectInstance effect : client.player.getActiveEffects()) {
            if (!effect.isInfiniteDuration() && effect.getDuration() <= 0) continue;
            var type = effect.getEffect().value();
            MobEffectCategory category = type.getCategory();
            boolean harmful = category == MobEffectCategory.HARMFUL;
            boolean beneficial = category == MobEffectCategory.BENEFICIAL;
            String level = switch (effect.getAmplifier()) {
                case 0 -> "";
                case 1 -> " II";
                case 2 -> " III";
                case 3 -> " IV";
                case 4 -> " V";
                default -> " " + (effect.getAmplifier() + 1L);
            };
            String duration = effect.isInfiniteDuration() ? "∞" : MobEffectUtil.formatDuration(effect, 1, tickRate).getString();
            next.add(new Entry(type.getDisplayName().getString() + level, duration,
                harmful ? BAD : beneficial ? GOOD : NEUTRAL, harmful ? 0 : beneficial ? 1 : 2));
        }
        next.sort(Comparator.comparingInt(Entry::order).thenComparing(Entry::name));
        entries = List.copyOf(next);
    }

    void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
        if (!VisualFX.config.effectStatus || entries.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.screen() != null) return;
        place(placement, graphics.guiWidth(), graphics.guiHeight(), panelHeight(entries.size()));
        drawScaled(graphics, client.font, placement.x, placement.y, placement.scale, entries);
    }

    static void placeDemo(HudPlacement placement, int width, int height) {
        place(placement, width, height, panelHeight(DEMO.size()));
    }

    private static void place(HudPlacement placement, int width, int height, int panelHeight) {
        Config config = VisualFX.config;
        placement.updateScreen(width, height, WIDTH, panelHeight, config.effectStatusScale, config.effectStatusX, config.effectStatusY);
        // Keep the top edge anchored to the editor's preview as the real list grows or shrinks.
        double previewTravel = Math.max(0, height - 8 - panelHeight(DEMO.size()) * placement.scale);
        placement.y = 4 + Math.min(placement.travelY, config.effectStatusY * previewTravel);
    }

    static void preview(GuiGraphicsExtractor graphics, Font font, double x, double y, double scale) {
        drawScaled(graphics, font, x, y, scale, DEMO);
    }

    static void previewInside(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height, double scale) {
        double fittedScale = Math.min(scale, Math.min(width / (double) WIDTH, height / (double) panelHeight(DEMO.size())));
        preview(graphics, font, x, y, fittedScale);
    }

    private static int panelHeight(int count) {
        return HEADER + ROW * Math.min(count, MAX_ROWS) + (count > MAX_ROWS ? ROW : 0) + 5;
    }

    private static void drawScaled(GuiGraphicsExtractor g, Font font, double x, double y, double scale, List<Entry> effects) {
        int height = panelHeight(effects.size());
        g.pose().pushMatrix();
        g.pose().translate((float)x, (float)y).scale((float)scale, (float)scale);
        g.fill(0, 0, WIDTH, height, 0xEE080F19);
        g.fill(0, 0, WIDTH, HEADER, 0xFF122432);
        g.outline(0, 0, WIDTH, height, 0xFF285466);
        g.text(font, "Effect Status", 9, 7, NEUTRAL, false);
        String total = Integer.toString(effects.size());
        g.text(font, total, WIDTH - 9 - font.width(total), 7, 0xFFA4B6C3, false);
        int visible = Math.min(effects.size(), MAX_ROWS);
        for (int i = 0; i < visible; i++) {
            Entry effect = effects.get(i);
            int row = HEADER + ROW * i;
            if ((i & 1) == 0) g.fill(4, row, WIDTH - 4, row + ROW, 0xFF101D2A);
            g.fill(8, row + 5, 11, row + 11, effect.color);
            int timeX = WIDTH - 9 - font.width(effect.duration);
            int available = timeX - 22;
            String name = font.width(effect.name) <= available ? effect.name
                : font.plainSubstrByWidth(effect.name, Math.max(0, available - font.width("…"))) + "…";
            g.text(font, name, 16, row + 4, 0xFFE3EDF3, false);
            g.text(font, effect.duration, timeX, row + 4, effect.color, false);
        }
        if (effects.size() > visible) g.text(font, "+" + (effects.size() - visible), 16, HEADER + visible * ROW + 4, 0xFFA4B6C3, false);
        g.pose().popMatrix();
    }
}
