package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.api.event.player.EventSendChatMessage;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/16/2019 @ 8:44 AM.
 */
public final class CommandsModule extends Module {

    public final Value<String> prefix = new Value("Prefix", new String[]{"prefx", "pfx"}, "The command prefix.", ".");

    public CommandsModule() {
        super("Commands", new String[]{"cmds", "cmd"}, "Allows you to execute client commands", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        if (this.prefix.getValue().length() == 1) {
            final char key = Keyboard.getEventCharacter();
            if (this.prefix.getValue().charAt(0) == key) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
            }
        }
    }

    @Listener
    public void sendChatMessage(EventSendChatMessage event) {
        if (event.getMessage().startsWith(this.prefix.getValue())) {
            final String input = event.getMessage().substring(this.prefix.getValue().length());
            final String[] split = input.split(" ");

            final Command command = Harakiri.INSTANCE.getCommandManager().find(split[0]);

            if (command != null) {
                try {
                    command.exec(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    Harakiri.INSTANCE.errorChat("Error while running command");
                }
            } else {
                Harakiri.INSTANCE.errorChat("Unknown command " + "\247f\"" + event.getMessage() + "\"");
                final Command similar = Harakiri.INSTANCE.getCommandManager().findSimilar(split[0]);

                if (similar != null) {
                    Harakiri.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }

            event.setCanceled(true);
        }
    }

    public Value<String> getPrefix() {
        return prefix;
    }
}
