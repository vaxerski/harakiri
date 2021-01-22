package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 8:15 AM.
 */
public final class PortalGuiModule extends Module {

    public PortalGuiModule() {
        super("PortalGui", new String[]{"PGui"}, "Allows you to open guis while in portals", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().player.inPortal = false;
        }
    }
}
