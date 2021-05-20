package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.render.EventHurtCamEffect;
import me.vaxry.harakiri.framework.Module;


public final class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", new String[]{"AntiHurtCam"}, "Removes annoying hurt camera effects.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void hurtCamEffect(EventHurtCamEffect event) {
        event.setCanceled(true);
    }

}
