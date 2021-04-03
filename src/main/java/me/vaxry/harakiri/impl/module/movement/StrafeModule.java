package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class StrafeModule extends Module {

    public Value<Boolean> ground = new Value<Boolean>("Ground", new String[]{"Ground", "OnGround"}, "Enables strafe movement while on ground.", false);
    public Value<Boolean> elytraCheck = new Value<Boolean>("ElytraCheck", new String[]{"Flycheck", "Elytra"}, "Lets you use ElytraFly and Strafe.", true);

    public StrafeModule() {
        super("Strafe", new String[]{"Strafe"}, "Full movement control while airborne, and optionally on ground.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onMove(EventMove event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        if (mc.player.isSneaking() || mc.player.isOnLadder() || mc.player.isInWeb || mc.player.isInLava() || mc.player.isInWater() || mc.player.capabilities.isFlying)
            return;

        if (this.elytraCheck.getValue() && mc.player.isElytraFlying())
            return;

        // check to bypass option on ground or not
        if (!this.ground.getValue()) {
            if (mc.player.onGround)
                return;
        }

        if(Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled())
            return; // dont run when freecam, bugs out.

        // check for flight, could be an option maybe but it bugs out  packet fly
        final FlightModule flightModule = (FlightModule) Harakiri.get().getModuleManager().find(FlightModule.class);
        if (flightModule != null && flightModule.isEnabled()) {
            return;
        }

        // movement data variables
        float playerSpeed = 0.2873f;
        float moveForward = mc.player.movementInput.moveForward;
        float moveStrafe = mc.player.movementInput.moveStrafe;
        float rotationPitch = mc.player.rotationPitch;
        float rotationYaw = mc.player.rotationYaw;

        // check for speed potion
        if (mc.player.isPotionActive(MobEffects.SPEED)) {
            final int amplifier = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
            playerSpeed *= (1.0f + 0.2f * (amplifier + 1));
        }

        // not movement input, stop all motion
        if (moveForward == 0.0f && moveStrafe == 0.0f) {
            event.setX(0.0d);
            event.setZ(0.0d);
        } else {
            if (moveForward != 0.0f) {
                if (moveStrafe > 0.0f) {
                    rotationYaw += ((moveForward > 0.0f) ? -45 : 45);
                } else if (moveStrafe < 0.0f) {
                    rotationYaw += ((moveForward > 0.0f) ? 45 : -45);
                }
                moveStrafe = 0.0f;
                if (moveForward > 0.0f) {
                    moveForward = 1.0f;
                } else if (moveForward < 0.0f) {
                    moveForward = -1.0f;
                }
            }
            event.setX((moveForward * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))) + (moveStrafe * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))));
            event.setZ((moveForward * playerSpeed) * Math.sin(Math.toRadians((rotationYaw + 90.0f))) - (moveStrafe * playerSpeed) * Math.cos(Math.toRadians((rotationYaw + 90.0f))));
        }

        // we need to ensure we don't interfere with safewalk's limitations, so we run it's checks again on the same event
        final SafeWalkModule safeWalkModule = (SafeWalkModule) Harakiri.get().getModuleManager().find(SafeWalkModule.class);
        if (safeWalkModule != null && safeWalkModule.isEnabled()) {
            safeWalkModule.onMove(event);
        }
    }
}
