package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class RotationLock extends Module {

    public final Value<Boolean> yawLock = new Value<Boolean>("Yaw", new String[]{"Y"}, "Lock the player's rotation yaw if enabled.", true);
    public final Value<Boolean> pitchLock = new Value<Boolean>("Pitch", new String[]{"P"}, "Lock the player's rotation pitch if enabled.", false);

    private float yaw;
    private float pitch;

    public RotationLock() {
        super("RotationLock", new String[]{"RotLock", "Rotation"}, "Locks you rotation.", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (Minecraft.getMinecraft().player != null) {
            this.yaw = Minecraft.getMinecraft().player.rotationYaw;
            this.pitch = Minecraft.getMinecraft().player.rotationPitch;
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        Harakiri.get().getRotationManager().updateRotations();
        if (this.yawLock.getValue()) {
            Harakiri.get().getRotationManager().setPlayerYaw(this.yaw);
        }
        if (this.pitchLock.getValue()) {
            Harakiri.get().getRotationManager().setPlayerPitch(this.pitch);
        }
    }

}
