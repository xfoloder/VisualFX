package dev.visualfx;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.UUID;

final class PresenceScreen extends Screen {
    private final Screen parent;
    private EditBox url, key;
    private int x, y, panelWidth;
    private String message = "";

    PresenceScreen(Screen parent) {
        super(Component.literal("VisualFX / Связь клиентов"));
        this.parent = parent;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override protected void init() {
        var config = VisualFX.config;
        String previousUrl = url == null ? config.presenceUrl : url.getValue();
        String previousKey = key == null ? config.presenceKey : key.getValue();
        panelWidth = Math.min(376, width - 16);
        x = (width - panelWidth) / 2;
        y = (height - 160) / 2;
        url = addRenderableWidget(new EditBox(font, x + 10, y + 40, panelWidth - 20, 20, Component.literal("Адрес ретранслятора")));
        url.setMaxLength(200);
        url.setValue(previousUrl);
        key = addRenderableWidget(new EditBox(font, x + 10, y + 78, panelWidth - 20, 20, Component.literal("Ключ комнаты")));
        key.setMaxLength(96);
        key.setValue(previousKey);
        int half = (panelWidth - 24) / 2;
        button("Создать ключ", x + 10, y + 105, half, () -> key.setValue(UUID.randomUUID().toString()));
        button("Сохранить", x + 14 + half, y + 105, half, this::save);
        setInitialFocus(url);
    }

    private void button(String label, int x, int y, int width, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), button -> action.run()).bounds(x, y, width, 19).build());
    }

    private void save() {
        String endpoint = url.getValue().trim(), token = key.getValue().trim();
        if (!Presence.validUrl(endpoint) || !Presence.validKey(token)) {
            message = "Проверьте адрес и ключ комнаты.";
            return;
        }
        var config = VisualFX.config;
        String previousUrl = config.presenceUrl, previousKey = config.presenceKey;
        config.presenceUrl = endpoint;
        config.presenceKey = token;
        if (config.saveChecked()) onClose();
        else {
            config.presenceUrl = previousUrl;
            config.presenceKey = previousKey;
            message = "Не удалось сохранить настройки.";
        }
    }

    @Override public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {}

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xB0090C14);
        g.fill(x, y, x + panelWidth, y + 160, 0xF50E161F);
        g.outline(x, y, panelWidth, 160, 0xFF39FF75);
        g.text(font, getTitle(), x + 10, y + 8, 0xFF39FF75, false);
        g.text(font, "Адрес ретранслятора", x + 10, y + 27, 0xFFD0DCE3, false);
        g.text(font, "Общий приватный ключ комнаты", x + 10, y + 65, 0xFFD0DCE3, false);
        String hint = message.isEmpty() ? "Друзья вводят тот же адрес и ключ." : message;
        g.text(font, font.plainSubstrByWidth(hint, panelWidth - 20), x + 10, y + 136, 0xFFA4B6C3, false);
        super.extractRenderState(g, mx, my, delta);
    }

    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
