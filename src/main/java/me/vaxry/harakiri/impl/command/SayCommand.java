package me.vaxry.harakiri.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 8/18/2019 @ 9:51 PM.
 */
public final class SayCommand extends Command {

    public SayCommand() {
        super("Say", new String[]{"say", "Say"}, "Sends a message in chat", "Say <text>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        String message = input.substring(input.indexOf(" "));
        message.substring(Math.min(255, message.length() - 1));

        mc.player.connection.sendPacket(new CPacketChatMessage(message));
    }
}
