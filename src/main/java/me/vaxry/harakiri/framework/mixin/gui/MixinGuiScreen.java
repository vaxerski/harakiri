package me.vaxry.harakiri.framework.mixin.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderTooltip;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
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
}
