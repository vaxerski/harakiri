package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.ignore.Ignored;
import me.vaxry.harakiri.impl.config.IgnoreConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 6/29/2019 @ 5:03 AM.
 */
public final class IgnoreCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public IgnoreCommand() {
        super("Ignore", new String[]{"Ignor"}, "Allows you to ignore other players", "Ignore Add <Username>\n" +
                "Ignore Remove <Username>\n" +
                "Ignore List\n" +
                "Ignore Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final Ignored ignored = Harakiri.INSTANCE.getIgnoredManager().find(split[2]);

            if (ignored != null) {
                Harakiri.INSTANCE.logChat("\247c" + ignored.getName() + " \247fis already ignored");
            } else {
                Harakiri.INSTANCE.logChat("Added \247c" + split[2] + "\247f to your ignore list");
                Harakiri.INSTANCE.getIgnoredManager().add(split[2]);
                Harakiri.INSTANCE.getConfigManager().save(IgnoreConfig.class);
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final int size = Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().size();

            if (size == 0) {
                Harakiri.INSTANCE.logChat("You don't have anyone ignored");
                return;
            }

            final Ignored ignored = Harakiri.INSTANCE.getIgnoredManager().find(split[2]);

            if (ignored != null) {
                Harakiri.INSTANCE.logChat("Removed \247c" + ignored.getName() + "\247f from your ignore list");
                Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().remove(ignored);
                Harakiri.INSTANCE.getConfigManager().save(IgnoreConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("\247c" + split[1] + " \247fis not ignored");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }
            final int size = Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Ignored [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final Ignored ignored = Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().get(i);
                    if (ignored != null) {
                        msg.appendSibling(new TextComponentString("\247a" + ignored.getName() + "\2477" + ((i == size - 1) ? "" : ", ")));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Harakiri.INSTANCE.logChat("You don't have anyone ignored");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }
            final int size = Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().size();

            if (size > 0) {
                Harakiri.INSTANCE.logChat("Removed \247c" + size + "\247f ignored player" + (size > 1 ? "s" : ""));
                Harakiri.INSTANCE.getIgnoredManager().getIgnoredList().clear();
                Harakiri.INSTANCE.getConfigManager().save(IgnoreConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("You don't have anyone ignored");
            }
        } else {
            Harakiri.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }
}
