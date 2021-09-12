package me.vaxry.harakiri.framework.mixin.block;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockSide;
import me.vaxry.harakiri.framework.event.world.EventAddCollisionBox;
import me.vaxry.harakiri.impl.module.render.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(at = @At("HEAD"), method = "shouldSideBeRendered", cancellable = true)
    private void onShouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        // prevent horrible block place lag
        if(Harakiri.get().getXRayModule().isEnabled()) {
            final EventRenderBlockSide event = new EventRenderBlockSide((Block) (Object) this);
            Harakiri.get().getEventManager().dispatchEvent(event);
            if (event.isCanceled()) {
                cir.cancel();
                cir.setReturnValue(event.isRenderable());
            }
        }
    }

    /*@Inject(at = @At("HEAD"), method = "getRenderLayer", cancellable = true)
    private void onGetRenderLayer(CallbackInfoReturnable<BlockRenderLayer> cir) {
        final EventGetBlockLayer event = new EventGetBlockLayer((Block) (Object) this);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }*/


    @Inject(at = @At("HEAD"), method = "addCollisionBoxToList(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V", cancellable = true)
    public void onAddCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState, CallbackInfo ci) {
        final EventAddCollisionBox event = new EventAddCollisionBox(pos, entityIn);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
