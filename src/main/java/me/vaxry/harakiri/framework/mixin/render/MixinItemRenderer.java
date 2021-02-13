package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderOverlay;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 2222)
public abstract class MixinItemRenderer {
    @Inject(at = @At("HEAD"), method = "renderSuffocationOverlay", cancellable = true)
    private void onRenderSuffocationOverlay(TextureAtlasSprite sprite, CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.BLOCK);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderWaterOverlayTexture", cancellable = true)
    private void onRenderWaterOverlayTexture(float partialTicks, CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.LIQUID);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderFireInFirstPerson", cancellable = true)
    private void onRenderFireInFirstPerson(CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.FIRE);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

}
