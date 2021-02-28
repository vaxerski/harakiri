package me.vaxry.harakiri.framework.mixin.game;

import me.vaxry.harakiri.framework.duck.MixinTimerInterface;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author cats
 */
@Mixin(Timer.class)
public abstract class MixinTimer implements MixinTimerInterface {
    @Accessor(value = "tickLength")
    public abstract void setTickLength(float tickLength);
}
