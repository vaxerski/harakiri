package me.vaxry.harakiri.framework.mixin.entity;


import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    // Compiler bait
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    /*@SuppressWarnings("EqualsBetweenInconvertibleTypes")
    // Tells IDEA not to warn us that EntityPlayerSP is not a MixinEntityLivingBase
    @Inject(at = @At("HEAD"), method = "isElytraFlying", cancellable = true)
    private void onIsElytraFlying(CallbackInfoReturnable<Boolean> cir) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && player.equals(this)) {
            final EventElytraFlyCheck event = new EventElytraFlyCheck();
            Harakiri.get().getEventManager().dispatchEvent(event);
            if (event.isCanceled()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }*/
}
