package me.vaxry.harakiri.framework.mixin.game;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.EventFoliageColor;
import me.vaxry.harakiri.framework.event.world.EventGrassColor;
import me.vaxry.harakiri.framework.event.world.EventWaterColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColorHelper.class)
public class MixinBiomeColorHelper {
    @Inject(at = @At("HEAD"), method = "getGrassColorAtPos", cancellable = true)
    private static void onGetGrassColorAtPos(IBlockAccess blockAccess, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        final EventGrassColor event = new EventGrassColor();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(event.getColor());
        }
    }

    @Inject(at = @At("HEAD"), method = "getFoliageColorAtPos", cancellable = true)
    private static void onGetFoliageColorAtPos(IBlockAccess blockAccess, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        final EventFoliageColor event = new EventFoliageColor();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(event.getColor());
        }
    }

    @Inject(at = @At("HEAD"), method = "getWaterColorAtPos", cancellable = true)
    private static void onGetWaterColorAtPos(IBlockAccess blockAccess, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        final EventWaterColor event = new EventWaterColor();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(event.getColor());
        }
    }

}
