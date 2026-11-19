package dev.visualfx.mixin;

import dev.visualfx.AimAssist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class AimMouseMixin {
    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void visualfx$assistAim(double elapsed, CallbackInfo ci) {
        AimAssist.frame(Minecraft.getInstance(), elapsed);
    }
}
