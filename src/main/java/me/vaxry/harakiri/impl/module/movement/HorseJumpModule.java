package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class HorseJumpModule extends Module {

    public HorseJumpModule() {
        super("HorseJump", new String[]{"JumpPower", "HJump"}, "Makes horses and llamas jump the highest.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().player.horseJumpPower = 1;
            Minecraft.getMinecraft().player.horseJumpPowerCounter = -10;
        }
    }

}
