package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;

final class NeonEcho {
    private final EchoHistory history=new EchoHistory();
    private ClientLevel world;
    private double clock,hitAt=-100;
    private EchoHistory.Snapshot frame;
    private final ThreadLocal<Geometry> geometry=ThreadLocal.withInitial(Geometry::new);
    void tick(Minecraft c){
        if(world!=c.level){world=c.level;history.clear();frame=null;clock=0;hitAt=-100;}
        clock+=.05;
        if(c.level==null||!VisualFX.config.neonEcho){history.clear();frame=null;return;}
        if(c.gui.screen()==null&&c.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof Player p&&p!=c.player&&p.isAlive()){
            int previous=history.target();
            history.push(p.getId(),p.getX(),p.getY(),p.getZ(),p.getBbHeight(),Math.toRadians(p.getYRot()),clock);
            if(previous!=history.target())hitAt=-100;
        }
    }
    void hit(int id){if(VisualFX.config.neonEcho&&VisualFX.config.echoPulse&&history.target()==id)hitAt=clock;}
    void extract(Minecraft c){
        if(c.level==null||!VisualFX.config.neonEcho||history.size()<2){frame=null;return;}
        double time=clock+c.getDeltaTracker().getGameTimeDeltaPartialTick(false)*.05;
        frame=history.snapshot(time,VisualFX.config.echoLength);
        if(frame.size()==0){history.clear();frame=null;}
    }
    void render(LevelRenderContext ctx){
        var snapshot=frame;if(snapshot==null||snapshot.size()<2)return;
        var origin=snapshot.points()[0];var camera=ctx.levelState().cameraRenderState.pos;
        var matrices=ctx.poseStack();matrices.pushPose();
        matrices.translate(origin.x()-camera.x,origin.y()-camera.y,origin.z()-camera.z);
        double pulse=VisualFX.config.echoPulse?Math.max(0,1-(snapshot.time()-hitAt)/.7):0;
        ctx.submitNodeCollector().submitCustomGeometry(matrices,RenderTypes.debugQuads(),(pose,vertices)->{
            Geometry g=geometry.get();Config cfg=VisualFX.config;
            int ghosts=cfg.optimization?Math.min(3,cfg.echoGhosts):cfg.echoGhosts;
            g.begin(vertices,pose.pose(),cfg,snapshot.time(),1,0);
            EchoVisual.draw(g,snapshot,ghosts,cfg.echoIntensity,pulse);g.end();
        });
        matrices.popPose();
    }
}
