package me.vaxry.harakiri.impl.module.movement;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import net.minecraft.client.Minecraft;


public final class HorseJumpModule extends Module {

    public HorseJumpModule() {
        super("HorseJump", new String[]{"JumpPower", "HJump"}, "Makes horses and llamas jump the highest.", "NONE", -1, ModuleType.MOVEMENT);
    }

    Attender<EventPlayerUpdate> onUpdatePlayer = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().player.horseJumpPower = 1;
            Minecraft.getMinecraft().player.horseJumpPowerCounter = -10;
        }
    });
}
