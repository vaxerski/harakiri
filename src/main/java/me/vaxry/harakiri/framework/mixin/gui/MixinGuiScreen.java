package me.vaxry.harakiri.framework.mixin.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderTooltip;
import me.vaxry.harakiri.impl.module.render.GuiPlusModule;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiScreen.class, priority = 2147483647)
public abstract class MixinGuiScreen extends Gui {
    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    private void onRenderTooltip(ItemStack stack, int x, int y, CallbackInfo ci) {
        final EventRenderTooltip event = new EventRenderTooltip(stack, x, y);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "drawDefaultBackground", at = @At("HEAD"), cancellable = true)
    public void drawDefaultBackground(CallbackInfo ci){
        final GuiPlusModule guiPlusModule = (GuiPlusModule)Harakiri.get().getModuleManager().find(GuiPlusModule.class);

        assert guiPlusModule != null;
        if(guiPlusModule.removeBackground.getValue() && guiPlusModule.isEnabled()) {
            ci.cancel();

            // Fix borders when half-transparent textures
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
        }

        if(Harakiri.get().getUsername().equalsIgnoreCase(""))
            Harakiri.get().getApiManager().killThisThing(); // Anti crack, some sort of
    }
}
