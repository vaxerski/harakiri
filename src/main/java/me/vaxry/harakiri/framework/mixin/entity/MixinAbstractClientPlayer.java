package me.vaxry.harakiri.framework.mixin.entity;

import com.mojang.authlib.GameProfile;
import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {

    // Makes the compiler happy
    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    /*@SuppressWarnings("all") // To keep IDEA from arguing about the "illegal" cast
    @Inject(method = "getLocationCape", cancellable = true, at = @At("HEAD"))
    public void onGetLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        EventCapeLocation event = new EventCapeLocation((AbstractClientPlayer) (EntityPlayer) this);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(event.getLocation());
        }
    }*/
}
