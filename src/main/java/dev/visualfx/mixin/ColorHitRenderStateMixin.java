package dev.visualfx.mixin;

import dev.visualfx.ColorHitState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
abstract class ColorHitRenderStateMixin implements ColorHitState.Render {
    @Unique private int visualfx$hitTint;

    @Override
    public int visualfx$hitTint() {
        return visualfx$hitTint;
    }

    @Override
    public void visualfx$setHitTint(int tint) {
        visualfx$hitTint = tint;
    }
}
