package me.vaxry.harakiri.impl.module.misc;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.network.play.client.CPacketUpdateSign;


public final class ColoredSignsModule extends Module {

    public ColoredSignsModule() {
        super("SignColor", new String[]{"CSigns", "CSign", "SignColor", "SignColor"}, "Allows you to use the & character to color signs (Doesn't work on spigot).", "NONE", -1, ModuleType.MISC);
    }

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketUpdateSign) {
                final CPacketUpdateSign packet = (CPacketUpdateSign) event.getPacket();
                for (int i = 0; i < 4; i++) {
                    packet.lines[i] = packet.lines[i].replace("&", "\247" + "\247a");
                }
            }
        }
    });

}
