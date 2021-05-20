package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventApplyCollision;
import me.vaxry.harakiri.framework.event.player.EventPushOutOfBlocks;
import me.vaxry.harakiri.framework.event.player.EventPushedByWater;
import me.vaxry.harakiri.framework.Module;


public final class NoPushModule extends Module {

    public NoPushModule() {
        super("NoPush", new String[]{"AntiPush"}, "Disable collision.", "NONE", -1, ModuleType.PLAYER);
    }

    Attender<EventApplyCollision> onApplyCollision = new Attender<>(EventApplyCollision.class, event->event.setCanceled(true));
    Attender<EventPushedByWater> onPushedByWater = new Attender<>(EventPushedByWater.class, event -> event.setCanceled(true));
    Attender<EventPushOutOfBlocks> onPushedOutOfBlocks = new Attender<>(EventPushOutOfBlocks.class, event -> event.setCanceled(true));

}
