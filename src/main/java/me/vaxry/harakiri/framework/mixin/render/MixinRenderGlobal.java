package me.vaxry.harakiri.framework.mixin.render;

import com.google.common.collect.Lists;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.render.*;
import me.vaxry.harakiri.impl.module.world.SkyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RenderGlobal.class, priority = 2222)
public class MixinRenderGlobal {
    @Inject(method = "isRenderEntityOutlines", at = @At("HEAD"), cancellable = true)
    private void onIsRenderEntityOutlines(CallbackInfoReturnable<Boolean> cir) {
        final EventRenderEntityOutlines event = new EventRenderEntityOutlines();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(float partialTicks, int pass, CallbackInfo ci) {
        final EventRenderSky event = new EventRenderSky();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "drawBlockDamageTexture", at = @At("HEAD"), cancellable = true)
    private void onDrawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks, CallbackInfo ci) {
        final EventRenderBlockDamage event = new EventRenderBlockDamage();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At("RETURN"), cancellable = true)
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci){
        final EventRenderEntities eventRenderEntities = new EventRenderEntities(EventStageable.EventStage.POST, renderViewEntity, camera, partialTicks);
        Harakiri.get().getEventManager().dispatchEvent(eventRenderEntities);
        if(eventRenderEntities.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At("HEAD"), cancellable = true)
    private void renderEntitiesPre(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci){
        final EventRenderEntities eventRenderEntities = new EventRenderEntities(EventStageable.EventStage.PRE, renderViewEntity, camera, partialTicks);
        Harakiri.get().getEventManager().dispatchEvent(eventRenderEntities);
        if(eventRenderEntities.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableFog()V", remap = false), cancellable = true)
    private void renderEntitiesMid(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci){
        final EventRenderEntities eventRenderEntities = new EventRenderEntities(EventStageable.EventStage.MID, renderViewEntity, camera, partialTicks);
        Harakiri.get().getEventManager().dispatchEvent(eventRenderEntities);
        if(eventRenderEntities.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V"))
    public void renderEntityPeri(CallbackInfo ci) {
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().getRenderManager().setRenderOutlines(true);
        final EventRenderEntities event = new EventRenderEntities(EventStageable.EventStage.RENDER1, null, null, 0);
        Harakiri.get().getEventManager().dispatchEvent(event);
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()V"))
    public void renderEntityPeri2(CallbackInfoReturnable<List<Entity>> ci) {
        // Add an entity to toggle rendering of the ESP always on.
        List<Entity> list1 = Lists.<Entity>newArrayList();
        list1.add(Minecraft.getMinecraft().player);

        ci.setReturnValue(list1);
    }

    @Redirect(method = "renderEntityOutlineFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V", remap = false))
    private void tryBlendFuncSeparate(int a, int b, int c, int d, CallbackInfo ci){
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.DST_ALPHA);
    }
}
