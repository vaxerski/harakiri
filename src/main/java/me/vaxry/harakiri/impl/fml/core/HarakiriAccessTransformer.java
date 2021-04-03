package me.vaxry.harakiri.impl.fml.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public final class HarakiriAccessTransformer extends AccessTransformer {

    public HarakiriAccessTransformer() throws IOException {
        super("harakiri_at.cfg");
    }

}