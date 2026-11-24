package dev.visualfx.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visualfx.ItemPose;
import dev.visualfx.VisualFX;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
abstract class ItemRendererMixin {
    private static final String SUBMIT = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V";

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void visualfx$extractGround(ItemEntity entity, ItemEntityRenderState state, float tick, CallbackInfo info) {
        ((ItemPose) state).visualfx$grounded(entity.onGround());
    }

    @Redirect(method = SUBMIT, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visualfx$position(PoseStack pose, float x, float y, float z, ItemEntityRenderState state,
                                   PoseStack ignored, SubmitNodeCollector collector, CameraRenderState camera) {
        if (VisualFX.config.itemPhysics) {
            var bounds = state.item.getModelBoundingBox();
            boolean ground = ((ItemPose) state).visualfx$grounded();
            if (ground) {
                y = (float) (bounds.getZsize() <= .0625 ? bounds.maxZ : -bounds.minY) + .016f;
            } else {
                y = (float) Math.max(.05, -bounds.minY * .65);
            }
        }
        pose.translate(x, y, z);
    }

    @Redirect(method = SUBMIT, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    private void visualfx$rotation(PoseStack pose, Quaternionfc rotation, ItemEntityRenderState state,
                                   PoseStack ignored, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!VisualFX.config.itemPhysics) {
            pose.mulPose(rotation);
            return;
        }
        boolean ground = ((ItemPose) state).visualfx$grounded();
        var bounds = state.item.getModelBoundingBox();
        if (ground) pose.mulPose(Axis.YP.rotation(state.bobOffset));
        else pose.mulPose(rotation);
        if (ground) {
            if (bounds.getZsize() <= .0625) pose.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            pose.mulPose(Axis.XP.rotation(state.ageInTicks * .28f + state.bobOffset));
        }
    }
}
