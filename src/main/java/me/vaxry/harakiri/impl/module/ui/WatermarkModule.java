package me.vaxry.harakiri.impl.module.ui;

import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;


public class WatermarkModule extends Module {
    public final Value<Integer> Xoff = new Value<Integer>("Xoff", new String[]{"Xoff", "x"}, "Xoff", 30, 0, 150, 1);

    private boolean on = false;


    public WatermarkModule() {
        super("Watermark", new String[]{"Watermark", "wm"}, "Watermark settings", "NONE", -1, ModuleType.UI);
        this.setHidden(true);

    }

    public void setWMOnState(boolean o) {
        this.on = o;
    }

}
