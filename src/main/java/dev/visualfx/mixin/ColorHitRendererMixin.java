package dev.visualfx.mixin;

import dev.visualfx.ColorHitState;
import dev.visualfx.ColorStyle;
import dev.visualfx.Config;
import dev.visualfx.VisualFX;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
abstract class ColorHitRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void visualfx$extractColorHit(LivingEntity entity, LivingEntityRenderState state, float partialTick,
                                         CallbackInfo ci) {
        int tint = 0;
        Config config = VisualFX.config;
        if (config.colorHit && entity instanceof Player && entity.hurtTime > 0
            && ((ColorHitState.Hurt) entity).visualfx$wasHitByLocalPlayer()) {
            double remaining = Math.clamp((entity.hurtTime - partialTick) / entity.hurtDuration, 0, 1);
            double elapsed = (entity.hurtDuration - entity.hurtTime + partialTick) / 20.0;
            int color = config.colorHitStyle.sample(config.colorHitPalette, elapsed);
            tint = 0xFF000000 | ColorStyle.mix(0xFFFFFF, color, remaining);
        }
        ((ColorHitState.Render) state).visualfx$setHitTint(tint);
    }

    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void visualfx$tintModel(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        int tint = ((ColorHitState.Render) state).visualfx$hitTint();
        if (tint != 0) cir.setReturnValue(ARGB.multiply(cir.getReturnValueI(), tint));
    }

    @Inject(method = "getOverlayCoords", at = @At("HEAD"), cancellable = true)
    private static void visualfx$replaceRedOverlay(LivingEntityRenderState state, float whiteProgress,
                                                  CallbackInfoReturnable<Integer> cir) {
        if (((ColorHitState.Render) state).visualfx$hitTint() != 0) {
            cir.setReturnValue(OverlayTexture.pack(whiteProgress, false));
        }
    }
}
