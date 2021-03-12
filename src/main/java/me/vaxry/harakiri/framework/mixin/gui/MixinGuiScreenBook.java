package me.vaxry.harakiri.framework.mixin.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventBookPage;
import me.vaxry.harakiri.framework.event.gui.EventBookTitle;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreenBook.class)
public abstract class MixinGuiScreenBook extends GuiScreen {
    @Shadow private String bookTitle;

    @Shadow
    protected abstract String pageGetCurrent();

    @Shadow
    protected abstract void pageSetCurrent(String p_146457_1_);

    @Inject(method = "pageInsertIntoCurrent", at = @At("HEAD"), cancellable = true)
    private void onPageInsertIntoCurrent(String p_146459_1_, CallbackInfo ci) {
        ci.cancel();

        final EventBookPage event = new EventBookPage(p_146459_1_);
        Harakiri.get().getEventManager().dispatchEvent(event);
        String s1 = pageGetCurrent() + event.getPage();
        int i = fontRenderer.getWordWrappedHeight(s1 + "" + TextFormatting.BLACK + "_", 118);
        if (i <= 128 && s1.length() < 256) {
            this.pageSetCurrent(s1);
        }
    }

    @Inject(method = "keyTypedInTitle", at = @At("HEAD"))
    private void onKeyTypedInTitle(char typedChar, int keyCode, CallbackInfo ci) {
        final EventBookTitle event = new EventBookTitle(bookTitle);
        Harakiri.get().getEventManager().dispatchEvent(event);
        bookTitle = event.getTitle();
    }

}
