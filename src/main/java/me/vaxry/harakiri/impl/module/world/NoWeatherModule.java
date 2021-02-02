package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.api.event.world.EventRainStrength;
import me.vaxry.harakiri.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/14/2019 @ 1:45 AM.
 */
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
