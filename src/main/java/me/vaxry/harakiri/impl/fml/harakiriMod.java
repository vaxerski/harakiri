package me.vaxry.harakiri.impl.fml;

import me.vaxry.harakiri.Harakiri;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * @author Seth
 * @author noil
 * @author the entire github & discordcommunity
 * @author vax > all
 * @author Zim
 */
@Mod(modid = "harakirimod", name = "HARAKIRI", version = "1.12.2", certificateFingerprint = "7979b1d0446af2675fcb5e888851a7f32637fdb9")
public final class harakiriMod {

    public static final String VERSION = "1.7";

    /**
     * Our mods entry point
     *
     * @param event
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //initialize the client
        Harakiri.get().init();
    }
}
