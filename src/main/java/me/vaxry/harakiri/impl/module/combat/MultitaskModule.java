package me.vaxry.harakiri.impl.module.combat;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventHandActive;
import me.vaxry.harakiri.framework.event.player.EventHittingBlock;
import me.vaxry.harakiri.framework.event.player.EventResetBlockRemoving;
import me.vaxry.harakiri.framework.Module;


public final class MultitaskModule extends Module {

    public MultitaskModule() {
        super("Multitask", new String[]{"multi", "task"}, "Allows you to perform multiple actions at once.", "NONE", -1, ModuleType.COMBAT);
    }

    Attender<EventHandActive> onActiveHand = new Attender<>(EventHandActive.class, event-> event.setCanceled(true));
    Attender<EventHittingBlock> onHittingBlock = new Attender<>(EventHittingBlock.class, event-> event.setCanceled(true));
    Attender<EventResetBlockRemoving> onResetBlockRemoving = new Attender<>(EventResetBlockRemoving.class, event -> event.setCanceled(true));
}
