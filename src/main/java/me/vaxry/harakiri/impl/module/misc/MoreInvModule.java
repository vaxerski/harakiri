package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketCloseWindow;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class MoreInvModule extends Module {

    public MoreInvModule() {
        super("XCarry", new String[]{"XCarry", "MoreInventory"}, "Allows you to carry items in your crafting and dragging slots.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null) {
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketCloseWindow(Minecraft.getMinecraft().player.inventoryContainer.windowId));
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketCloseWindow) {
                final CPacketCloseWindow packet = (CPacketCloseWindow) event.getPacket();
                if (packet.windowId == Minecraft.getMinecraft().player.inventoryContainer.windowId) {
                    event.setCanceled(true);
                }
            }
        }
    }


}
