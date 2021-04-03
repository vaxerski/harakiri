package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.entity.EventHorseSaddled;
import me.vaxry.harakiri.framework.event.entity.EventPigTravel;
import me.vaxry.harakiri.framework.event.entity.EventSteerEntity;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class EntityControlModule extends Module {

    public EntityControlModule() {
        super("EntityControl", new String[]{"AntiSaddle", "EntityRide", "NoSaddle"}, "Allows you to control llamas, horses, pigs without a saddle/carrot on a stick", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void pigTravel(EventPigTravel event) {
        try {
            final Minecraft mc = Minecraft.getMinecraft();
            final boolean moving = mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.jump;

            final Entity riding = mc.player.getRidingEntity();

            if (riding instanceof EntityPig) {
                if (!moving && riding.onGround) {
                    event.setCanceled(true);
                }
            }
        }catch (Throwable t){
            // Suppress an error in logs.
        }
    }

    @Listener
    public void steerEntity(EventSteerEntity event) {
        event.setCanceled(true);
    }

    @Listener
    public void horseSaddled(EventHorseSaddled event) {
        event.setCanceled(true);
    }

}
