package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActiveRenderInfo.class)
public abstract class MixinActiveRenderInfo {
    @Inject(remap = false, at = @At("RETURN"), method = "updateRenderInfo(Lnet/minecraft/entity/Entity;Z)V")
    private static void onUpdateRenderInfo(Entity ignored, boolean ignored2, CallbackInfo ci) {
        RenderUtil.updateModelViewProjectionMatrix();
    }
}
