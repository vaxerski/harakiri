package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventFovModifier;
import me.vaxry.harakiri.framework.event.render.EventHurtCamEffect;
import me.vaxry.harakiri.framework.event.render.EventOrientCamera;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderer.class, priority = 2147483647)
public class MixinEntityRenderer {
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V"))
    private void onRenderGameOverlay(float partialTicks, long nanoTime, CallbackInfo ci) {
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventRender2D(partialTicks, new ScaledResolution(Minecraft.getMinecraft())));
    }


    // This will call when the hand is rendered
    // @Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = {"ldc=hand"}))
    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z"))
    private void onRenderHand(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (Harakiri.INSTANCE.getCameraManager().isCameraRecording()) return;
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventRender3D(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtCameraEffect(float partialTicks, CallbackInfo ci) {
        final EventHurtCamEffect event = new EventHurtCamEffect();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    private RayTraceResult orientCameraProxy(WorldClient world, Vec3d start, Vec3d end) {
        final EventOrientCamera event = new EventOrientCamera();
        return event.isCanceled() ? null : world.rayTraceBlocks(start, end);
    }

    @Inject(method = "getFOVModifier", at = @At("HEAD"), cancellable = true)
    private void onGetFOVModifier(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        final EventFovModifier event = new EventFovModifier();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(event.getFov());
            cir.cancel();
        }
    }

    /*@Inject(at = @At("HEAD"), method = "setupFog", cancellable = true)
    private void renderFog(int startCoords, float partialTicks, CallbackInfo ci) {
        final EventSetupFog event = new EventSetupFog();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }*/

}
