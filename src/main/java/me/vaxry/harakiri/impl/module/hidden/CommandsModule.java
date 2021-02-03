package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.api.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.api.event.player.EventSendChatMessage;
import me.vaxry.harakiri.api.extd.MiscExtd;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import org.lwjgl.input.Keyboard;
import scala.collection.parallel.ParIterableLike;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;

/**
 * Author Seth
 * 4/16/2019 @ 8:44 AM.
 */
public final class CommandsModule extends Module {

    private boolean once = false;
    public final Value<String> prefix = new Value("Prefix", new String[]{"prefx", "pfx"}, "The command prefix.", ".");

    public CommandsModule() {
        super("Commands", new String[]{"cmds", "cmd"}, "Allows you to execute client commands", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.setEnabled(true);
        this.onEnable();
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

    @Listener
    public void displayScreen(EventDisplayGui event) {
        if(!once){
            if(Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu){
                // Send a MSG
                Harakiri.INSTANCE.getApiManager().mex.writeFile("HaraMenu");
                once = true;
            }
        }
    }

    public Value<String> getPrefix() {
        return prefix;
    }
}
