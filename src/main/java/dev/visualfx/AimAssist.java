package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class AimAssist {
    // Entity.turn expects mouse units, scaled by vanilla to 0.15 degrees per unit.
    private static final double DEGREES_PER_MOUSE_UNIT = 0.15;

    static void tick(Minecraft client) {
        var player = client.player;
        Config config = VisualFX.config;
        if (!config.aimAssist || player == null || client.level == null || !player.isAlive()
            || player.isSpectator() || client.getCameraEntity() != player || client.gui.screen() != null
            || client.isPaused() || !client.isWindowActive() || !client.mouseHandler.isMouseGrabbed()
            || !InputConstants.isKeyDown(InputConstants.KEY_LALT)) return;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1);
        double bestAlignment = Math.cos(Math.toRadians(config.aimAngle));
        double rangeSquared = config.aimRange * config.aimRange;
        Vec3 bestDelta = null;

        for (Player target : client.level.players()) {
            if (target == player || !target.isAlive() || target.isSpectator() || !target.isPickable()
                || target.isInvisibleTo(player) || player.isAlliedTo(target)) continue;

            Vec3 point = target.getBoundingBox().getCenter();
            Vec3 delta = point.subtract(eye);
            double distanceSquared = delta.lengthSqr();
            if (distanceSquared < 1e-6 || distanceSquared > rangeSquared) continue;

            double alignment = look.dot(delta) / Math.sqrt(distanceSquared);
            if (alignment <= bestAlignment) continue;
            var ray = new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            if (client.level.clip(ray).getType() != HitResult.Type.MISS) continue;
            bestAlignment = alignment;
            bestDelta = delta;
        }

        if (bestDelta == null) return;
        double yaw = Math.toDegrees(Math.atan2(bestDelta.z, bestDelta.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(bestDelta.y, Math.hypot(bestDelta.x, bestDelta.z)));
        double yawError = Mth.wrapDegrees(yaw - player.getYRot());
        double pitchError = pitch - player.getXRot();
        double step = config.aimSpeed / 20;
        double weight = Math.min(.35, step / Math.max(1e-6, Math.hypot(yawError, pitchError)));
        player.turn(yawError * weight / DEGREES_PER_MOUSE_UNIT, pitchError * weight / DEGREES_PER_MOUSE_UNIT);
    }
}
