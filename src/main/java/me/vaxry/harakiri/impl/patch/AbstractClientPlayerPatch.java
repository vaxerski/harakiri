package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.framework.patch.ClassPatch;

/**
 * Author Seth
 * 7/9/2019 @ 3:34 AM.
 */
public final class AbstractClientPlayerPatch extends ClassPatch {

    public AbstractClientPlayerPatch() {
        super("net.minecraft.client.entity.AbstractClientPlayer", "bua");
    }


}
