package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventPlayerDamageBlock;
import me.vaxry.harakiri.framework.event.render.EventHurtCamEffect;
import me.vaxry.harakiri.framework.Module;


public final class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", new String[]{"AntiHurtCam"}, "Removes annoying hurt camera effects.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventHurtCamEffect> onhurtcam = new Attender<>(EventHurtCamEffect.class, event -> {
        event.setCanceled(true);
    });

}
