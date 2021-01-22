package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.event.render.EventRender2D;
import me.vaxry.harakiri.impl.module.world.InfEnderChestModule;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth (riga)
 */
public final class LastInvCommand extends Command {

    public LastInvCommand() {
        super("LastInv", new String[]{"EnderChest", "Echest", "Portable"}, "Opens your previous inventory if \"MoreInv\" is enabled.", "LastInv");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 1, 1)) {
            this.printUsage();
            return;
        }

        Harakiri.INSTANCE.getEventManager().addEventListener(this); // subscribe to the event listener
    }

    @Listener
    public void render(EventRender2D event) {
        final InfEnderChestModule mod = (InfEnderChestModule) Harakiri.INSTANCE.getModuleManager().find(InfEnderChestModule.class);
        if (mod != null) {
            if (mod.getScreen() != null) {
                Minecraft.getMinecraft().displayGuiScreen(mod.getScreen());
                Harakiri.INSTANCE.logChat("Opening the last inventory.");
            } else {
                Harakiri.INSTANCE.logChat("Inventory already closed.");
            }
        }
        Harakiri.INSTANCE.getEventManager().removeEventListener(this);
    }

}