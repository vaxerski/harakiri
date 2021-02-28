package me.vaxry.harakiri.framework.event.render;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;


public class EventRenderEntities extends EventCancellable {

    private Entity renderviewentity;
    private ICamera camera;
    private float partialTicks;

    public EventRenderEntities(EventStage stage, Entity entity, ICamera camera, float partialTicks) {
        super(stage);
        this.camera = camera;
        this.partialTicks = partialTicks;
        this.renderviewentity = entity;
    }

    public Entity getRenderviewentity() {
        return renderviewentity;
    }

    public void setRenderviewentity(Entity renderviewentity) {
        this.renderviewentity = renderviewentity;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public ICamera getCamera() {
        return camera;
    }

    public void setCamera(ICamera camera) {
        this.camera = camera;
    }
}
