package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketEntityEffect;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoEffectsModule extends Module {

    /* any anti-cheat */
    public final Value<Boolean> nausea = new Value<Boolean>("Nausea", new String[]{"naus", "nau", "n"}, "Disables the nausea effect.", true);
    public final Value<Boolean> blindness = new Value<Boolean>("Blindness", new String[]{"blind", "b"}, "Disables the blindness effect.", true);
    public final Value<Boolean> invisibility = new Value<Boolean>("Invisibility", new String[]{"invis", "inv", "i"}, "Disables the invisibility effect.", false);

    /* questionable on certain anti-cheats */
    public final Value<Boolean> wither = new Value<Boolean>("Wither", new String[]{"wit", "w"}, "Disables the withering effect.", false);
    public final Value<Boolean> levitation = new Value<Boolean>("Levitation", new String[]{"lev", "l"}, "Disables the levitation effect.", false);

    public NoEffectsModule() {
        super("NoEffects", new String[]{"AntiEffects", "NoEff", "AntiEff"}, "Removes potion effects from you.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketEntityEffect) {
                final SPacketEntityEffect packet = (SPacketEntityEffect) event.getPacket();
                if (Minecraft.getMinecraft().player != null && packet.getEntityId() == Minecraft.getMinecraft().player.getEntityId()) {
                    int effectId = packet.getEffectId();
                    if (this.nausea.getValue() && effectId == 9 /*nausea*/)
                        event.setCanceled(true);

                    if (this.invisibility.getValue() && effectId == 14 /*invisibility*/)
                        event.setCanceled(true);

                    if (this.blindness.getValue() && effectId == 15 /*blindness*/)
                        event.setCanceled(true);

                    if (this.wither.getValue() && effectId == 20 /*wither*/)
                        event.setCanceled(true);

                    if (this.levitation.getValue() && effectId == 25 /*levitation*/)
                        event.setCanceled(true);
                }
            }
        }
    }
}
