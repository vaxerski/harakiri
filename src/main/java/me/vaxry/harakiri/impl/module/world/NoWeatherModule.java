package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.world.EventRainStrength;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoWeatherModule extends Module {

    public NoWeatherModule() {
        super("NoWeather", new String[]{"AntiWeather"}, "Allows you to change the weather clientside.", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onRainStrength(EventRainStrength event) {
        if (Minecraft.getMinecraft().world == null)
            return;

        event.setCanceled(true);
    }

}
