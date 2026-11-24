package dev.visualfx;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

final class FeaturedServer {
    private static final String NAME = "Cynthia-metaphor";
    private static final String ADDRESS = "cynthia-metaphor.gomc.me";
    private static final int DEFAULT_PORT = 25565;
    private static boolean invitationQueued;

    private FeaturedServer() {}

    static void register() {
        ClientLifecycleEvents.CLIENT_STARTED.register(FeaturedServer::install);
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof JoinMultiplayerScreen) || invitationQueued) return;
            invitationQueued = true;
            client.gui.toastManager().addToast(new InvitationToast(client.font, width));
        });
    }

    static void install(Minecraft client) {
        Config config = VisualFX.config;
        if (config.cynthiaServerInitialized) return;

        var listPath = client.gameDirectory.toPath().resolve("servers.dat");
        try {
            if (Files.exists(listPath) && NbtIo.read(listPath) == null) return;
        } catch (IOException | RuntimeException unreadable) {
            VisualFX.LOG.warn("Cynthia-metaphor was not added because servers.dat could not be read", unreadable);
            return;
        }

        var servers = new ServerList(client);
        servers.load();
        if (!containsServer(servers)) {
            servers.add(new ServerData(NAME, ADDRESS, ServerData.Type.OTHER), false);
            servers.save();
            servers.load();
            if (!containsServer(servers)) return;
        }
        config.cynthiaServerInitialized = true;
        config.saveChecked();
    }

    private static boolean containsServer(ServerList servers) {
        for (int i = 0; i < servers.size(); i++) {
            if (isFeaturedAddress(servers.get(i).ip)) return true;
        }
        return false;
    }

    private static boolean isFeaturedAddress(String value) {
        ServerAddress address = ServerAddress.parseString(value.trim());
        String host = address.getHost();
        if (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return address.getPort() == DEFAULT_PORT && host.equalsIgnoreCase(ADDRESS);
    }

    private static final class InvitationToast implements Toast {
        private static final Component TITLE = Component.literal("Пожалуйста, посетите мой сервер " + NAME);
        private static final Component IP = Component.literal("IP: " + ADDRESS);
        private final int width;
        private final List<FormattedCharSequence> lines;
        private final List<FormattedCharSequence> addressLines;
        private Visibility visibility = Visibility.SHOW;

        InvitationToast(Font font, int screenWidth) {
            int desired = Math.max(font.width(TITLE), font.width(IP)) + 24;
            width = Math.min(desired, Math.max(120, screenWidth - 16));
            lines = font.split(TITLE, width - 24);
            addressLines = font.split(IP, width - 24);
        }

        @Override public int width() { return width; }
        @Override public int height() { return 15 + 12 * (lines.size() + addressLines.size()); }
        @Override public Visibility getWantedVisibility() { return visibility; }

        @Override
        public void update(ToastManager manager, long visibleTime) {
            if (visibleTime >= 6500 * manager.getNotificationDisplayTimeMultiplier()) visibility = Visibility.HIDE;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long visibleTime) {
            graphics.fill(0, 0, width, height(), 0xFA101921);
            graphics.outline(0, 0, width, height(), 0xFF255047);
            graphics.fill(0, 0, 3, height(), 0xFF39FF75);
            int row = 8;
            for (FormattedCharSequence line : lines) {
                graphics.text(font, line, 12, row, 0xFFF0FAF3, false);
                row += 12;
            }
            row += 4;
            for (FormattedCharSequence line : addressLines) {
                graphics.text(font, line, 12, row, 0xFF39FF75, false);
                row += 12;
            }
        }
    }
}
