package me.vaxry.harakiri.framework.mixin.game;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final EventDestroyBlock event = new EventDestroyBlock(pos);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "clickBlock", cancellable = true, at = @At("HEAD"))
    private void onClickBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        final EventClickBlock event = new EventClickBlock(loc, face);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "resetBlockRemoving", cancellable = true, at = @At("HEAD"))
    private void onResetBlockRemoving(CallbackInfo ci) {
        final EventResetBlockRemoving event = new EventResetBlockRemoving();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        final EventPlayerDamageBlock event = new EventPlayerDamageBlock(posBlock, directionFacing);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
    private void onProcessRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        final EventRightClickBlock event = new EventRightClickBlock(pos, direction, vec, hand);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(EnumActionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "processRightClick", at = @At("HEAD"), cancellable = true)
    private void onProcessRightClick(EntityPlayer player, World worldIn, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        final EventRightClick event = new EventRightClick(hand);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(EnumActionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "isHittingPosition", at = @At("HEAD"), cancellable = true)
    private void onIsHittingPosition(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final EventHittingPosition event = new EventHittingPosition(pos);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    /*@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void onAttackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo callbackInfo) {
        EventPreAttack preEvent = new EventPreAttack(playerIn, targetEntity);
        Harakiri.get().getEventManager().dispatchEvent(preEvent);
    }*/
}
