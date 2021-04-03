package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.Friend;
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
    public void run(String input) {
        if (!this.verifyInput(input, 2, 4)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(addAlias, split[1])) {
            if (!this.verifyInput(input, 3, 4)) {
                this.printUsage();
                return;
            }

            final String username = split[2];
            final Friend friend = Harakiri.get().getFriendManager().find(username);

            if (friend != null) {
                Harakiri.get().logChat("\247c" + username + " \247fis already your friend");
            } else {
                if (split.length > 3) {
                    if (!this.verifyInput(input, 4, 4)) {
                        this.printUsage();
                        return;
                    }
                    final String alias = split[3];
                    Harakiri.get().logChat("Added \247c" + username + " \247fas \247c" + alias + "\247f");
                    Harakiri.get().getFriendManager().add(username, alias, true);
                } else {
                    Harakiri.get().logChat("Added \247c" + username + " \247f");
                    Harakiri.get().getFriendManager().add(username, username, true);
                }
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.verifyInput(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final int friends = Harakiri.get().getFriendManager().getFriendList().size();

            if (friends == 0) {
                Harakiri.get().logChat("You don't have any friends :(");
                return;
            }

            final String username = split[2];
            final Friend friend = Harakiri.get().getFriendManager().find(username);

            if (friend != null) {
                Harakiri.get().logChat("Removed \247c" + friend.getAlias() + " \247f");
                Harakiri.get().getFriendManager().getFriendList().remove(friend);
                Harakiri.get().getConfigManager().save(FriendConfig.class);
            } else {
                Harakiri.get().logChat("\247c" + username + " \247fis not your friend");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.verifyInput(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int size = Harakiri.get().getFriendManager().getFriendList().size();

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Friends [" + size + "]\247f ");

                for (int i = 0; i < size; i++) {
                    final Friend friend = Harakiri.get().getFriendManager().getFriendList().get(i);
                    if (friend != null) {
                        msg.appendSibling(new TextComponentString("\247a" + friend.getAlias() + "\2477" + ((i == size - 1) ? "" : ", "))
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Name: " + friend.getName() + "\n" + "UUID: " + friend.getUuid())))));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Harakiri.get().logChat("You don't have any friends :(");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.verifyInput(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int friends = Harakiri.get().getFriendManager().getFriendList().size();

            if (friends > 0) {
                Harakiri.get().logChat("Removed \247c" + friends + "\247f friend" + (friends > 1 ? "s" : ""));
                Harakiri.get().getFriendManager().getFriendList().clear();
                Harakiri.get().getConfigManager().save(FriendConfig.class);
            } else {
                Harakiri.get().logChat("You don't have any friends :(");
            }
        } else {
            Harakiri.get().errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }
    }
}
