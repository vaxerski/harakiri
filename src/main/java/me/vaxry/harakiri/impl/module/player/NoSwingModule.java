package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.api.event.player.EventSwingArm;
import me.vaxry.harakiri.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/20/2019 @ 9:12 AM.
 */
public final class NoSwingModule extends Module {

    public NoSwingModule() {
        super("NoSwing", new String[]{"AntiSwing"}, "Prevents swinging server-side.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void swingArm(EventSwingArm event) {
        event.setCanceled(true);
    }

}
