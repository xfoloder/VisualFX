package dev.visualfx.mixin;

import dev.visualfx.Presence;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTabOverlay.class)
abstract class TabOverlayMixin {
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void visualfx$peerBadge(PlayerInfo player, CallbackInfoReturnable<Component> info) {
        var name = Presence.decorate(info.getReturnValue(), player.getProfile().id());
        if (name != info.getReturnValue()) info.setReturnValue(name);
    }
}
