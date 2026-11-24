package dev.visualfx.mixin;

import dev.visualfx.ItemPose;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
abstract class ItemPoseMixin implements ItemPose {
    @Unique private boolean visualfx$onGround;

    @Override public boolean visualfx$grounded() { return visualfx$onGround; }
    @Override public void visualfx$grounded(boolean grounded) { visualfx$onGround = grounded; }
}
