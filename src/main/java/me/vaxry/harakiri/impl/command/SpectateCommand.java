package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Author Seth
 * 4/21/2019 @ 2:18 PM.
 */
public final class SpectateCommand extends Command {

    public SpectateCommand() {
        super("Spectate", new String[]{"Spec"}, "Allows you to spectate nearby players", "Spectate <Username>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        EntityPlayer target = null;

        for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
            if (e != null) {
                if (e instanceof EntityPlayer && e.getName().equalsIgnoreCase(split[1])) {
                    target = (EntityPlayer) e;
                    break;
                }
            }
        }

        if (target != null) {
            Harakiri.get().logChat("Now spectating: " + target.getName());
            Minecraft.getMinecraft().setRenderViewEntity(target);
        } else {
            Harakiri.get().errorChat("\"" + split[1] + "\" is not within range");
        }
    }

}
