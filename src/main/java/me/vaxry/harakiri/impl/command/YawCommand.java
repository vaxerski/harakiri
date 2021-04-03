package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.util.StringUtil;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 5/3/2019 @ 5:27 PM.
 */
public final class YawCommand extends Command {

    public YawCommand() {
        super("Yaw", new String[]{"Yw"}, "Allows you to set your yaw", "Yaw <Number>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isDouble(split[1])) {
            final float num = Float.parseFloat(split[1]);

            Minecraft.getMinecraft().player.rotationYaw = num;

            if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                Minecraft.getMinecraft().player.getRidingEntity().rotationYaw = num;
            }

            Harakiri.get().logChat("Set your yaw to " + num);
        } else {
            Harakiri.get().errorChat("Unknown number: " + "\247f\"" + split[1] + "\"");
        }
    }
}
