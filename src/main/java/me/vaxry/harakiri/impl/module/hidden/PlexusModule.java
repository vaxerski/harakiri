package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class PlexusModule extends Module{

    public final Value<Integer> PARTICLE_MAX_OFFSCREEN = new Value<Integer>("PARTICLE_MAX_OFFSCREEN", new String[]{"PARTICLE_MAX_OFFSCREEN"}, "PARTICLE_MAX_OFFSCREEN", 50, 0, 2500, 1);
    public final Value<Float> PARTICLE_SPEED_MIN = new Value<Float>("PARTICLE_SPEED_MIN", new String[]{"PARTICLE_SPEED_MIN"}, "PARTICLE_SPEED_MIN", 20.f, 0.f, 2500.f, 1.0f);
    public final Value<Float> PARTICLE_SPEED_MAX = new Value<Float>("PARTICLE_SPEED_MAX", new String[]{"PARTICLE_SPEED_MAX"}, "PARTICLE_SPEED_MAX", 60.f, 0.f, 2500.f, 1.0f);
    public final Value<Integer> PARTICLE_NUM = new Value<Integer>("PARTICLE_NUM", new String[]{"PARTICLE_NUM"}, "PARTICLE_NUM", 100, 0, 2500, 1);
    public final Value<Integer> LINE_MAX_DIST = new Value<Integer>("LINE_MAX_DIST", new String[]{"LINE_MAX_DIST"}, "LINE_MAX_DIST", 200, 0, 2500, 1);
    public final Value<Integer> LINE_MAX_ALPHA = new Value<Integer>("LINE_MAX_ALPHA", new String[]{"LINE_MAX_ALPHA"}, "LINE_MAX_ALPHA", 75, 0, 255, 1);
    public final Value<Float> PARTICLE_SIZE_SCALE = new Value<Float>("PARTICLE_SIZE_SCALE", new String[]{"PARTICLE_SIZE_SCALE"}, "PARTICLE_SIZE_SCALE", 0.5f, 0.f, 20.f, 1.f);
    public final Value<Integer> RAINBOW_SIZE = new Value<Integer>("RAINBOW_SIZE", new String[]{"RAINBOW_SIZE"}, "RAINBOW_SIZE", 200, 0, 2000, 1);
    public final Value<Float> CLICK_FORCE = new Value<Float>("CLICK_FORCE", new String[]{"CLICK_FORCE"}, "CLICK_FORCE", 0.5f, 0.f, 20.f, 1.f);
    public final Value<Integer> CLICK_MAX_RANGE = new Value<Integer>("CLICK_MAX_RANGE", new String[]{"CLICK_MAX_RANGE"}, "CLICK_MAX_RANGE", 50, 0, 2000, 1);


    public PlexusModule() {
        super("Plexus", new String[]{"Plexus", "plexus"}, "Values for plexus", "NONE", -1, Module.ModuleType.HIDDEN);
        this.setHidden(true);
    }
}
