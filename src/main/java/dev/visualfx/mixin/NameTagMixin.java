package dev.visualfx.mixin;

import dev.visualfx.Presence;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
abstract class NameTagMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("TAIL"))
    private void visualfx$peerTag(Entity entity, EntityRenderState state, float tick, CallbackInfo info) {
        if (state.nameTag != null && entity instanceof Player) {
            state.nameTag = Presence.decorate(state.nameTag, entity.getUUID());
        }
    }
}
