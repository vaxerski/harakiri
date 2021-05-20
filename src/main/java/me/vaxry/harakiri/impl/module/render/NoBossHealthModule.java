package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.client.EventSaveConfig;
import me.vaxry.harakiri.framework.event.render.EventRenderBossHealth;
import me.vaxry.harakiri.framework.Module;


public final class NoBossHealthModule extends Module {

    public NoBossHealthModule() {
        super("NoBossHealth", new String[]{"NoBossHealthBar", "NoBossBar"}, "Disables the rendering of the boss health.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventRenderBossHealth> onrenderboss = new Attender<>(EventRenderBossHealth.class, event -> {
        event.setCanceled(true);
    });
}
