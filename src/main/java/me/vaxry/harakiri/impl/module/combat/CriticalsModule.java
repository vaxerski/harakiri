package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class CriticalsModule extends Module {

    public final Value<Mode> mode = new Value("Mode", new String[]{"Mode", "M"}, "The criticals mode to use.", Mode.PACKET);

    public CriticalsModule() {
        super("Criticals", new String[]{"Crits"}, "Always makes you try to hit criticals.", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketUseEntity) {
                final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
                if (packet.getAction() == CPacketUseEntity.Action.ATTACK) {
                    final Minecraft mc = Minecraft.getMinecraft();

                    if (mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase) {
                        switch (this.mode.getValue()) {
                            case JUMP:
                                mc.player.jump();
                                break;
                            case PACKET:
                                //TODO make sure u can actually go there
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                                break;
                        }
                    }
                }
            }
        }
    }

    private enum Mode {
        JUMP, PACKET
    }

}
