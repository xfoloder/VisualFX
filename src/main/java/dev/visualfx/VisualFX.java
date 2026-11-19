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
    @Override public void onInitializeClient() {
        config=Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean down=InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
            if(down&&!held) {
                if(client.gui.screen() instanceof TerminalScreen s) s.onClose();
                else if(client.gui.screen()==null) client.gui.setScreen(new TerminalScreen());
            }
            held=down; TARGET.tick(client);
        });
        LevelExtractionEvents.END_EXTRACTION.register(context -> TARGET.extract(Minecraft.getInstance()));
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> TARGET.render(context));
        AttackEntityCallback.EVENT.register((player,world,hand,entity,hit) -> {
            if(world.isClientSide() && player==Minecraft.getInstance().player && entity instanceof Player && config.hitEffect) TARGET.hit(entity.getId());
            return InteractionResult.PASS;
        });
    }
}





