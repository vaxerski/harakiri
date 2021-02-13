package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockDamage;
import me.vaxry.harakiri.framework.event.render.EventRenderEntityOutlines;
import me.vaxry.harakiri.framework.event.render.EventRenderSky;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderGlobal.class, priority = 2222)
public class MixinRenderGlobal {
    @Inject(method = "isRenderEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void onIsRenderEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        final EventRenderEntityOutlines event = new EventRenderEntityOutlines();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(float partialTicks, int pass, CallbackInfo ci) {
        final EventRenderSky event = new EventRenderSky();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "drawBlockDamageTexture", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks, CallbackInfo ci) {
        final EventRenderBlockDamage event = new EventRenderBlockDamage();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }
}
