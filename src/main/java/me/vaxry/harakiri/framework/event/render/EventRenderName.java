package me.vaxry.harakiri.framework.event.render;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.entity.EntityLivingBase;

/**
 * Author Seth
 * 4/9/2019 @ 11:01 AM.
 */
public class EventRenderName extends EventCancellable {

    private EntityLivingBase entity;

    public EventRenderName(EntityLivingBase entity) {
        this.entity = entity;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public void setEntity(EntityLivingBase entity) {
        this.entity = entity;
    }
}
