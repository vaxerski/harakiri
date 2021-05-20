package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventSwingArm;
import me.vaxry.harakiri.framework.Module;


public final class NoSwingModule extends Module {

    public NoSwingModule() {
        super("NoSwing", new String[]{"AntiSwing"}, "Prevents swinging server-side.", "NONE", -1, ModuleType.PLAYER);
    }

    Attender<EventSwingArm> onArmSwing = new Attender<>(EventSwingArm.class, event -> event.setCanceled(true));
}
