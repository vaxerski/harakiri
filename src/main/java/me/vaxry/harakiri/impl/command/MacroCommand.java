package me.vaxry.harakiri.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.Macro;
import me.vaxry.harakiri.impl.config.MacroConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

public final class MacroCommand extends Command {

    public MacroCommand() {
        super("Macro", new String[]{"Mac"}, "Allows you to create macros", "Macro Add <name> <key> <macro>\n" +
                "Macro Remove <name>\n" +
                "Macro List\n" +
                "Macro Clear");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] args = input.split(" ");

        if(args[1].equalsIgnoreCase("add")){
            if(args.length < 5) {
                Harakiri.get().logChat(ChatFormatting.GRAY + "Not enough input! Expected at least " + ChatFormatting.RESET + "5" + ChatFormatting.GRAY + ", got " + ChatFormatting.RESET + args.length + ChatFormatting.GRAY + ".");
                return;
            }

            final String name = args[2];
            final String key = args[3];

            if(Harakiri.get().getMacroManager().find(name) != null){
                Harakiri.get().logChat(ChatFormatting.RED + name + ChatFormatting.GRAY + " is already an existing macro's name!");
                return;
            }

            final int keycode = Keyboard.getKeyIndex(key.toUpperCase());
            if(keycode != Keyboard.KEY_NONE){
                final String command = coerceArgsToSpacedString(args, 4, -1);
                Harakiri.get().logChat(ChatFormatting.GREEN + "Added " + ChatFormatting.GRAY + "a new macro: " + ChatFormatting.RESET + name + ChatFormatting.GRAY + " -> " + ChatFormatting.GRAY + command);
                Harakiri.get().getMacroManager().getMacroList().add(new Macro(name, key.toUpperCase(), command));
                Harakiri.get().getConfigManager().saveAll();
            }else{
                Harakiri.get().logChat(ChatFormatting.GRAY + "Invalid key: " + ChatFormatting.RESET + key);
            }
        } else if(args[1].equalsIgnoreCase("remove")) {
            if(args.length < 4) {
                Harakiri.get().logChat(ChatFormatting.GRAY + "Not enough input! Expected at least " + ChatFormatting.RESET + "4" + ChatFormatting.GRAY + ", got " + ChatFormatting.RESET + args.length + ChatFormatting.GRAY + ".");
                return;
            }
            final String name = args[2];

            Macro macro = Harakiri.get().getMacroManager().find(name);

            if(macro == null){
                Harakiri.get().logChat(ChatFormatting.GRAY + "Macro named " + ChatFormatting.RESET + name + ChatFormatting.GRAY + " was not found.");
                return;
            }

            Harakiri.get().getMacroManager().getMacroList().remove(macro);
            Harakiri.get().logChat(ChatFormatting.GRAY + "Removed a macro " + ChatFormatting.RESET + name + ChatFormatting.GRAY + ".");
            Harakiri.get().getConfigManager().saveAll();
        } else if(args[1].equalsIgnoreCase("list")) {
            Harakiri.get().logChat(ChatFormatting.GRAY + "Printing the macro list (" + ChatFormatting.RESET + Harakiri.get().getMacroManager().getMacroList().size() + ChatFormatting.GRAY + " macros):");
            for(Macro macro : Harakiri.get().getMacroManager().getMacroList()) {
                Harakiri.get().logChat(macro.getName() + ChatFormatting.GRAY + " -> " + ChatFormatting.RESET + macro.getMacro());
            }
        } else if(args[1].equalsIgnoreCase("clear")) {
            final int macroSize = Harakiri.get().getMacroManager().getMacroList().size();
            Harakiri.get().getMacroManager().getMacroList().clear();
            Harakiri.get().getConfigManager().saveAll();
            Harakiri.get().logChat(ChatFormatting.GRAY + "Cleared the macro list, removing " + ChatFormatting.RESET + macroSize + ChatFormatting.GRAY + " macros.");
        }

    }

    // param to can be -1, meaning to the end
    public String coerceArgsToSpacedString(String[] args, int from, int to){

        String returns = "";
        for(int i = from; to == -1 ? i < args.length : i < to; ++i){
            returns += args[i] + " ";
        }

        returns = returns.substring(0, returns.length() - 1); // strip last space

        return returns;
    }
}
