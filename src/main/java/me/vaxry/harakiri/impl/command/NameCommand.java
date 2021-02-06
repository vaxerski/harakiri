package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

/**
 * Author Seth
 * 5/4/2019 @ 3:09 AM.
 */
public final class NameCommand extends Command {

    public NameCommand() {
        super("Name", new String[]{"Nam"}, "Allows you to change the case of your name", "Name <Username>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (split[1].equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
            Minecraft.getMinecraft().session = new Session(split[1], Minecraft.getMinecraft().session.getPlayerID(), Minecraft.getMinecraft().session.getToken(), "mojang");
            Harakiri.INSTANCE.logChat("Set username to " + split[1]);
        } else {
            Harakiri.INSTANCE.errorChat("Name must match");
        }
    }
}
