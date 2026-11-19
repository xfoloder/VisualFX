package dev.visualfx;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.TitleScreen;
public final class EchoUiSmoke implements ClientModInitializer {
 private int wait,ticks;
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(c->{
  if(c.gui.screen() instanceof TitleScreen&&++wait==40){c.gui.setScreen(new TerminalScreen());VisualFX.LOG.info("ECHO_UI_OPEN");}
  else if(c.gui.screen() instanceof TerminalScreen&&++ticks==80){VisualFX.LOG.info("ECHO_UI_PASS: default Neon Echo tab rendered for 80 ticks");c.stop();}
 });}
}
