package me.vaxry.harakiri.impl.module.combat;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Friend;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketUseEntity;


public final class NoFriendHurtModule extends Module {

    public NoFriendHurtModule() {
        super("NoFriendDamage", new String[]{"NoFriendDMG", "FriendProtect"}, "Makes you not hit friends.", "NONE", -1, ModuleType.COMBAT);
    }

    Attender<EventSendPacket> onSendPacket = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage().equals(EventStageable.EventStage.PRE)) {
            if (event.getPacket() instanceof CPacketUseEntity) {
                final CPacketUseEntity packetUseEntity = (CPacketUseEntity) event.getPacket();
                if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().objectMouseOver == null)
                    return;

                if (Minecraft.getMinecraft().objectMouseOver.entityHit == null)
                    return;

                final Friend friend = Harakiri.get().getFriendManager().isFriend(Minecraft.getMinecraft().objectMouseOver.entityHit);
                if (packetUseEntity.getAction() == CPacketUseEntity.Action.ATTACK && friend != null) {
                    event.setCanceled(true);
                }
            }
        }
    });
}
