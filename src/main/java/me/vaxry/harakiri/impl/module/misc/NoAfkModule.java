package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoAfkModule extends Module {

    public final Value<Integer> yawOffset = new Value<Integer>("Yaw", new String[]{"yaw", "y"}, "The yaw to alternate each tick.", 1, 0, 180, 1);

    private final RotationTask rotationTask = new RotationTask("NoAFKTask", 1); /* 1 == low priority */

    public NoAfkModule() {
        super("AntiAFK", new String[]{"AntiAFK"}, "Prevents you from being kicked while AFK.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        switch (event.getStage()) {
            case PRE:
                float yaw = mc.player.rotationYaw;
                float pitch = mc.player.rotationPitch;
                yaw += (this.yawOffset.getValue() * Math.sin(mc.player.ticksExisted / Math.PI));

                Harakiri.get().getRotationManager().startTask(this.rotationTask);
                if (this.rotationTask.isOnline()) {
                    Harakiri.get().getRotationManager().setPlayerRotations(yaw, pitch);
                }
                break;
            case POST:
                if (this.rotationTask.isOnline())
                    Harakiri.get().getRotationManager().finishTask(this.rotationTask);
                break;
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer.Rotation) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    final CPacketPlayer.Rotation packet = (CPacketPlayer.Rotation) event.getPacket();
                    packet.yaw += (this.yawOffset.getValue() * Math.sin(Minecraft.getMinecraft().player.ticksExisted / Math.PI));
                }
            }
        }
    }

}
