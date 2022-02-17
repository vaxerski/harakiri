package me.vaxry.harakiri.impl.module.player;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public final class HighwayAutoUnpackModule extends Module {

    public final Value<Boolean> pickupLost = new Value<Boolean>("PickupLost", new String[]{"P"}, "Pick up lost shulkers.", false);

    public HighwayAutoUnpackModule() {
        super("HighwayAutoUnpack", new String[] { "HighwayAutoUnpack" }, "Automatically unpacks a pickaxe on a pickaxe being broken (Highway)", "NONE", -1, ModuleType.PLAYER);
    }

    // its handled in Nuker
}
