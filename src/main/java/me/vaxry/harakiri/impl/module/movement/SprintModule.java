package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;


public final class SprintModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The sprint mode to use.", Mode.RAGE);

    private enum Mode {
        RAGE, LEGIT
    }

    public SprintModule() {
        super("Sprint", new String[]{"AutoSprint", "Spr"}, "Automatically sprints for you.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (Minecraft.getMinecraft().world != null) {
            Minecraft.getMinecraft().player.setSprinting(false);
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            switch (this.mode.getValue()) {
                case RAGE:
                    if ((mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) && !(mc.player.isSneaking()) && !(mc.player.collidedHorizontally) && !(mc.player.getFoodStats().getFoodLevel() <= 6f)) {
                        mc.player.setSprinting(true);
                    }
                    break;
                case LEGIT:
                    if ((mc.gameSettings.keyBindForward.isKeyDown()) && !(mc.player.isSneaking()) && !(mc.player.isHandActive()) && !(mc.player.collidedHorizontally) && mc.currentScreen == null && !(mc.player.getFoodStats().getFoodLevel() <= 6f)) {
                        mc.player.setSprinting(true);
                    }
                    break;
            }
        }
    }

}
