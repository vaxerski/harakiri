package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Author Seth
 * 5/23/2019 @ 8:11 AM.
 */
public final class IPCommand extends Command {

    public IPCommand() {
        super("IP", new String[]{"IPAddress"}, "Copies the current server ip to your clipboard", "IP");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.getCurrentServerData() != null) {
            final StringSelection contents = new StringSelection(mc.getCurrentServerData().serverIP);
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(contents, null);
            Harakiri.INSTANCE.logChat("Copied IP to clipboard");
        } else {
            Harakiri.INSTANCE.errorChat("Error, Join a server");
        }
    }
}
