package dev.visualfx.mixin;

import dev.visualfx.ColorHitState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class ColorHitDamageMixin implements ColorHitState.Hurt {
    @Unique private boolean visualfx$hitByLocalPlayer;

    @Inject(method = "handleDamageEvent", at = @At("RETURN"))
    private void visualfx$recordAttacker(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof Player target && target.level().isClientSide()) {
            Player local = Minecraft.getInstance().player;
            visualfx$hitByLocalPlayer = local != null && target != local && source.getDirectEntity() == local;
        }
    }

    @Override
    public boolean visualfx$wasHitByLocalPlayer() {
        return visualfx$hitByLocalPlayer;
    }
}
