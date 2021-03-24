package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemExpBottle;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/23/2019 @ 12:58 PM.
 */
public final class NoBreakCooldown extends Module {

    public NoBreakCooldown() {
        super("NoBreakCooldown", new String[]{"NoBreakCooldown"}, "Removes break delay.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().rightClickDelayTimer = 6;
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().playerController.blockHitDelay = 0;
        }
    }

}
