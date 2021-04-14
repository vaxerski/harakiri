package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoAfkModule extends Module {

    public final Value<Boolean> yawOffset = new Value<Boolean>("Yaw", new String[]{"yaw", "y"}, "Moves the yaw.", false);
    public final Value<Boolean> jump = new Value<Boolean>("Jump", new String[]{"jump", "j"}, "Jumps.", false);
    public final Value<Boolean> punch = new Value<Boolean>("Punch", new String[]{"punch", "p"}, "Punches.", false);
    public final Value<Integer> delay = new Value<Integer>("Delay", new String[]{"delay", "d"}, "Delay between actions (seconds).", 1, 1, 5, 1);

    private final Timer timer = new Timer();

    private final RotationTask rotationTask = new RotationTask("noafk", 2);

    public NoAfkModule() {
        super("AntiAFK", new String[]{"AntiAFK"}, "Prevents you from being kicked while AFK.", "NONE", -1, ModuleType.MISC);
        timer.reset();
        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        if(timer.passed(this.delay.getValue()*1000) && event.getStage() == EventStageable.EventStage.PRE){

            if(yawOffset.getValue()){
                float yaw = (float) Math.random() * 360F - 180F;
                float pitch = mc.player.rotationPitch;
                Harakiri.get().getRotationManager().setPlayerRotations(yaw, pitch);
            }

            if(jump.getValue())
                mc.player.jump();

            if(punch.getValue())
                mc.player.swingArm(EnumHand.MAIN_HAND);

            timer.reset();
        }

    }

}
