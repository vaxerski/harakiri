package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketUseEntity;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.sound.sampled.*;
import java.io.File;

public class HitsoundModule extends Module {
    String audiopath;

    public final Value<Float> volume = new Value<Float>("Volume", new String[]{"Volume", "Vol"}, "Volume of the hitsound", 0.5f, 0f, 1.f, 0.01f);
    CPacketUseEntity lastPacket = null;

    public HitsoundModule(){
        super("Hitsounds", new String[]{"Hitsounds"}, "Plays a sound when you hit an entity.", "NONE", -1, ModuleType.MISC);
        audiopath = Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/hitsound.wav" : "\\harakiri\\hitsound.wav");
    }

    @Listener
    public void onPacketSend(EventSendPacket event){
        if(event.getPacket() instanceof CPacketUseEntity){
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
            if(packet.getAction() == CPacketUseEntity.Action.ATTACK && packet != lastPacket) {
                PlaySound(new File(audiopath));
                lastPacket = packet;
            }
        }
    }

    /*@SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event){
        if(!this.isEnabled())
            return;

        try {
            if (event.getSource().getTrueSource().equals(Minecraft.getMinecraft().player)) {

                PlaySound(new File(audiopath));
            }
        }catch(Throwable t){
            // Oof
        }
    }*/


    private void PlaySound(File in){
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(in));
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume.getValue()));
            clip.start();
        }catch(Throwable t){
            Harakiri.get().logChat("The sound file was not found or is incorrectly exported. In the 99% of cases where this is your mistake, please refer to the FAQ about hitsounds. In the case of that 1%, please contact vaxry.");
        }
    }
}
