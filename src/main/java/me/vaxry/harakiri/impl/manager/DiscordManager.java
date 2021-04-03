package me.vaxry.harakiri.impl.manager;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import me.vaxry.harakiri.impl.module.misc.DiscordRPCModule;

import java.time.Instant;

public class DiscordManager {
    private DiscordRPCModule rpcModule = null;
    private Thread thread = null;

    private int startTime = 0;

    public DiscordManager(){
        startTime = (int)Instant.now().getEpochSecond();
        rpcModule = (DiscordRPCModule)Harakiri.get().getModuleManager().find(DiscordRPCModule.class);
        if(rpcModule.isEnabled())
            enable();
    }

    public void enable()
    {
        DiscordRPC lib = DiscordRPC.INSTANCE;
        String applicationId = "805861650762432592";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Ready");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = startTime; // epoch second
        lib.Discord_UpdatePresence(presence);
        presence.largeImageKey = "icon";
        presence.largeImageText = "Harakiri v" + harakiriMod.VERSION;
        thread = new Thread(() ->
        {
            while (!Thread.currentThread().isInterrupted())
            {
                lib.Discord_RunCallbacks();
                presence.details = rpcModule.getRPCDetails();
                presence.state = rpcModule.getRPCStatus();
                lib.Discord_UpdatePresence(presence);
                try
                {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored)
                {
                }
            }
        }, "RPC-Callback-Handler");

        thread.start();
    }

    public void disable() throws InterruptedException
    {
        if (thread != null) {
            DiscordRPC lib = DiscordRPC.INSTANCE;
            lib.Discord_Shutdown();
            thread.interrupt();
            thread = null;
        }
    }
}
