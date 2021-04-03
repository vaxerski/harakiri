package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class AutoFishModule extends Module {

    public AutoFishModule() {
        super("AutoFish", new String[]{"AutomaticFish"}, "Automatically catches fish.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getCategory() == SoundCategory.NEUTRAL && packet.getSound() == SoundEvents.ENTITY_BOBBER_SPLASH) {
                    final Minecraft mc = Minecraft.getMinecraft();

                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemFishingRod) {
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                    }
                }
            }
        }
    }

}
