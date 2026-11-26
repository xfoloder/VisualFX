package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Color;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Edits a copy so a palette change can be previewed and cancelled. */
final class ColorEditorScreen extends Screen {
    private static final int[] PRESETS = {0x39FF14, 0xFF00CC, 0x00F5FF, 0xB026FF, 0xFFFFFF, 0};
    private static final String[] PRESET_NAMES = {"Зелёный", "Розовый", "Голубой", "Фиолетовый", "Белый", "Чёрный"};
    private static final String DEFAULT_HINT = "Выберите цвет и нажмите «Применить».";

    private final Screen parent;
    private final ColorTarget target;
    private final ColorStyle draft;
    private final float[] hsb = new float[3];
    private int mode, endpoint;
    private boolean syncing;
    private EditBox hex;
    private ColorSlider spectrum, brightness;
    private Button startButton, endButton, solidButton, gradientButton;
    private AbstractSliderButton speedSlider;
    private int left, top, panelWidth, panelHeight, pickerSize, controlsX, controlsWidth;
    private int frameAccent;
    private String message = DEFAULT_HINT;

    ColorEditorScreen(Screen parent, ColorTarget target) {
        super(Component.literal("Редактор цветов"));
        this.parent = parent;
        this.target = target;
        Config config = VisualFX.config;
        draft = target.style(config).copy();
        int palette = target.palette(config);
        mode = palette == 4 || palette == 6 ? 6 : 5;
        if (palette < 4) draft.start = ColorStyle.PRESETS[palette];
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override protected void init() {
        panelWidth = Math.min(418, width - 12);
        pickerSize = Math.clamp(Math.min(panelWidth / 3, height - 111), 32, 132);
        panelHeight = Math.max(191, pickerSize + 101);
        left = (width - panelWidth) / 2;
        top = Math.max(3, (height - panelHeight) / 2);
        controlsX = left + 23 + pickerSize;
        controlsWidth = left + panelWidth - 11 - controlsX;
        int pickerX = left + 11, pickerY = top + 27;
        int half = (controlsWidth - 4) / 2;

        addRenderableWidget(new ColorSquare(pickerX, pickerY, pickerSize));
        spectrum = addRenderableWidget(new ColorSlider(pickerX, pickerY + pickerSize + 6, pickerSize, true));
        brightness = addRenderableWidget(new ColorSlider(pickerX, pickerY + pickerSize + 23, pickerSize, false));

        hex = addRenderableWidget(new EditBox(font, controlsX, top + 47, controlsWidth, 18, Component.literal("HEX-код цвета")));
        hex.setMaxLength(7);
        hex.setResponder(value -> {
            if (syncing) return;
            try {
                setRgb(ColorStyle.parseHex(value));
                syncPicker();
                message = DEFAULT_HINT;
            } catch (IllegalArgumentException error) {
                message = error.getMessage();
            }
        });
        solidButton = button("Один цвет", controlsX, top + 69, half, 18, () -> { mode = 5; endpoint = 0; syncWidgets(); });
        gradientButton = button("Градиент", controlsX + half + 4, top + 69, half, 18, () -> { mode = 6; syncWidgets(); });
        startButton = button("Начало", controlsX, top + 91, half, 18, () -> { endpoint = 0; syncWidgets(); });
        endButton = button("Конец", controlsX + half + 4, top + 91, half, 18, () -> { endpoint = 1; syncWidgets(); });
        speedSlider = addRenderableWidget(new AbstractSliderButton(controlsX, top + 113, controlsWidth, 18,
            Component.literal("Скорость"), (draft.speed - .1) / 1.9) {
            @Override protected void updateMessage() { setMessage(speedLabel()); }
            @Override protected void applyValue() { draft.speed = .1 + value * 1.9; }
        });
        int cell = controlsWidth / PRESETS.length;
        for (int i = 0; i < PRESETS.length; i++) {
            int color = PRESETS[i];
            Button preset = Button.builder(Component.literal("■").withColor(color), b -> {
                setRgb(color);
                syncWidgets();
            }).bounds(controlsX + i * cell, top + 136, cell - 2, 16)
              .tooltip(Tooltip.create(Component.literal(PRESET_NAMES[i]))).build();
            addRenderableWidget(preset);
        }
        int actionWidth = (panelWidth - 26) / 2;
        int actionsY = top + panelHeight - 22;
        button("Отмена", left + 11, actionsY, actionWidth, 17, this::onClose);
        button("Применить", left + 15 + actionWidth, actionsY, actionWidth, 17, this::apply);
        syncWidgets();
    }

    private void apply() {
        try {
            setRgb(ColorStyle.parseHex(hex.getValue()));
        } catch (IllegalArgumentException error) {
            message = error.getMessage();
            return;
        }
        Config config = VisualFX.config;
        int beforePalette = target.palette(config);
        ColorStyle before = target.style(config).copy();
        target.apply(config, mode, draft);
        if (!config.saveChecked()) {
            target.apply(config, beforePalette, before);
            message = "Не удалось сохранить цвет. Попробуйте ещё раз.";
            return;
        }
        minecraft.setScreen(parent);
    }

    private Button button(String label, int x, int y, int width, int height, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(label), button -> action.run())
            .bounds(x, y, width, height).build());
    }

    private Component speedLabel() {
        return Component.literal(String.format(Locale.ROOT, "Скорость: %.2f", draft.speed));
    }

    private int rgb() { return endpoint == 0 ? draft.start : draft.end; }

    private void setRgb(int value) {
        if (endpoint == 0) draft.start = value;
        else draft.end = value;
    }

    private void syncHex() {
        syncing = true;
        hex.setValue(ColorStyle.hex(rgb()));
        syncing = false;
    }

    private void syncPicker() {
        int color = rgb();
        Color.RGBtoHSB(color >> 16 & 255, color >> 8 & 255, color & 255, hsb);
        spectrum.sync();
        brightness.sync();
    }

    private void updateFromPicker() {
        setRgb(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0xFFFFFF);
        syncHex();
        spectrum.sync();
        brightness.sync();
        message = DEFAULT_HINT;
    }

    private void syncWidgets() {
        syncHex();
        syncPicker();
        startButton.active = endButton.active = speedSlider.active = mode == 6;
        startButton.setMessage(Component.literal(endpoint == 0 ? "[ Начало ]" : "Начало"));
        endButton.setMessage(Component.literal(endpoint == 1 ? "[ Конец ]" : "Конец"));
        solidButton.setMessage(Component.literal(mode == 5 ? "[ Один цвет ]" : "Один цвет"));
        gradientButton.setMessage(Component.literal(mode == 6 ? "[ Градиент ]" : "Градиент"));
        speedSlider.setMessage(speedLabel());
        message = DEFAULT_HINT;
    }

    /** Rotates the GUI's vertical gradient primitive into a horizontal one. */
    private static void gradientAcross(GuiGraphics graphics, int x, int y, int width, int height, int from, int to) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y + height).rotate(-(float) Math.PI / 2);
        graphics.fillGradient(0, 0, height, width, from, to);
        graphics.pose().popMatrix();
    }

    private final class ColorSquare extends AbstractWidget {
        ColorSquare(int x, int y, int size) {
            super(x, y, size, size, Component.literal("Насыщенность и яркость"));
            setTooltip(Tooltip.create(getMessage()));
        }

        private void pick(MouseButtonEvent event) {
            hsb[1] = (float) Math.clamp((event.x() - getX()) / (getWidth() - 1), 0, 1);
            hsb[2] = 1 - (float) Math.clamp((event.y() - getY()) / (getHeight() - 1), 0, 1);
            updateFromPicker();
        }

        @Override public void onClick(MouseButtonEvent event, boolean doubleClick) { pick(event); }
        @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) { pick(event); }

        @Override public boolean keyPressed(KeyEvent event) {
            switch (event.key()) {
                case InputConstants.KEY_LEFT -> hsb[1] = Math.max(0, hsb[1] - .02f);
                case InputConstants.KEY_RIGHT -> hsb[1] = Math.min(1, hsb[1] + .02f);
                case InputConstants.KEY_DOWN -> hsb[2] = Math.max(0, hsb[2] - .02f);
                case InputConstants.KEY_UP -> hsb[2] = Math.min(1, hsb[2] + .02f);
                default -> { return super.keyPressed(event); }
            }
            updateFromPicker();
            return true;
        }

        @Override protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }

        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY(), size = getWidth();
            gradientAcross(graphics, x, y, size, size, 0xFFFFFFFF, Color.HSBtoRGB(hsb[0], 1, 1));
            graphics.fillGradient(x, y, x + size, y + size, 0x00000000, 0xFF000000);
            graphics.renderOutline(x - 1, y - 1, size + 2, size + 2, isFocused() ? frameAccent : 0xFF58636C);
            int selectedX = x + Math.round(hsb[1] * (size - 1));
            int selectedY = y + Math.round((1 - hsb[2]) * (size - 1));
            graphics.renderOutline(selectedX - 4, selectedY - 4, 9, 9, 0xFA070A0D);
            graphics.renderOutline(selectedX - 3, selectedY - 3, 7, 7, 0xFFFFFFFF);
        }
    }

    private final class ColorSlider extends AbstractSliderButton {
        private final boolean hue;

        ColorSlider(int x, int y, int width, boolean hue) {
            super(x, y, width, 13, Component.literal(hue ? "Оттенок" : "Яркость"), 0);
            this.hue = hue;
            setTooltip(Tooltip.create(getMessage()));
        }

        void sync() { value = hsb[hue ? 0 : 2]; }
        @Override protected void updateMessage() {}

        @Override protected void applyValue() {
            hsb[hue ? 0 : 2] = (float) value;
            updateFromPicker();
        }

        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int x = getX(), y = getY(), length = getWidth() - 8;
            graphics.fill(x, y, x + getWidth(), y + 13, 0xFF17212A);
            if (hue) {
                for (int i = 0; i < 6; i++) {
                    int start = x + 4 + i * length / 6;
                    int end = x + 4 + (i + 1) * length / 6;
                    gradientAcross(graphics, start, y + 3, end - start, 7,
                        Color.HSBtoRGB(i / 6f, 1, 1), Color.HSBtoRGB((i + 1) / 6f, 1, 1));
                }
            } else {
                gradientAcross(graphics, x + 4, y + 3, length, 7, 0xFF000000, Color.HSBtoRGB(hsb[0], hsb[1], 1));
            }
            int marker = x + 4 + (int) Math.round(value * length);
            graphics.renderOutline(marker - 3, y + 1, 7, 11, 0xFF06090C);
            graphics.renderOutline(marker - 2, y + 2, 5, 9, 0xFFFFFFFF);
            if (isFocused()) graphics.renderOutline(x, y, getWidth(), 13, frameAccent);
        }
    }

    @Override public void renderBackground(GuiGraphics graphics, int x, int y, float delta) {}

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        double phase = System.nanoTime() / 1e9 * .45;
        int animated = draft.sample(mode, phase);
        int theme = target == ColorTarget.MENU ? animated : MenuTheme.current();
        frameAccent = 0xFF000000 | MenuTheme.accent(theme);
        graphics.fill(0, 0, width, height, 0xBA000000 | MenuTheme.surface(0x020409, theme, .035));
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFA000000 | MenuTheme.surface(0x10141B, theme, .09));
        graphics.renderOutline(left, top, panelWidth, panelHeight, frameAccent);
        graphics.drawString(font, "Редактор цветов / " + target.title, left + 11, top + 9, frameAccent, false);

        int y = top + 27, previewWidth = controlsWidth - 22;
        gradientAcross(graphics, controlsX, y, previewWidth, 14, 0xFF000000 | draft.start,
            0xFF000000 | (mode == 6 ? draft.end : draft.start));
        graphics.renderOutline(controlsX, y, previewWidth, 14, 0xFF63717A);
        int swatchX = controlsX + controlsWidth - 18;
        graphics.fill(swatchX, y, swatchX + 18, y + 14, 0xFF000000 | rgb());
        graphics.renderOutline(swatchX, y, 18, 14, 0xFFD9E1E7);

        int hintY = top + panelHeight - 34;
        String hint = message;
        if (font.width(hint) > panelWidth - 22) hint = font.plainSubstrByWidth(hint, panelWidth - 30) + "…";
        graphics.drawString(font, hint, left + 11, hintY, 0xFF91A5AC, false);
        if (mouseY >= hintY - 2 && mouseY < hintY + 9) {
            graphics.setTooltipForNextFrame(Component.literal(message), mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
