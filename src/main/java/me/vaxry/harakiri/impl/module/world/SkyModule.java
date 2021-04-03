package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class SkyModule extends Module {

    public long worldtime = 0;
    public Timer timerWorld = new Timer();

    //public final Value<Boolean> use = new Value<Boolean>("CelestialAngle", new String[]{"CelestialAngle", "ca"}, "Change the time of day.", false);
    public final Value<Integer> celestialAng = new Value<Integer>("CelestialAngle", new String[]{"CelestialAngle", "ca"}, "Change the time of day.", 0, 0, 24000, 1);

    public SkyModule() {
        super("Sky", new String[]{"Sky"}, "Change the sky behavior.", "NONE", -1, Module.ModuleType.WORLD);
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        this.timerWorld.reset();
    }

}
