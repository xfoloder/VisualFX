package dev.visualfx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VisualFX implements ClientModInitializer {
    public static final Logger LOG=LoggerFactory.getLogger("VisualFX");
    public static Config config;
    public static final TargetSystem TARGET = new TargetSystem();
    private boolean held;
    private final TargetHud hud=new TargetHud();
    private final Watermark watermark=new Watermark();
    private final EffectStatus effectStatus=new EffectStatus();
    private final SeasonalParticles seasons = new SeasonalParticles();
    private final Presence presence = new Presence();
    @Override public void onInitializeClient() {
        config=Config.load();
        FeaturedServer.register();
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client->SoundCloudPlayer.INSTANCE.stop());
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementBefore(
            net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CHAT,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("visualfx","target_hud"),hud::render);
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementBefore(
            net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CHAT,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("visualfx","watermark"),watermark::render);
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementBefore(
            net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.CHAT,
            net.minecraft.resources.Identifier.fromNamespaceAndPath("visualfx","effect_status"),effectStatus::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean down=InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
            if(down&&!held) {
                if(client.gui.screen() instanceof ColorEditorScreen color) color.onClose();
                else if(client.gui.screen() instanceof SoundCloudScreen music) music.onClose();
                else if(client.gui.screen() instanceof HudEditorScreen e) e.onClose();
                else if(client.gui.screen() instanceof PresenceScreen link) link.onClose();
                else if(client.gui.screen() instanceof TerminalScreen s) s.toggleFromKey();
                else if(client.gui.screen()==null) client.gui.setScreen(new TerminalScreen());
            }
            held=down; TARGET.tick(client);hud.tick(client);effectStatus.tick(client);
            seasons.tick(client);
            presence.tick(client);
        });
        LevelExtractionEvents.END_EXTRACTION.register(context -> {
            TARGET.extract(Minecraft.getInstance());
            hud.project(context.levelState().cameraRenderState);
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            TARGET.render(context);
            seasons.render(context, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        });
        AttackEntityCallback.EVENT.register((player,world,hand,entity,hit) -> {
            if(world.isClientSide() && player==Minecraft.getInstance().player && entity instanceof Player targetPlayer && (config.hitEffect||config.neonEcho)) TARGET.hit(targetPlayer);
            return InteractionResult.PASS;
        });
    }
}








