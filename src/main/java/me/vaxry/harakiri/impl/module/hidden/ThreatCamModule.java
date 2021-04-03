package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class ThreatCamModule extends Module {
    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Distance", "dist", "d"}, "Sets the distance of the Third-Person camera.", 1.2f, 0.5f, 5.0f, 1.0f);
    public final Value<Boolean> optimized = new Value<Boolean>("Optimized", new String[]{"Optimized", "opti", "o"}, "Enables the optimized mode", false);
    public boolean yes = false;

    public ThreatCamModule() {
        super("ThreatCam", new String[]{"ThreatCam", "threatcam", "tc"}, "Values for ThreatCam", "NONE", -1, Module.ModuleType.HIDDEN);
        this.setHidden(true);
    }
}
