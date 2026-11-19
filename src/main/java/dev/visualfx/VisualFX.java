package dev.visualfx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;
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
            boolean down=GLFW.glfwGetKey(client.getWindow().getHandle(),GLFW.GLFW_KEY_RIGHT_SHIFT)==GLFW.GLFW_PRESS;
            if(down&&!held) {
                if(client.currentScreen instanceof TerminalScreen s) s.close();
                else if(client.currentScreen==null) client.setScreen(new TerminalScreen());
            }
            held=down; TARGET.tick(client);
        });
        WorldRenderEvents.END_EXTRACTION.register(context -> TARGET.extract(MinecraftClient.getInstance()));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> TARGET.render(context));
        AttackEntityCallback.EVENT.register((player,world,hand,entity,hit) -> {
            if(world.isClient() && player==MinecraftClient.getInstance().player && entity instanceof PlayerEntity && config.hitEffect) TARGET.hit(entity.getId());
            return ActionResult.PASS;
        });
    }
}
