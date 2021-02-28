package me.vaxry.harakiri.framework.event.render;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.block.Block;

/**
 * Author Seth
 * 4/9/2019 @ 12:21 PM.
 */
public class EventRenderBlockSide extends EventCancellable {

    private Block block;
    private boolean renderable;

    public EventRenderBlockSide(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public boolean isRenderable() {
        return renderable;
    }

    public void setRenderable(boolean renderable) {
        this.renderable = renderable;
    }
}
