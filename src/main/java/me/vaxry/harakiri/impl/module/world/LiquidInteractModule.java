package me.vaxry.harakiri.impl.module.world;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.world.EventCanCollide;
import me.vaxry.harakiri.framework.Module;


public final class LiquidInteractModule extends Module {

    public LiquidInteractModule() {
        super("LiquidInteract", new String[]{"LiquidInt", "LiqInt"}, "Allows you to interact with liquids. (Place blocks etc)", "NONE", -1, ModuleType.WORLD);
    }

    Attender<EventCanCollide> onCanCollide = new Attender<>(EventCanCollide.class, event -> event.setCanceled(true));
}
