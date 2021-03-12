package me.vaxry.harakiri.framework.mixin.entity;

import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerControllerMP.class, priority = 9998)
public class MixinPlayerControllerMP {
    /*@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo callbackInfo) {
        EventPreAttack preEvent = new EventPreAttack(playerIn, targetEntity);
        Harakiri.get().getEventManager().dispatchEvent(preEvent);
    }*/
}
