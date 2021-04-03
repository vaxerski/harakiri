package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class HandOffsetModule extends Module {

    public final Value<Boolean> remove = new Value<Boolean>("Remove", new String[]{"Remove", "r"}, "Do not draw them at all.", false);
    public final Value<Float> posX = new Value<Float>("PosX", new String[]{"PosX", "x"}, "PosX offset to move items by.", 0F, -2F, 2F, 0.1F);
    public final Value<Float> posY = new Value<Float>("PosY", new String[]{"PosY", "y"}, "PosY offset to move items by.", 0F, -2F, 2F, 0.1F);
    public final Value<Float> posZ = new Value<Float>("PosZ", new String[]{"PosZ", "z"}, "PosZ offset to move items by.", 0F, -2F, 2F, 0.1F);

    public HandOffsetModule(){
        super("HandOffset", new String[]{"HandOffset"}, "Sets the offset for rendering held stuff.", "NONE", -1, ModuleType.RENDER);
    }


}
