package me.vaxry.harakiri.framework.mixin.render;

import com.google.common.base.MoreObjects;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderOverlay;
import me.vaxry.harakiri.impl.module.render.HandOffsetModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 2147483647)
public abstract class MixinItemRenderer {
    @Inject(at = @At("HEAD"), method = "renderSuffocationOverlay", cancellable = true)
    private void onRenderSuffocationOverlay(TextureAtlasSprite sprite, CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.BLOCK);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderWaterOverlayTexture", cancellable = true)
    private void onRenderWaterOverlayTexture(float partialTicks, CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.LIQUID);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderFireInFirstPerson", cancellable = true)
    private void onRenderFireInFirstPerson(CallbackInfo ci) {
        final EventRenderOverlay event = new EventRenderOverlay(EventRenderOverlay.OverlayType.FIRE);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderItemInFirstPerson", cancellable = true)
    public void renderItemInFirstPerson(float p_187457_7_, CallbackInfo ci){
        HandOffsetModule handOffsetModule = (HandOffsetModule)Harakiri.get().getModuleManager().find(HandOffsetModule.class);

        if(handOffsetModule == null)
            return;


        if(handOffsetModule.isEnabled()){
            if(handOffsetModule.remove.getValue()){
                ci.cancel();
                return;
            }

            GlStateManager.translate(handOffsetModule.posX.getValue(), handOffsetModule.posY.getValue(), handOffsetModule.posZ.getValue());
        }
    }

    @Inject(at = @At("RETURN"), method = "renderItemInFirstPerson")
    public void renderItemInFirstPersonPost(float p_187457_7_, CallbackInfo ci) {
        HandOffsetModule handOffsetModule = (HandOffsetModule)Harakiri.get().getModuleManager().find(HandOffsetModule.class);

        if(handOffsetModule == null)
            return;

        if(handOffsetModule.isEnabled()){
            if(handOffsetModule.remove.getValue()){
                return;
            }

            GlStateManager.translate(-handOffsetModule.posX.getValue(), -handOffsetModule.posY.getValue(), -handOffsetModule.posZ.getValue());
        }
    }
}
