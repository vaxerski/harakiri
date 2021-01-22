package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.impl.config.AutoIgnoreConfig;
import me.vaxry.harakiri.impl.module.misc.AutoIgnoreModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 7/1/2019 @ 11:37 PM.
 */
public final class AutoIgnoreCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public AutoIgnoreCommand() {
        super("AutoIgnore", new String[]{"AutomaticIgnore", "AIG", "AIgnore"}, "Allows you to add or remove phrases from AutoIgnore", "AutoIgnore Add <Phrase>\n" +
                "AutoIgnore Remove <Phrase>\n" +
                "AutoIgnore List\n" +
                "AutoIgnore Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final AutoIgnoreModule autoIgnoreModule = (AutoIgnoreModule) Harakiri.INSTANCE.getModuleManager().find(AutoIgnoreModule.class);

        if (autoIgnoreModule == null) {
            Harakiri.INSTANCE.errorChat("AutoIgnore is missing");
            return;
        }

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 3)) {
                this.printUsage();
                return;
            }
            final StringBuilder sb = new StringBuilder();

            for (int i = 2; i < split.length; i++) {
                final String s = split[i];
                sb.append(s + (i == split.length - 1 ? "" : " "));
            }

            final String phrase = sb.toString();

            if (autoIgnoreModule.blacklistContains(phrase.toLowerCase())) {
                Harakiri.INSTANCE.logChat("AutoIgnore already contains that phrase");
            } else {
                Harakiri.INSTANCE.logChat("Added phrase \"" + phrase + "\"");
                autoIgnoreModule.getBlacklist().add(phrase);
                Harakiri.INSTANCE.getConfigManager().save(AutoIgnoreConfig.class);
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3)) {
                this.printUsage();
                return;
            }
            final StringBuilder sb = new StringBuilder();

            for (int i = 2; i < split.length; i++) {
                final String s = split[i];
                sb.append(s + (i == split.length - 1 ? "" : " "));
            }

            final String phrase = sb.toString();

            if (autoIgnoreModule.blacklistContains(phrase.toLowerCase())) {
                Harakiri.INSTANCE.logChat("Removed phrase \"" + phrase + "\"");
                autoIgnoreModule.getBlacklist().remove(phrase);
                Harakiri.INSTANCE.getConfigManager().save(AutoIgnoreConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("AutoIgnore does not contain that phrase");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = autoIgnoreModule.getBlacklist().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Phrases [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final String phrase = autoIgnoreModule.getBlacklist().get(i);
                    if (phrase != null) {
                        msg.appendSibling(new TextComponentString("\247a" + phrase + "\2477" + ((i == size - 1) ? "" : ", ")));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any phrases");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = autoIgnoreModule.getBlacklist().size();

            if (size > 0) {
                Harakiri.INSTANCE.logChat("Removed \247c" + size + "\247f phrase" + (size > 1 ? "s" : ""));
                autoIgnoreModule.getBlacklist().clear();
                Harakiri.INSTANCE.getConfigManager().save(AutoIgnoreConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any phrases");
            }

        } else {
            Harakiri.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }
}
