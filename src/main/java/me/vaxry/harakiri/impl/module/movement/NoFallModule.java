package me.vaxry.harakiri.impl.module.movement;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;


public final class NoFallModule extends Module {

    public NoFallModule() {
        super("NoFall", new String[]{"NoFallDamage"}, "Eliminates fall damage.", "NONE", -1, ModuleType.MOVEMENT);
    }

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer && Minecraft.getMinecraft().player.fallDistance >= 3.0f) {
                final CPacketPlayer packet = (CPacketPlayer) event.getPacket();
                packet.onGround = true;
            }
        }
    });
}
