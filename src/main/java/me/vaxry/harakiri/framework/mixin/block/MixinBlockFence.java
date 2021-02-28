package me.vaxry.harakiri.framework.mixin.block;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.EventAddCollisionBox;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockFence.class)
public abstract class MixinBlockFence extends Block {
    // To make the compiler happy
    public MixinBlockFence(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    @Inject(at = @At("HEAD"), method = "addCollisionBoxToList", cancellable = true)
    public void onAddCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState, CallbackInfo ci) {
        final EventAddCollisionBox event = new EventAddCollisionBox(pos, entityIn);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
