package me.vaxry.harakiri.api.event.player;

import me.vaxry.harakiri.api.event.EventCancellable;

/**
 * Author Seth
 * 4/8/2019 @ 3:50 AM.
 */
public class EventUpdateWalkingPlayer extends EventCancellable {

    public EventUpdateWalkingPlayer(EventStage stage) {
        super(stage);
    }

}
