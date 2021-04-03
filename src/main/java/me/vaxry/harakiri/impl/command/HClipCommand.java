package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

/**
 * Author Seth
 * 4/16/2019 @ 9:11 PM.
 */
public final class HClipCommand extends Command {

    public HClipCommand() {
        super("HClip", new String[]{"HC", "HorizontalClip"}, "Allows you to teleport horizontally", "HClip <Amount>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isDouble(split[1])) {
            final double num = Double.parseDouble(split[1]);

            final Vec3d dir = MathUtil.direction(Minecraft.getMinecraft().player.rotationYaw);

            if (dir != null) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    Minecraft.getMinecraft().player.getRidingEntity().setPosition(Minecraft.getMinecraft().player.getRidingEntity().posX + dir.x * num, Minecraft.getMinecraft().player.getRidingEntity().posY, Minecraft.getMinecraft().player.getRidingEntity().posZ + dir.z * num);
                } else {
                    Minecraft.getMinecraft().player.setPosition(Minecraft.getMinecraft().player.posX + dir.x * num, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ + dir.z * num);
                }
                Harakiri.get().logChat("HClipped you " + ((num > 0) ? "forward" : "backward") + " " + num);
            }
        } else {
            Harakiri.get().errorChat("Unknown number: " + "\247f\"" + split[1] + "\"");
        }
    }
}
