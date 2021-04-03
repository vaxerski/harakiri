package me.vaxry.harakiri.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.EventAddEntity;
import me.vaxry.harakiri.framework.event.world.EventRemoveEntity;
import me.vaxry.harakiri.framework.Friend;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class VisualRangeModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between alert modes.", Mode.NOTIFICATION);
    public final Value<Boolean> sound = new Value<Boolean>("Sound", new String[]{"Sound", "S"}, "Plays a sound when someone enters/exits.", false);
    public final Value<Integer> soundVol = new Value<Integer>("SoundVolume", new String[]{"SoundVolume", "SV"}, "Sound volume.", 1, 0, 10, 1);

    private enum Mode {
        CHAT, NOTIFICATION, BOTH
    }

    private int prevPlayer = -1;

    public VisualRangeModule() {
        super("VisualRange", new String[]{"VisRange", "VRange", "VR"}, "Sends a notification when someone enters your visual range.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onEntityAdded(EventAddEntity event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        if (!Minecraft.getMinecraft().player.isDead && event.getEntity() instanceof EntityPlayer && !event.getEntity().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getName())) {
            final Friend friend = Harakiri.get().getFriendManager().isFriend(event.getEntity());

            final String msg = (friend != null ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED) + (friend != null ? friend.getAlias() : event.getEntity().getName()) + ChatFormatting.WHITE + " has entered your visual range.";

            if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                Harakiri.get().getNotificationManager().addNotification("", msg);
            }

            if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                Harakiri.get().logChat(msg);
            }

            if (event.getEntity().getEntityId() == this.prevPlayer) {
                this.prevPlayer = -1;
            }

            if(this.sound.getValue())
                Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player.posX,
                        Minecraft.getMinecraft().player.posY,
                        Minecraft.getMinecraft().player.posZ,
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.AMBIENT,
                        this.soundVol.getValue(),
                        1F,
                        false);
        }
    }

    @Listener
    public void onEntityRemove(EventRemoveEntity event) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null)
            return;

        if (!Minecraft.getMinecraft().player.isDead && event.getEntity() instanceof EntityPlayer && !event.getEntity().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getName())) {
            if (this.prevPlayer != event.getEntity().getEntityId()) {
                this.prevPlayer = event.getEntity().getEntityId();
                final Friend friend = Harakiri.get().getFriendManager().isFriend(event.getEntity());
                final String msg = (friend != null ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED) + (friend != null ? friend.getAlias() : event.getEntity().getName()) + ChatFormatting.WHITE + " has left your visual range.";

                if (this.mode.getValue() == Mode.NOTIFICATION || this.mode.getValue() == Mode.BOTH) {
                    Harakiri.get().getNotificationManager().addNotification("", msg);
                }

                if (this.mode.getValue() == Mode.CHAT || this.mode.getValue() == Mode.BOTH) {
                    Harakiri.get().logChat(msg);
                }

                if(this.sound.getValue())
                    Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player.posX,
                            Minecraft.getMinecraft().player.posY,
                            Minecraft.getMinecraft().player.posZ,
                            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                            SoundCategory.AMBIENT,
                            this.soundVol.getValue(),
                            1F,
                            false);

            }
        }
    }


}
