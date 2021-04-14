package me.vaxry.harakiri.framework.mixin.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderSky;
import me.vaxry.harakiri.framework.event.render.EventRenderTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRenderDispatcher {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(TileEntity te, float pt, int deststage, CallbackInfo ci){
        final EventRenderTileEntity event = new EventRenderTileEntity(te, pt, deststage);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }
}
