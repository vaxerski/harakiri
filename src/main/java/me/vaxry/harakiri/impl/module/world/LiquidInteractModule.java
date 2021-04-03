package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.world.EventCanCollide;
import me.vaxry.harakiri.framework.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class LiquidInteractModule extends Module {

    public LiquidInteractModule() {
        super("LiquidInteract", new String[]{"LiquidInt", "LiqInt"}, "Allows you to interact with liquids. (Place blocks etc)", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void canCollide(EventCanCollide event) {
        event.setCanceled(true);
    }

}
