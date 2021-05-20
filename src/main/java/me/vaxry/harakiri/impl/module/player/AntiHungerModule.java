package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;


public class AntiHungerModule extends Module {

    public AntiHungerModule() {
        super("AntiHunger", new String[]{"AntiHunger", "AH"}, "Makes you lose less hunger.", "NONE", -1, ModuleType.PLAYER);
    }

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        final Minecraft mc = Minecraft.getMinecraft();
        if(event.getPacket() instanceof CPacketPlayer && mc.player.onGround && !mc.player.isElytraFlying()){
            final CPacketPlayer packet = (CPacketPlayer)event.getPacket();

            if (mc.player.fallDistance > 0 || mc.playerController.isHittingBlock)
            {
                packet.onGround = true;
            }
            else
            {
                packet.onGround = false;
            }
        }

        if (event.getPacket() instanceof CPacketEntityAction && mc.player.isSprinting())
        {
            final CPacketEntityAction l_Packet = (CPacketEntityAction) event.getPacket();
            if (l_Packet.getAction() == CPacketEntityAction.Action.START_SPRINTING || l_Packet.getAction() == CPacketEntityAction.Action.STOP_SPRINTING)
            {
                event.setCanceled(true);
            }
        }
    });
}
