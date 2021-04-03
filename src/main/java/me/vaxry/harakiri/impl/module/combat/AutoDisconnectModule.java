package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class AutoDisconnectModule extends Module {

    public final Value<Float> health = new Value("Health", new String[]{"Hp"}, "The amount of health, in HP, to disconnect.", 8.0f, 0.0f, 20.0f, 0.5f);

    public AutoDisconnectModule() {
        super("AutoDisconnect", new String[]{"Disconnect"}, "Automatically disconnects when your health is low enough.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (Minecraft.getMinecraft().player.getHealth() <= this.health.getValue()) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));
                this.toggle();
            }
        }
    }

}
