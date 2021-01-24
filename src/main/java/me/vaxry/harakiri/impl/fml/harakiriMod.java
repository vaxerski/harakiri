package me.vaxry.harakiri.impl.fml;

import me.vaxry.harakiri.Harakiri;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * @author Seth
 * @author noil
 * @author the entire github & discordcommunity
 */
@Mod(modid = "harakirimod", name = "HARAKIRI", version = harakiriMod.VERSION, certificateFingerprint = "7979b1d0446af2675fcb5e888851a7f32637fdb9")
public final class harakiriMod {


    public static final String VERSION = "1.4dev";

    /**
     * Our mods entry point
     *
     * @param event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //initialize the client
        Harakiri.INSTANCE.init();
    }

}
