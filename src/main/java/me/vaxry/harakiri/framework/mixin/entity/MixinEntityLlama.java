package me.vaxry.harakiri.framework.mixin.entity;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.entity.EventSteerEntity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLlama.class)
public abstract class MixinEntityLlama extends EntityAnimal {

    // Makes the compiler happy
    public MixinEntityLlama(World worldIn) {
        super(worldIn);
    }

    // Allows steering without a saddle
    @Inject(at = @At("HEAD"), method = "canBeSteered", cancellable = true)
    public void onCanBeSteered(CallbackInfoReturnable<Boolean> cir) {
        final EventSteerEntity event = new EventSteerEntity();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
