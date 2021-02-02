package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.module.Module;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 8/12/2019 @ 1:59 AM.
 */
public final class VanillaTabModule extends Module {

    public VanillaTabModule() {
        super("NoTabHeader", new String[]{"VTab", "VanillaT"}, "Removes the Tab Header and Footer.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketPlayerListHeaderFooter) {
                event.setCanceled(true);
            }
        }
    }

}
