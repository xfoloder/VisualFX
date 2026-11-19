package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** Observes actual client death state. A disappearing or unloaded entity is not a death. */
final class DeathEffectSystem {
    static final String[] NAMES={"Ascend","Exit Hell"};
    private static final int LIFETIME=52;
    private ClientLevel world;
    private Player watched;
    private int watchTicks;
    private final Burst[] bursts={new Burst(),new Burst(),new Burst(),new Burst()};
    private final ThreadLocal<Geometry> geometry=ThreadLocal.withInitial(Geometry::new);
    private static final class Burst { double x,y,z,h; int age=LIFETIME,mode; }
    void tick(Minecraft c){
        if(world!=c.level){world=c.level;watched=null;watchTicks=0;for(Burst b:bursts)b.age=LIFETIME;}
        for(Burst b:bursts)if(b.age<LIFETIME)b.age++;
        if(c.level==null)return;
        if(!VisualFX.config.deathEffect){watched=null;for(Burst b:bursts)b.age=LIFETIME;return;}
        if(watched!=null){
            if(watched.isDeadOrDying()){
                Burst chosen=bursts[0];for(Burst b:bursts)if(b.age>chosen.age)chosen=b;
                chosen.x=watched.getX();chosen.y=watched.getY();chosen.z=watched.getZ();chosen.h=watched.getBbHeight();
                chosen.age=0;chosen.mode=VisualFX.config.deathMode;
                watched=null;watchTicks=0;
            }else if(watched.isRemoved()||--watchTicks<=0){watched=null;}
        }
        if(c.gui.screen()==null&&c.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof Player p&&p!=c.player&&p.isAlive()){
            watched=p;watchTicks=40;
        }
    }
    void render(LevelRenderContext ctx){
        var matrices=ctx.poseStack();var camera=ctx.levelState().cameraRenderState.pos;
        float partial=Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        for(Burst b:bursts)if(b.age<LIFETIME){
            final double age=Math.clamp((b.age+partial)/LIFETIME,0,1),h=b.h;
            final int mode=b.mode;
            matrices.pushPose();matrices.translate(b.x-camera.x,b.y-camera.y,b.z-camera.z);
            ctx.submitNodeCollector().submitCustomGeometry(matrices,RenderTypes.debugQuads(),(pose,vertices)->{
                Geometry g=geometry.get();g.begin(vertices,pose.pose(),VisualFX.config,age*2.6,opacity(age),0);
                draw(g,h,age,mode);g.end();
            });
            matrices.popPose();
        }
    }
    static double opacity(double age){return Math.min(1,age*12)*Math.min(1,(1-age)*4);}
    static void draw(Geometry g,double h,double age,int mode){
        double r=g.c.radius;
        if(mode==0){
            // Three ascending halos and a column of floating light fragments.
            for(int band=0;band<3;band++){
                double y=age*2.7+band*.28;
                g.ring(y,r*(1-age*.45)*(1-band*.12),.08*Math.sin(age*8+band),age*5+band,.018);
            }
            for(int i=0;i<g.c.shards;i++){
                double a=i*2.39996+age*3,y=h*.3+age*(1.5+(i%5)*.25);
                double x=Math.cos(a)*r*.65,z=Math.sin(a)*r*.65;
                g.line(x,y,z,x,y+.18+age*.22,z,.012,i*.3);
                g.shard(a,y,r*.65,.05);
            }
        }else{
            // A hot ground fissure with jagged rays and rising embers.
            g.forcedColor=0xFF4A16;
            double radius=r*(.6+Math.sin(age*Math.PI)*.9);
            g.ring(.04,radius,0,age*4,.025);
            g.ring(.07,radius*.65,0,-age*5,.012);
            for(int i=0;i<12;i++){
                double a=i*Math.PI/6+age*.25,b=a+.1;
                double x=Math.cos(a)*radius,z=Math.sin(a)*radius;
                double X=Math.cos(b)*radius*1.35,Z=Math.sin(b)*radius*1.35;
                g.line(x,.03,z,X,.03,Z,.018,i*.2);
                g.line(X,.03,Z,Math.cos(a)*radius*1.6,.03,Math.sin(a)*radius*1.6,.009,i*.2);
                double y=Math.sin(age*Math.PI)*(h+.4)*(i%2==0?1:.65);
                g.line(x,.08,z,x*.65,y,z*.65,.018,i*.2);
            }
            for(int i=0;i<g.c.shards;i++){
                double a=i*2.39996-age*2;
                g.shard(a,.1+age*(1+(i%7)*.2),radius*(.5+(i%3)*.15),.055*(1-age*.5));
            }
            g.forcedColor=-1;
        }
    }
}
