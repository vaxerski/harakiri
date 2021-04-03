package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.event.player.EventApplyCollision;
import me.vaxry.harakiri.framework.event.player.EventPushOutOfBlocks;
import me.vaxry.harakiri.framework.event.player.EventPushedByWater;
import me.vaxry.harakiri.framework.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoPushModule extends Module {

    public NoPushModule() {
        super("NoPush", new String[]{"AntiPush"}, "Disable collision.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void pushOutOfBlocks(EventPushOutOfBlocks event) {
        event.setCanceled(true);
    }

    @Listener
    public void pushedByWater(EventPushedByWater event) {
        event.setCanceled(true);
    }

    @Listener
    public void applyCollision(EventApplyCollision event) {
        event.setCanceled(true);
    }

}
