package dev.visualfx.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.visualfx.VisualFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
abstract class ItemScrollerMixin {
    @Shadow @Final protected AbstractContainerMenu menu;
    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int button, ClickType input);
    @Invoker("getHoveredSlot") protected abstract Slot visualfx$slotAt(double x, double y);

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void visualfx$scroll(double x, double y, double horizontal, double vertical, CallbackInfoReturnable<Boolean> info) {
        if (!VisualFX.config.itemScroller || vertical == 0 || !menu.getCarried().isEmpty()
                || (Object) this instanceof CreativeModeInventoryScreen) return;
        if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_LSHIFT) && !InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), InputConstants.KEY_RSHIFT)) return;
        var player = Minecraft.getInstance().player;
        var slot = visualfx$slotAt(x, y);
        if (player == null || player.isSpectator() || slot == null || !slot.hasItem()
                || !slot.isActive() || slot.isFake() || !slot.mayPickup(player)) return;
        slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
        info.setReturnValue(true);
    }
}
