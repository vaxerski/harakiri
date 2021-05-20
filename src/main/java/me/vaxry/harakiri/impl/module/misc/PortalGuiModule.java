package me.vaxry.harakiri.impl.module.misc;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;


public final class PortalGuiModule extends Module {

    public PortalGuiModule() {
        super("PortalGui", new String[]{"PGui"}, "Allows you to open guis while in portals.", "NONE", -1, ModuleType.MISC);
    }

    Attender<EventUpdateWalkingPlayer> onUpdateWalkingPlayer = new Attender<>(EventUpdateWalkingPlayer.class, event -> {
        if(event.getStage() != EventStageable.EventStage.PRE)
            return;

        Minecraft.getMinecraft().player.inPortal = false;
    });
}
