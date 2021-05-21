package me.vaxry.harakiri.impl.manager;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.camera.Camera;
import me.vaxry.harakiri.framework.camera.Camera2;
import me.vaxry.harakiri.framework.event.minecraft.EventUpdateFramebufferSize;
import me.vaxry.harakiri.framework.event.player.EventFovModifier;
import me.vaxry.harakiri.framework.event.render.*;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CameraManager {

    private List<Camera> cameraList = new ArrayList();
    private List<Camera2> camera2List = new ArrayList();

    public CameraManager() {
        Harakiri.get().getEventManager().registerAttender(this);
        Harakiri.get().getEventManager().build();
        Harakiri.get().getEventManager().setAttending(this, true);
    }

    public void update() {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && !cam.isRecording() && cam.isRendering()) {
                    cam.updateFbo();
                }
            }
            for (Camera2 cam : this.camera2List) {
                if (cam != null && !cam.isRecording() && cam.isRendering()) {
                    cam.updateFbo();
                }
            }
        }
    }

    Attender<EventRenderOverlay> onRenderOverlay = new Attender<>(EventRenderOverlay.class, event -> {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    });

    Attender<EventUpdateFramebufferSize> onFBOResize = new Attender<>(EventUpdateFramebufferSize.class, event -> {
        for (Camera cam : this.cameraList) {
            if (cam != null) {
                cam.resize();
            }
        }

        for (Camera2 cam : this.camera2List) {
            if (cam != null) {
                cam.resize();
            }
        }
    });

    Attender<EventFovModifier> fovModifierAttender = new Attender<>(EventFovModifier.class, event -> {
        if (this.isCameraRecording()) {
            event.setFov(90.0f);
            event.setCanceled(true);
        }
    });

    Attender<EventRenderEntityOutlines> onRenderEntityOutlines = new Attender<>(EventRenderEntityOutlines.class, event -> {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    });

    Attender<EventHurtCamEffect> onHurtCamEffect = new Attender<>(EventHurtCamEffect.class, event -> {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    });

    Attender<EventRenderSky> onRenderSky = new Attender<>(EventRenderSky.class, event -> {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    });

    Attender<EventRenderBlockDamage> onRenderBlockDamage = new Attender<>(EventRenderBlockDamage.class, event -> {
        if (this.isCameraRecording()) {
            event.setCanceled(true);
        }
    });

    public void addCamera(Camera cam) {
        this.cameraList.add(cam);
    }

    public void addCamera2(Camera2 cam) {
        this.camera2List.add(cam);
    }

    public void unload() {
        this.cameraList.clear();
        Harakiri.get().getEventManager().unregisterAttender(this);
    }

    public boolean isCameraRecording() {
        if (Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().currentScreen == null) {
            for (Camera cam : this.cameraList) {
                if (cam != null && cam.isRecording()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Camera> getCameraList() {
        return cameraList;
    }

    public void setCameraList(List<Camera> cameraList) {
        this.cameraList = cameraList;
    }
}
