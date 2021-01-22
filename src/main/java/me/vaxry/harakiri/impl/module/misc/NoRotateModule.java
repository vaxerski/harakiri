package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 10:03 PM.
 */
public final class NoRotateModule extends Module {

    public NoRotateModule() {
        super("NoRotate", new String[]{"NoRot", "AntiRotate"}, "Prevents you from processing server rotations", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                if (Minecraft.getMinecraft().player != null) {
                    packet.yaw = Minecraft.getMinecraft().player.rotationYaw;
                    packet.pitch = Minecraft.getMinecraft().player.rotationPitch;
                }
            }
        }
    }

}
