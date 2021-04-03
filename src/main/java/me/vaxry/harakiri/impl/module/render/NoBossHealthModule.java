package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.render.EventRenderBossHealth;
import me.vaxry.harakiri.framework.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoBossHealthModule extends Module {

    public NoBossHealthModule() {
        super("NoBossHealth", new String[]{"NoBossHealthBar", "NoBossBar"}, "Disables the rendering of the boss health.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRenderBossHealth(EventRenderBossHealth event) {
        event.setCanceled(true);
    }
}
