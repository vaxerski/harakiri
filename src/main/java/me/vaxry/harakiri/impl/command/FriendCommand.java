package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import me.vaxry.harakiri.framework.friend.Friend;
import me.vaxry.harakiri.impl.config.FriendConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Author Seth
 * 4/16/2019 @ 11:45 PM.
 */
public final class FriendCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public FriendCommand() {
        super("Friend", new String[]{"F"}, "Allows you to add or remove friends.", "Friend Add <username>\n" +
                "Friend Add <username> <alias>\n" +
                "Friend Remove <username>\n" +
                "Friend List\n" +
                "Friend Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 4)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 3, 4)) {
                this.printUsage();
                return;
            }

            final String username = split[2];
            final Friend friend = Harakiri.INSTANCE.getFriendManager().find(username);

            if (friend != null) {
                Harakiri.INSTANCE.logChat("\247c" + username + " \247fis already your friend");
            } else {
                if (split.length > 3) {
                    if (!this.clamp(input, 4, 4)) {
                        this.printUsage();
                        return;
                    }
                    final String alias = split[3];
                    Harakiri.INSTANCE.logChat("Added \247c" + username + " \247fas \247c" + alias + "\247f");
                    Harakiri.INSTANCE.getFriendManager().add(username, alias, true);
                } else {
                    Harakiri.INSTANCE.logChat("Added \247c" + username + " \247f");
                    Harakiri.INSTANCE.getFriendManager().add(username, username, true);
                }
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final int friends = Harakiri.INSTANCE.getFriendManager().getFriendList().size();

            if (friends == 0) {
                Harakiri.INSTANCE.logChat("You don't have any friends :(");
                return;
            }

            final String username = split[2];
            final Friend friend = Harakiri.INSTANCE.getFriendManager().find(username);

            if (friend != null) {
                Harakiri.INSTANCE.logChat("Removed \247c" + friend.getAlias() + " \247f");
                Harakiri.INSTANCE.getFriendManager().getFriendList().remove(friend);
                Harakiri.INSTANCE.getConfigManager().save(FriendConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("\247c" + username + " \247fis not your friend");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = Harakiri.INSTANCE.getFriendManager().getFriendList().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Friends [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final Friend friend = Harakiri.INSTANCE.getFriendManager().getFriendList().get(i);
                    if (friend != null) {
                        msg.appendSibling(new TextComponentString("\247a" + friend.getAlias() + "\2477" + ((i == size - 1) ? "" : ", "))
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Name: " + friend.getName() + "\n" + "UUID: " + friend.getUuid())))));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any friends :(");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int friends = Harakiri.INSTANCE.getFriendManager().getFriendList().size();

            if (friends > 0) {
                Harakiri.INSTANCE.logChat("Removed \247c" + friends + "\247f friend" + (friends > 1 ? "s" : ""));
                Harakiri.INSTANCE.getFriendManager().getFriendList().clear();
                Harakiri.INSTANCE.getConfigManager().save(FriendConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any friends :(");
            }
        } else {
            Harakiri.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }
}
