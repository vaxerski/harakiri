package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.framework.event.player.EventHandActive;
import me.vaxry.harakiri.framework.event.player.EventHittingBlock;
import me.vaxry.harakiri.framework.event.player.EventResetBlockRemoving;
import me.vaxry.harakiri.framework.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class MultitaskModule extends Module {

    public MultitaskModule() {
        super("Multitask", new String[]{"multi", "task"}, "Allows you to perform multiple actions at once.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onActiveHand(EventHandActive event) {
        event.setCanceled(true);
    }

    @Listener
    public void onHittingBlock(EventHittingBlock event) {
        event.setCanceled(true);
    }

    @Listener
    public void onResetBlockRemoving(EventResetBlockRemoving event) {
        event.setCanceled(true);
    }
}
