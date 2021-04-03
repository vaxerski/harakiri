package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.framework.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 8/1/2019 @ 7:22 PM.
 */
public final class FakeChatCommand extends Command {

    public FakeChatCommand() {
        super("FakeChat", new String[]{"FChat", "TellRaw"}, "Sends a fake chat message.", "FakeChat <message>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final StringBuilder sb = new StringBuilder();

        for (int i = 1; i < split.length; i++) {
            final String s = split[i];
            sb.append(s + (i == split.length - 1 ? "" : " "));
        }

        final String message = sb.toString();
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message.replace("&", "\247")));
    }
}
