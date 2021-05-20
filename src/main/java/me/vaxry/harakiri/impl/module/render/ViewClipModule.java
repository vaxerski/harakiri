package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.render.EventOrientCamera;
import me.vaxry.harakiri.framework.Module;


public final class ViewClipModule extends Module {

    public ViewClipModule() {
        super("ThirdPersonClip", new String[]{"ViewC"}, "Prevents the F5 camera from clipping.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void orientCamera(EventOrientCamera event) {
        event.setCanceled(true);
    }

}
