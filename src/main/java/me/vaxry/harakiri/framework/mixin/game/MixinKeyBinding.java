package me.vaxry.harakiri.framework.mixin.game;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {
    @Shadow public boolean pressed;

    @Inject(at = @At("HEAD"), method = "isKeyDown", cancellable = true)
    public void onIsKeyDown(CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        cir.setReturnValue(pressed);
    }
}
