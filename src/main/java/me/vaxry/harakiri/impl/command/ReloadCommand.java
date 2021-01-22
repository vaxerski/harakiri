package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;

/**
 * Author Seth
 * 5/12/2019 @ 9:10 AM.
 */
public final class ReloadCommand extends Command {

    public ReloadCommand() {
        super("Reload", new String[]{"Rload"}, "Reloads the client", "Reload");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Harakiri.INSTANCE.reload();
        Harakiri.INSTANCE.logChat("Client Reloaded");
    }
}
