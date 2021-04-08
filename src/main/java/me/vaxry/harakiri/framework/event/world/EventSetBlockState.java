package me.vaxry.harakiri.framework.event.world;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class EventSetBlockState extends EventCancellable {
    public BlockPos pos;
    public IBlockState newstate;
    public int flags;

    public EventSetBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        this.pos = pos;
        newstate = newState;
        this.flags = flags;
    }
}
