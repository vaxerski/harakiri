package me.vaxry.harakiri.framework.util;

import net.minecraft.enchantment.Enchantment;

public final class ItemUtil {

    public static boolean isIllegalEnchant(Enchantment enc, short lvl) {
        final int maxPossibleLevel = enc.getMaxLevel();
        if (lvl == 0 || lvl > maxPossibleLevel)
            return true;

        return lvl == Short.MAX_VALUE;
    }
}
