package dev.visualfx;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.TitleScreen;

/** Development-only integration probe, not packaged in VisualFX. */
public final class SoundCloudUiSmoke implements ClientModInitializer {
 private int wait,frames,reopens;
 @Override public void onInitializeClient(){
  ClientTickEvents.END_CLIENT_TICK.register(c->{
   if(c.gui.screen() instanceof TitleScreen){
    if(++wait>=40){
     if(SoundCloudPlayer.INSTANCE.status()==null)throw new AssertionError("Null status on first open");
     c.gui.setScreen(new SoundCloudScreen(c.gui.screen()));frames=0;
     VisualFX.LOG.info("UI_SMOKE_OPEN_SOUNDCloud {}",reopens);
    }
   }else if(c.gui.screen() instanceof SoundCloudScreen screen){
    if(++frames==80){
     VisualFX.LOG.info("UI_SMOKE_SCREEN_SURVIVED {} ticks; connected={}",frames,SoundCloudPlayer.INSTANCE.connected());
     if(reopens++==0){screen.onClose();wait=39;}
     else{VisualFX.LOG.info("UI_SMOKE_PASS: first open and reopen without browser or track");c.stop();}
    }
   }
  });
 }
}
