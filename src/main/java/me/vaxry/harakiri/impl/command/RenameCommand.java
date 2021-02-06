package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;

/**
 * Author Seth
 * 8/15/2019 @ 10:27 PM.
 */
public final class RenameCommand extends Command {

    public RenameCommand() {
        super("Rename", new String[]{"Ren"}, "Allows you to rename your held item while in creative mode(Supports color codes)", "Rename <Name>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.player.isCreative()) {
            Harakiri.INSTANCE.errorChat("Creative mode is required to use this command.");
            return;
        }

        final ItemStack itemStack = mc.player.getHeldItemMainhand();

        if (itemStack.isEmpty()) {
            Harakiri.INSTANCE.errorChat("Please hold an item in your main hand to enchant.");
            return;
        }

        final String[] split = input.split(" ");

        final StringBuilder sb = new StringBuilder();

        final int size = split.length;

        for (int i = 1; i < size; i++) {
            final String arg = split[i];
            sb.append(arg + ((i == size - 1) ? "" : " "));
        }

        final String name = sb.toString().replace("&", "\247");

        NBTTagCompound tagCompound = itemStack.getTagCompound();

        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }

        itemStack.getOrCreateSubCompound("display").setString("Name", name);

        mc.player.connection.sendPacket(new CPacketCreativeInventoryAction(mc.player.inventory.currentItem, itemStack));
        Harakiri.INSTANCE.logChat("Renamed your item to " + name);
    }
}
