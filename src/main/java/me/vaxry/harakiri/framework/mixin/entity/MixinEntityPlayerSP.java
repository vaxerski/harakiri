package me.vaxry.harakiri.framework.mixin.entity;

import com.mojang.authlib.GameProfile;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.*;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author cats
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    // Compiler bait
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Shadow
    protected abstract void updateAutoJump(float p_189810_1_, float p_189810_2_);

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    private void onUpdatePre(CallbackInfo ci) {
        EventPlayerUpdate event = new EventPlayerUpdate(EventStageable.EventStage.PRE);
        Harakiri.get().getCameraManager().update();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void onUpdatePost(CallbackInfo ci) {
        EventPlayerUpdate event = new EventPlayerUpdate(EventStageable.EventStage.POST);
        Harakiri.get().getEventManager().dispatchEvent(event);
    }


    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayerPre(CallbackInfo ci) {
        Harakiri.get().getRotationManager().updateRotations();
        Harakiri.get().getPositionManager().updatePosition();
        EventUpdateWalkingPlayer event = new EventUpdateWalkingPlayer(EventStageable.EventStage.PRE);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void onUpdateWalkingPlayerPost(CallbackInfo ci) {
        EventUpdateWalkingPlayer event = new EventUpdateWalkingPlayer(EventStageable.EventStage.POST);
        Harakiri.get().getEventManager().dispatchEvent(event);

        Harakiri.get().getPositionManager().restorePosition();
        Harakiri.get().getRotationManager().restoreRotations();
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        EventSendChatMessage event = new EventSendChatMessage(message);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void onSwingArm(EnumHand hand, CallbackInfo ci) {
        final EventSwingArm event = new EventSwingArm(hand);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "closeScreen", at = @At("HEAD"), cancellable = true)
    private void onCloseScreen(CallbackInfo ci) {
        final EventCloseScreen event = new EventCloseScreen();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        final EventPushOutOfBlocks event = new EventPushOutOfBlocks();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "onLivingUpdate", at = @At(remap = false, value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onInputUpdate(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovementInput;)V"))
    private void onUpdateInput(CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventUpdateInput());
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void onMove(MoverType type, double x, double y, double z, CallbackInfo ci) {
        ci.cancel();
        double d0 = posX;
        double d1 = posZ;
        final EventMove event = new EventMove(type, x, y, z);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) return;
        super.move(type, event.getX(), event.getY(), event.getZ());
        updateAutoJump((float) (posX - d0), (float) (posZ - d1));
    }
}
