package dev.visualfx;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.world.ClientWorld;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;

/** Two bounded slots allow cross fades without accumulating entity references. */
public final class TargetSystem {
    private ClientWorld world;
    private PlayerEntity target;
    private final Slot[] slots={new Slot(),new Slot()};
    private long last=System.nanoTime();
    private double time;
    private final Geometry geometry=new Geometry();
    static final class Slot {
        int id=-1; double x,y,z,height=1.8,alpha,hit;
    }
    public void tick(MinecraftClient c) {
        if(world!=c.world) { world=c.world; target=null; for(Slot s:slots){s.id=-1;s.alpha=0;s.hit=0;} }
        target=VisualFX.config.enabled && c.currentScreen==null && c.crosshairTarget instanceof EntityHitResult h && h.getEntity() instanceof PlayerEntity p && p!=c.player && p.isAlive() ? p:null;
    }
    public void hit(int id) { for(Slot s:slots) if(s.id==id) s.hit=1; }
    public void extract(MinecraftClient c) {
        long now=System.nanoTime(); double dt=Math.min(.1,(now-last)/1e9); last=now; time+=dt;
        if(c.world==null) return;
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
            if(on) { var pos=target.getLerpedPos(c.getRenderTickCounter().getTickProgress(false)); s.x=pos.x;s.y=pos.y;s.z=pos.z;s.height=target.getHeight(); }
        }
    }
    public void render(WorldRenderContext ctx) {
        var camera=ctx.worldState().cameraRenderState.pos;
        var matrices=ctx.matrices();
        var vertices=ctx.consumers().getBuffer(RenderLayers.debugQuads());
        for(Slot s:slots) if(s.alpha>.005) {
            matrices.push(); matrices.translate(s.x-camera.x,s.y-camera.y,s.z-camera.z);
            Geometry g=geometry;
            g.begin(vertices,matrices.peek().getPositionMatrix(),VisualFX.config,time,s.alpha,s.hit);
            Effects.MODES[VisualFX.config.mode].draw(g,s.height);
            if(s.hit>0 && VisualFX.config.hitEffect) Effects.impact(g,s.height,s.hit);
            matrices.pop();
            g.end();
        }
    }
}
