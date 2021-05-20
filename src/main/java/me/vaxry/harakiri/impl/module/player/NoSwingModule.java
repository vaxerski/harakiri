package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.event.player.EventSwingArm;
import me.vaxry.harakiri.framework.Module;


public final class NoSwingModule extends Module {

    public NoSwingModule() {
        super("NoSwing", new String[]{"AntiSwing"}, "Prevents swinging server-side.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void swingArm(EventSwingArm event) {
        event.setCanceled(true);
    }

}
