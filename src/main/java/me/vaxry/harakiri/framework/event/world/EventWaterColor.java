package me.vaxry.harakiri.framework.event.world;

import me.vaxry.harakiri.framework.event.EventCancellable;

/**
 * Author Seth
 * 8/11/2019 @ 2:44 AM.
 */
public class EventWaterColor extends EventCancellable {

    private int color;

    public EventWaterColor() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
