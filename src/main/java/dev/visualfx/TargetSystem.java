package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.client.multiplayer.ClientLevel;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Two bounded slots allow cross fades without accumulating entity references. */
public final class TargetSystem {
    private ClientLevel world;
    private Player target;
    private final DeathEffectSystem deaths=new DeathEffectSystem();
    private final Slot[] slots={new Slot(),new Slot()};
    private long last=System.nanoTime();
    private double time;
    private final ThreadLocal<Geometry> geometry=ThreadLocal.withInitial(Geometry::new);
    static final class Slot {
        int id=-1; double x,y,z,height=1.8,alpha,hit;
    }
    public void tick(Minecraft c) {
        deaths.tick(c);
        if(world!=c.level) { world=c.level; target=null; for(Slot s:slots){s.id=-1;s.alpha=0;s.hit=0;} }
        target=VisualFX.config.enabled && c.screen==null && c.hitResult instanceof EntityHitResult h && h.getEntity() instanceof Player p && p!=c.player && p.isAlive() ? p:null;
    }
    public void hit(int id) { for(Slot s:slots) if(s.id==id) s.hit=1; }
    public void extract(Minecraft c) {
        long now=System.nanoTime(); double dt=Math.min(.1,(now-last)/1e9); last=now; time+=dt;
        if(c.level==null) return;
        Slot active=null;
        if(target!=null) {
            for(Slot s:slots) if(s.id==target.getId()) active=s;
            if(active==null) { active=slots[0].alpha<slots[1].alpha?slots[0]:slots[1]; active.id=target.getId(); active.alpha=0; active.hit=0; }
        }
        for(Slot s:slots) {
            boolean on=s==active;
            double ease=1-Math.exp(-dt/VisualFX.config.smoothing);
            s.alpha+=( (on?1:0)-s.alpha)*ease;
            s.hit=Math.max(0,s.hit-dt*1.7);
            if(on) { var pos=target.getPosition(c.getDeltaTracker().getGameTimeDeltaPartialTick(false)); s.x=pos.x;s.y=pos.y;s.z=pos.z;s.height=target.getBbHeight(); }
        }
    }
    public void render(WorldRenderContext ctx) {
        deaths.render(ctx);
        var camera=ctx.worldState().cameraRenderState.pos;
        var matrices=ctx.matrices();
        for(Slot s:slots) if(s.alpha>.005) {
            matrices.pushPose(); matrices.translate(s.x-camera.x,s.y-camera.y,s.z-camera.z);
            final double height=s.height, opacity=s.alpha, impact=s.hit, frameTime=time;
            final int mode=VisualFX.config.mode;
            var vertices=ctx.consumers().getBuffer(RenderTypes.debugQuads());
            {var pose=matrices.last();
                Geometry g=geometry.get();
                g.begin(vertices,pose.pose(),VisualFX.config,frameTime,opacity,impact);
                Effects.MODES[mode].draw(g,height);
                if(impact>0 && VisualFX.config.hitEffect) Effects.impact(g,height,impact);
                g.end();
            }
            matrices.popPose();
        }
    }
}




