package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Four independent attack bursts, including when Target ESP is disabled. */
final class HitEffectSystem {
    private ClientLevel world;
    private final Burst[] bursts={new Burst(),new Burst(),new Burst(),new Burst()};
    private final ThreadLocal<Geometry> geometry=ThreadLocal.withInitial(Geometry::new);
    private static final class Burst {int id,mode;double x,y,z,h,age=Double.POSITIVE_INFINITY,duration=.6;}
    void tick(Minecraft c){
        if(world!=c.level){world=c.level;for(Burst b:bursts)b.age=Double.POSITIVE_INFINITY;}
        if(!VisualFX.config.hitEffect)for(Burst b:bursts)b.age=Double.POSITIVE_INFINITY;
    }
    void spawn(Player p){
        if(!VisualFX.config.hitEffect)return;
        Burst chosen=bursts[0];
        for(Burst b:bursts){if(b.age>=b.duration){chosen=b;break;}if(b.age>chosen.age)chosen=b;}
        chosen.id=p.getId();chosen.x=p.getX();chosen.y=p.getY();chosen.z=p.getZ();chosen.h=p.getBbHeight();
        chosen.age=0;chosen.duration=VisualFX.config.hitDuration;chosen.mode=VisualFX.config.hitMode;
    }
    void extract(Minecraft c,double dt){
        if(c.level==null)return;
        for(Burst b:bursts)if(b.age<b.duration){
            b.age+=dt;
            var entity=c.level.getEntity(b.id);
            if(entity instanceof Player p&&!p.isRemoved()){
                var pos=p.getPosition(c.getDeltaTracker().getGameTimeDeltaPartialTick(false));b.x=pos.x;b.y=pos.y;b.z=pos.z;
            }
        }
    }
    void render(LevelRenderContext ctx){
        var matrices=ctx.poseStack();var camera=ctx.levelState().cameraRenderState.pos;
        for(Burst b:bursts)if(b.age<b.duration){
            final double life=1-b.age/b.duration,h=b.h,time=b.age;final int mode=b.mode;
            matrices.pushPose();matrices.translate(b.x-camera.x,b.y-camera.y,b.z-camera.z);
            ctx.submitNodeCollector().submitCustomGeometry(matrices,RenderTypes.debugQuads(),(pose,vertices)->{
                Geometry g=geometry.get();g.begin(vertices,pose.pose(),VisualFX.config,time,1,life);
                HitEffects.draw(g,h,life,mode);g.end();
            });
            matrices.popPose();
        }
    }
}
