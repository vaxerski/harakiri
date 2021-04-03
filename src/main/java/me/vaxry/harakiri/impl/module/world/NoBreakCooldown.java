package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoBreakCooldown extends Module {

    public NoBreakCooldown() {
        super("NoBreakCooldown", new String[]{"NoBreakCooldown"}, "Removes break delay.", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().playerController.blockHitDelay = 0;
        }
    }

}
