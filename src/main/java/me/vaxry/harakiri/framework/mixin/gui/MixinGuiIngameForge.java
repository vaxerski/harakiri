package me.vaxry.harakiri.framework.mixin.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderHelmet;
import me.vaxry.harakiri.framework.event.gui.EventRenderPortal;
import me.vaxry.harakiri.framework.event.gui.EventRenderPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge extends GuiIngame {
    public MixinGuiIngameForge(Minecraft mcIn) {
        super(mcIn);
    }

    @Inject(method = "renderPortal", cancellable = true, at = @At("HEAD"))
    private void onRenderPortal(ScaledResolution res, float partialTicks, CallbackInfo ci) {
        final EventRenderPortal event = new EventRenderPortal();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderPotionIcons", cancellable = true, at = @At("HEAD"))
    private void onRenderPotionIcons(ScaledResolution resolution, CallbackInfo ci) {
        final EventRenderPotions event = new EventRenderPotions();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderHelmet", cancellable = true, at = @At("HEAD"))
    private void onRenderHelmet(ScaledResolution res, float partialTicks, CallbackInfo ci) {
        final EventRenderHelmet event = new EventRenderHelmet();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

}
