package dev.visualfx;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

final class SeasonalParticles {
    private static final int MAX = 110;
    private static final double TURN = 2 * Math.PI;
    private static final int[] LEAVES = {0xE9A13D, 0xCC642A, 0xD7572E, 0xF2C454};
    private final Particle[] particles = new Particle[MAX];
    private final Random random = new Random();
    private final ThreadLocal<Geometry> geometry = ThreadLocal.withInitial(Geometry::new);
    private ClientLevel world;
    private int previousMode, next, spawnDelay;

    SeasonalParticles() {
        for (int i = 0; i < MAX; i++) particles[i] = new Particle();
    }

    private static final class Particle {
        double x, y, z, size, phase;
        int life, peak, color;
    }

    void tick(Minecraft client) {
        var config = VisualFX.config;
        int mode = config.seasonParticles;
        if (world != client.level || previousMode != mode) {
            for (Particle particle : particles) particle.life = 0;
            world = client.level;
            previousMode = mode;
            next = spawnDelay = 0;
        }
        var player = client.player;
        if (world == null || player == null || mode == 0 || client.isPaused()) return;
        for (Particle particle : particles) {
            if (particle.life <= 0) continue;
            particle.life--;
            particle.x += (mode == 1 ? .025 : .05) + .019 * Math.sin(particle.phase + particle.life * .08);
            particle.z += .018 * Math.cos(particle.phase + particle.life * .075);
            particle.y -= mode == 1 ? .030 : .055;
            if (Math.abs(player.getX() - particle.x) > 18 || Math.abs(player.getZ() - particle.z) > 18
                    || world.getBlockState(BlockPos.containing(particle.x, particle.y, particle.z)).isSolidRender()) particle.life = 0;
        }
        int interval = (int) Math.round(7 - Math.min(config.seasonDensity, config.optimization ? 2 : 5));
        if (++spawnDelay < interval) return;
        spawnDelay = 0;
        for (int i = 0; i < (config.optimization ? 1 : 2); i++) {
            double x = player.getX() + random.nextDouble(-10, 10);
            double z = player.getZ() + random.nextDouble(-10, 10);
            double y = player.getY() + random.nextDouble(1.1, 5);
            if (!world.hasChunk((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4)
                    || !world.canSeeSky(BlockPos.containing(x, y, z))) continue;
            var particle = particles[next];
            next = (next + 1) % MAX;
            particle.x = x;
            particle.y = y;
            particle.z = z;
            particle.phase = random.nextDouble(TURN);
            particle.size = random.nextDouble(.085, mode == 1 ? .16 : .22);
            particle.life = particle.peak = random.nextInt(70, 115);
            particle.color = mode == 1 ? 0xDEF6FF : LEAVES[random.nextInt(LEAVES.length)];
        }
    }

    void render(WorldRenderContext ctx, float partialTick) {
        int mode = VisualFX.config.seasonParticles;
        if (mode == 0 || mode != previousMode) return;
        var camera = ctx.worldState().cameraRenderState;
        var pos = camera.pos;
        var orientation = new Quaternionf(camera.orientation);
        var right = orientation.transform(new Vector3f(1, 0, 0));
        var up = orientation.transform(new Vector3f(0, 1, 0));
        var vertices = ctx.consumers().getBuffer(RenderTypes.debugQuads());
        {
            var g = geometry.get();
            g.begin(vertices, ctx.matrices().last().pose(), VisualFX.config, 0, 1, 0);
            try {
                for (Particle p : particles) {
                    if (p.life <= 0) continue;
                    double drift = p.phase + p.life * .08;
                    double x = p.x - pos.x + partialTick * ((mode == 1 ? .025 : .05) + .019 * Math.sin(drift));
                    double y = p.y - pos.y - partialTick * (mode == 1 ? .030 : .055);
                    double z = p.z - pos.z + partialTick * .018 * Math.cos(p.phase + p.life * .075);
                    int alpha = (int) (215 * Math.min(1, Math.min(p.life, p.peak - p.life) / 12.0));
                    double age = p.peak - p.life + partialTick;
                    double angle = p.phase + age * (mode == 1 ? .025 : .10);
                    if (mode == 1) snow(g, right, up, x, y, z, p.size, angle, p.color, alpha);
                    else leaf(g, right, up, x, y, z, p.size, angle, p.color, alpha);
                }
            } finally { g.end(); }
        }
    }

    private static void snow(Geometry g, Vector3f right, Vector3f up, double x, double y, double z,
                             double size, double angle, int color, int alpha) {
        for (int i = 0; i < 6; i++) {
            double a = angle + i * TURN / 6;
            double u = Math.cos(a), v = Math.sin(a);
            stroke(g, right, up, x, y, z, 0, 0, u * size, v * size, size * .065, color, alpha);
            double branch = size * .43;
            for (int direction = -1; direction <= 1; direction += 2) {
                double b = a + direction * 2.15;
                stroke(g, right, up, x, y, z, u * size * .84, v * size * .84,
                        u * size * .84 + branch * Math.cos(b), v * size * .84 + branch * Math.sin(b),
                        size * .059, color, alpha);
            }
        }
    }

    private static void leaf(Geometry g, Vector3f right, Vector3f up, double x, double y, double z,
                             double size, double angle, int color, int alpha) {
        double u = Math.cos(angle), v = Math.sin(angle);
        double w = size * (.32 + Math.abs(Math.sin(angle * 1.3)) * .25);
        point(g, right, up, x, y, z, u * size, v * size, color, alpha);
        point(g, right, up, x, y, z, -v * w, u * w, color, alpha);
        point(g, right, up, x, y, z, -u * size * .88, -v * size * .88, color, alpha);
        point(g, right, up, x, y, z, v * w, -u * w, color, alpha);
        stroke(g, right, up, x, y, z, -u * size, -v * size, u * size * .78, v * size * .78, size * .055, 0x64331E, alpha);
    }

    private static void stroke(Geometry g, Vector3f right, Vector3f up, double x, double y, double z,
                               double a, double b, double c, double d, double width, int color, int alpha) {
        double length = Math.max(.000001, Math.hypot(c - a, d - b));
        double dx = -(d - b) * width / length, dy = (c - a) * width / length;
        point(g, right, up, x, y, z, a + dx, b + dy, color, alpha);
        point(g, right, up, x, y, z, a - dx, b - dy, color, alpha);
        point(g, right, up, x, y, z, c - dx, d - dy, color, alpha);
        point(g, right, up, x, y, z, c + dx, d + dy, color, alpha);
    }

    private static void point(Geometry g, Vector3f right, Vector3f up, double x, double y, double z,
                              double u, double v, int color, int alpha) {
        g.vertex(x + u * right.x + v * up.x, y + u * right.y + v * up.y, z + u * right.z + v * up.z, color, alpha);
    }
}
