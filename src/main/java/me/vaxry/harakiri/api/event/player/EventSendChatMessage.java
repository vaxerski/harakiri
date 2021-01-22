package me.vaxry.harakiri.api.event.player;

import me.vaxry.harakiri.api.event.EventCancellable;

/**
 * Author Seth
 * 4/8/2019 @ 3:57 AM.
 */
public class EventSendChatMessage extends EventCancellable {

    private String message;

    public EventSendChatMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
