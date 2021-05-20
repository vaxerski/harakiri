package me.vaxry.harakiri.impl.module.world;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.world.EventRainStrength;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;


public final class NoWeatherModule extends Module {

    public NoWeatherModule() {
        super("NoWeather", new String[]{"AntiWeather"}, "Allows you to change the weather clientside.", "NONE", -1, ModuleType.WORLD);
    }

    Attender<EventRainStrength> onRainStrength = new Attender<>(EventRainStrength.class, event -> {
        if (Minecraft.getMinecraft().world == null)
            return;

        event.setCanceled(true);
    });
}
