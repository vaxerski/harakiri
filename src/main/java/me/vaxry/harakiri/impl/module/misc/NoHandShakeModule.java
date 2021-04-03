package me.vaxry.harakiri.impl.module.misc;

import io.netty.buffer.Unpooled;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoHandShakeModule extends Module {

    public NoHandShakeModule() {
        super("NoHandShake", new String[]{"AntiHandShake", "NoShake"}, "Prevents forge from sending your mod list to the server while connecting.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof FMLProxyPacket && !Minecraft.getMinecraft().isSingleplayer()) {
                event.setCanceled(true);
            }
            if (event.getPacket() instanceof CPacketCustomPayload) {
                final CPacketCustomPayload packet = (CPacketCustomPayload) event.getPacket();
                if (packet.getChannelName().equals("MC|Brand")) {
                    packet.data = new PacketBuffer(Unpooled.buffer()).writeString("vanilla");
                }
            }
        }
    }

}
