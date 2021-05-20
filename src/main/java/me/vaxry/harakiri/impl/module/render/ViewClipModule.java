package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.render.EventOrientCamera;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.render.EventRender3D;


public final class ViewClipModule extends Module {

    public ViewClipModule() {
        super("ThirdPersonClip", new String[]{"ViewC"}, "Prevents the F5 camera from clipping.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventOrientCamera> onorient = new Attender<>(EventOrientCamera.class, event -> {
        event.setCanceled(true);
    });

}
