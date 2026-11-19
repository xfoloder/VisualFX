package dev.visualfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.CloudStatus;
import net.minecraft.server.level.ParticleStatus;

/** Explicit, reversible presets for existing game settings and a bounded VisualFX budget. */
final class Optimizer {
    static final String[] NAMES={"Баланс","Больше FPS","Минимум нагрузки"};
    static final String[] DETAILS={
        "Прорисовка до 12 чанков, симуляция до 8, облака выкл., меньше частиц. До 24 осколков VisualFX.",
        "Прорисовка до 8 чанков, симуляция до 5, облака и мягкий свет выкл., минимум частиц. До 12 осколков VisualFX.",
        "Прорисовка и симуляция до 5 чанков, облака и мягкий свет выкл., минимум частиц. До 6 осколков VisualFX."
    };
    static final class Snapshot {
        public int renderDistance,simulationDistance;
        public double entityDistance;
        public String clouds,particles;
        public boolean ambientOcclusion;
        static Snapshot capture(Options o){
            Snapshot s=new Snapshot();s.renderDistance=o.renderDistance().get();s.simulationDistance=o.simulationDistance().get();
            s.entityDistance=o.entityDistanceScaling().get();s.clouds=o.cloudStatus().get().name();
            s.particles=o.particles().get().name();s.ambientOcclusion=o.ambientOcclusion().get();return s;
        }
    }
    static String apply(){
        Minecraft mc=Minecraft.getInstance();Options o=mc.options;Config c=VisualFX.config;
        if(c.optimizationBackup==null){
            c.optimizationBackup=Snapshot.capture(o);
            if(!c.saveChecked()){c.optimizationBackup=null;return "Не удалось сохранить копию. Настройки игры не изменены.";}
        }
        int p=c.optimizationPreset;
        o.renderDistance().set(Math.min(o.renderDistance().get(),p==0?12:p==1?8:5));
        o.simulationDistance().set(Math.min(o.simulationDistance().get(),p==0?8:5));
        o.entityDistanceScaling().set(Math.min(o.entityDistanceScaling().get(),p==0?.75:.5));
        o.cloudStatus().set(CloudStatus.OFF);
        ParticleStatus desired=p==0?ParticleStatus.DECREASED:ParticleStatus.MINIMAL;
        if(o.particles().get().ordinal()<desired.ordinal())o.particles().set(desired);
        if(p>0)o.ambientOcclusion().set(false);
        c.optimization=true;c.save();o.save();o.broadcastOptions();
        return "Пресет применён. Прежние значения сохранены.";
    }
    static String restore(){
        Config c=VisualFX.config;Snapshot s=c.optimizationBackup;
        if(s==null){c.optimization=false;c.save();return "Сохранённых настроек Minecraft нет.";}
        // Validate persisted data before mutating any option.
        final CloudStatus clouds;final ParticleStatus particles;
        try{
            clouds=CloudStatus.valueOf(s.clouds);particles=ParticleStatus.valueOf(s.particles);
            if(s.renderDistance<2||s.renderDistance>64||s.simulationDistance<2||s.simulationDistance>64||!Double.isFinite(s.entityDistance)||s.entityDistance<.5||s.entityDistance>5)
                return "Резервная копия повреждена. Настройки не изменены.";
        }catch(RuntimeException e){return "Резервная копия повреждена. Настройки не изменены.";}
        Options o=Minecraft.getInstance().options;
        o.renderDistance().set(s.renderDistance);o.simulationDistance().set(s.simulationDistance);
        o.entityDistanceScaling().set(s.entityDistance);o.cloudStatus().set(clouds);
        o.particles().set(particles);o.ambientOcclusion().set(s.ambientOcclusion);
        o.save();o.broadcastOptions();c.optimizationBackup=null;c.optimization=false;c.save();
        return "Исходные настройки Minecraft восстановлены.";
    }
}
