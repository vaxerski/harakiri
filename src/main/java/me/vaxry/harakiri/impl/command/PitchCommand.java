package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.util.StringUtil;
import net.minecraft.client.Minecraft;

/**
 * Author Seth
 * 5/3/2019 @ 5:31 PM.
 */
public final class PitchCommand extends Command {

    public PitchCommand() {
        super("Pitch", new String[]{"Pch"}, "Allows you to set your pitch", "Pitch <Number>");
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

            Minecraft.getMinecraft().player.rotationPitch = num;
            if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                Minecraft.getMinecraft().player.getRidingEntity().rotationPitch = num;
            }

            Harakiri.get().logChat("Set your pitch to " + num);
        } else {
            Harakiri.get().errorChat("Unknown number: " + "\247f\"" + split[1] + "\"");
        }
    }
}
