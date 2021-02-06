package me.vaxry.harakiri.framework.event.render;

import me.vaxry.harakiri.framework.event.EventCancellable;

/**
 * Author Seth
 * 4/9/2019 @ 12:33 AM.
 */
public class EventRenderOverlay extends EventCancellable {

    private OverlayType type;

    public EventRenderOverlay(OverlayType type) {
        this.type = type;
    }

    public OverlayType getType() {
        return type;
    }

    public void setType(OverlayType type) {
        this.type = type;
    }

    public enum OverlayType {
        BLOCK,
        LIQUID,
        FIRE
    }

}
