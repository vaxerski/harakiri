package me.vaxry.harakiri.framework.event.player;

import me.vaxry.harakiri.framework.event.EventCancellable;

/**
 * Author Seth
 * 4/8/2019 @ 3:50 AM.
 */
public class EventUpdateWalkingPlayer extends EventCancellable {

    public EventUpdateWalkingPlayer(EventStage stage) {
        super(stage);
    }

}
