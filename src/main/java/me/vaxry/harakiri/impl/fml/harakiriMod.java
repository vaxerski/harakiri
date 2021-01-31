package me.vaxry.harakiri.impl.fml;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.extd.MiscExtd;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;

/**
 * @author Seth
 * @author noil
 * @author the entire github & discordcommunity
 */
@Mod(modid = "harakirimod", name = "HARAKIRI", version = "1.12.2", certificateFingerprint = "7979b1d0446af2675fcb5e888851a7f32637fdb9")
public final class harakiriMod {

    public static final String VERSION = "1.5-dev";

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
