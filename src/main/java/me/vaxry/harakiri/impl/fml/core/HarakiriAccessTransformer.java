package me.vaxry.harakiri.impl.fml.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

/**
 * Author Seth
 * 4/5/2019 @ 1:24 AM.
 */
public final class HarakiriAccessTransformer extends AccessTransformer {

    public HarakiriAccessTransformer() throws IOException {
        super("harakiri_at.cfg");
    }

}