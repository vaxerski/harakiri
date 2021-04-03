package me.vaxry.harakiri.framework.event.command;

import me.vaxry.harakiri.framework.Command;

/**
 * Author Seth
 * 6/10/2019 @ 2:37 PM.
 */
public class EventCommandLoad {

    private Command command;

    public EventCommandLoad(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
