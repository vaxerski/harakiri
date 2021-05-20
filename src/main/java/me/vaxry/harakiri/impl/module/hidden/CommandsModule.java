package me.vaxry.harakiri.impl.module.hidden;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.framework.event.player.EventSendChatMessage;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import org.lwjgl.input.Keyboard;


public final class CommandsModule extends Module {

    private boolean once = false;
    public final Value<String> prefix = new Value("Prefix", new String[]{"prefx", "pfx"}, "The command prefix.", ".");

    public CommandsModule() {
        super("Commands", new String[]{"cmds", "cmd"}, "Allows you to execute client commands", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.setEnabled(true);
        this.onEnable();
    }

    Attender<EventKeyPress> onKeyPress = new Attender<>(EventKeyPress.class, event -> {
        if (this.prefix.getValue().length() == 1) {
            final char key = Keyboard.getEventCharacter();
            if (this.prefix.getValue().charAt(0) == key) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiChat());
            }
        }
    });

    Attender<EventSendChatMessage> onSendChatMessage = new Attender<>(EventSendChatMessage.class, event -> {
        if (event.getMessage().startsWith(this.prefix.getValue())) {
            final String input = event.getMessage().substring(this.prefix.getValue().length());
            final String[] split = input.split(" ");

            final Command command = Harakiri.get().getCommandManager().find(split[0]);

            if (command != null) {
                try {
                    command.run(input);
                } catch (Exception e) {
                    e.printStackTrace();
                    Harakiri.get().errorChat("Error while running command");
                }
            } else {
                Harakiri.get().errorChat("Unknown command " + "\247f\"" + event.getMessage() + "\"");
                final Command similar = Harakiri.get().getCommandManager().findSimilar(split[0]);

                if (similar != null) {
                    Harakiri.get().logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }

            event.setCanceled(true);
        }
    });

    Attender<EventDisplayGui> onDisplayGUI = new Attender<>(EventDisplayGui.class, event -> {
        if(!once){
            if(Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu){
                // Send a MSG
                Harakiri.get().getApiManager().mex.writeFile("HaraMenu");
                once = true;
            }
        }
    });

    public Value<String> getPrefix() {
        return prefix;
    }
}
