package dev.visualfx.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Reads vanilla's effective FOV so the target HUD also follows sprint/FOV effects. */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("getFov")
    float visualfx$getFov(Camera camera,float partialTick,boolean useSetting);
}
