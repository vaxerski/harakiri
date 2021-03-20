package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.entity.EventSetupFog;
import me.vaxry.harakiri.framework.event.player.EventFovModifier;
import me.vaxry.harakiri.framework.event.render.EventHurtCamEffect;
import me.vaxry.harakiri.framework.event.render.EventOrientCamera;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.impl.module.render.ESPModule;
import me.vaxry.harakiri.impl.module.render.NoOverlayModule;
import me.vaxry.harakiri.impl.module.render.StorageESPModule;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderer.class, priority = 2147483647)
public class MixinEntityRenderer {
    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntityOutlineFramebuffer()V"))
    private void onRenderGameOverlay(CallbackInfo ci) {
        if(Harakiri.get().getModuleManager().find(ESPModule.class).isEnabled() || Harakiri.get().getModuleManager().find(StorageESPModule.class).isEnabled())
            ((ESPModule)Harakiri.get().getModuleManager().find(ESPModule.class)).renderFramebuffer();
    }


    // This will call when the hand is rendered
    // @Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = {"ldc=hand"}))
    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z"))
    private void onRenderHand(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (Harakiri.get().getCameraManager().isCameraRecording()) return;
        Harakiri.get().getEventManager().dispatchEvent(new EventRender3D(partialTicks));
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtCameraEffect(float partialTicks, CallbackInfo ci) {
        final EventHurtCamEffect event = new EventHurtCamEffect();
        Harakiri.get().getEventManager().dispatchEvent(event);
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
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(event.getFov());
            cir.cancel();
        }
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    private IBlockState setupFog(World world, Entity start, float partialticks) {
        Harakiri.get().getEventManager().dispatchEvent(new EventSetupFog(world, start, partialticks));
        IBlockState iBlockState = ActiveRenderInfo.getBlockStateAtEntityViewpoint(world, start, partialticks);

        if(((NoOverlayModule)Harakiri.get().getModuleManager().find(NoOverlayModule.class)).lava.getValue()){
            if(iBlockState.getMaterial() == Material.LAVA) iBlockState = Minecraft.getMinecraft().world.getBlockState(new BlockPos(
                    Minecraft.getMinecraft().player.getPosition().getX(),
                    257,
                    Minecraft.getMinecraft().player.getPosition().getZ()));
        }else if(((NoOverlayModule)Harakiri.get().getModuleManager().find(NoOverlayModule.class)).water.getValue()){
            if(iBlockState.getMaterial() == Material.WATER) iBlockState = Minecraft.getMinecraft().world.getBlockState(new BlockPos(
                    Minecraft.getMinecraft().player.getPosition().getX(),
                    257,
                    Minecraft.getMinecraft().player.getPosition().getZ()));
        }
        return iBlockState;
    }

    /*@Inject(at = @At("HEAD"), method = "setupFog", cancellable = true)
    private void renderFog(int startCoords, float partialTicks, CallbackInfo ci) {
        final EventSetupFog event = new EventSetupFog();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }*/

}
