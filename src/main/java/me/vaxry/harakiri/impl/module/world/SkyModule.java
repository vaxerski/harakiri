package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class SkyModule extends Module {

    public long worldtime = 0;

    //public final Value<Boolean> use = new Value<Boolean>("CelestialAngle", new String[]{"CelestialAngle", "ca"}, "Change the time of day.", false);
    public final Value<Integer> celestialAng = new Value<Integer>("CelestialAngle", new String[]{"CelestialAngle", "ca"}, "Change the time of day.", 0, 0, 24000, 1);

    public SkyModule() {
        super("Sky", new String[]{"Sky"}, "Change the sky behavior.", "NONE", -1, Module.ModuleType.WORLD);
    }


    @Listener
    public void onRender3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

    }
}
