package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.player.EventPlayerJoin;
import me.vaxry.harakiri.framework.event.player.EventPlayerLeave;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class JoinLeaveManager {

    public JoinLeaveManager() {
        Harakiri.get().getEventManager().addEventListener(this);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketPlayerListItem) {
                final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
                final Minecraft mc = Minecraft.getMinecraft();
                if (mc.player != null && mc.player.ticksExisted >= 1000) {
                    if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
                        for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                            if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                                new Thread(() -> {
                                    final String name = Harakiri.get().getApiManager().resolveName(playerData.getProfile().getId().toString());
                                    if (name != null) {
                                        Harakiri.get().getEventManager().dispatchEvent(new EventPlayerJoin(name, playerData.getProfile().getId().toString()));
                                    }
                                }).start();
                            }
                        }
                    }
                    if (packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                        for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                            if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                                new Thread(() -> {
                                    final String name = Harakiri.get().getApiManager().resolveName(playerData.getProfile().getId().toString());
                                    if (name != null) {
                                        Harakiri.get().getEventManager().dispatchEvent(new EventPlayerLeave(name, playerData.getProfile().getId().toString()));
                                    }
                                }).start();
                            }
                        }
                    }
                }
            }
        }
    }

    public void unload() {
        Harakiri.get().getEventManager().removeEventListener(this);
    }
}
