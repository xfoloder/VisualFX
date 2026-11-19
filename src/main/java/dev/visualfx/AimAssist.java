package dev.visualfx;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class AimAssist {
    // Entity.turn expects mouse units, scaled by vanilla to 0.15 degrees per unit.
    private static final double DEGREES_PER_MOUSE_UNIT = 0.15;
    private static KeyMapping activation;
    private static ClientLevel world;
    private static Player selected;
    private static double untilScan;

    static void register() {
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("visualfx", "controls"));
        activation = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.visualfx.aim_assist", InputConstants.KEY_LALT, category));
    }

    static String keyLabel() {
        return activation.getTranslatedKeyMessage().getString();
    }

    public static void frame(Minecraft client, double elapsed) {
        var player = client.player;
        Config config = VisualFX.config;
        if (world != client.level) {
            world = client.level;
            selected = null;
            untilScan = 0;
        }
        if (config == null || activation == null || !activation.isDown() || !config.aimAssist
            || player == null || world == null || !player.isAlive()
            || player.isSpectator() || client.getCameraEntity() != player || client.gui.screen() != null
            || client.isPaused() || !client.isWindowActive()) {
            selected = null;
            untilScan = 0;
            return;
        }
        if (!Double.isFinite(elapsed) || elapsed <= 0) return;
        double seconds = Math.min(.05, elapsed);
        float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1);
        double cone = Math.cos(Math.toRadians(config.aimAngle));
        double rangeSquared = config.aimRange * config.aimRange;
        untilScan -= seconds;
        if (untilScan <= 0) {
            double bestAlignment = cone;
            selected = null;
            for (Player target : world.players()) {
                if (!eligible(player, target)) continue;
                Vec3 point = aimPoint(target, partialTick);
                double alignment = alignment(look, point.subtract(eye), rangeSquared);
                if (alignment <= bestAlignment || !visible(player, eye, point)) continue;
                bestAlignment = alignment;
                selected = target;
            }
            untilScan = .05;
        }

        if (selected == null || !eligible(player, selected)) return;
        Vec3 point = aimPoint(selected, partialTick);
        Vec3 delta = point.subtract(eye);
        if (alignment(look, delta, rangeSquared) <= cone || !visible(player, eye, point)) return;
        double yaw = Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90;
        double pitch = -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
        double yawError = Mth.wrapDegrees(yaw - player.getYRot());
        double pitchError = pitch - player.getXRot();
        double easing = -Math.expm1(-seconds * 5);
        double step = config.aimSpeed * seconds;
        double weight = Math.min(easing, step / Math.max(1e-6, Math.hypot(yawError, pitchError)));
        player.turn(yawError * weight / DEGREES_PER_MOUSE_UNIT, pitchError * weight / DEGREES_PER_MOUSE_UNIT);
    }

    private static boolean eligible(Player player, Player target) {
        return target != player && target.isAlive() && !target.isSpectator() && target.isPickable()
            && !target.isInvisibleTo(player) && !player.isAlliedTo(target);
    }

    private static Vec3 aimPoint(Player target, float partialTick) {
        return target.getPosition(partialTick).add(0, target.getBbHeight() * .5, 0);
    }

    private static double alignment(Vec3 look, Vec3 delta, double rangeSquared) {
        double distanceSquared = delta.lengthSqr();
        return distanceSquared >= 1e-6 && distanceSquared <= rangeSquared
            ? look.dot(delta) / Math.sqrt(distanceSquared) : -1;
    }

    private static boolean visible(Player player, Vec3 eye, Vec3 point) {
        var ray = new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        return world.clip(ray).getType() == HitResult.Type.MISS;
    }
}
