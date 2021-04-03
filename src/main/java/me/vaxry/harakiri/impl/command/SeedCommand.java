package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.impl.config.WorldConfig;
import me.vaxry.harakiri.impl.manager.WorldManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

/**
 * Author Seth
 * 6/11/2019 @ 7:15 AM.
 */
public final class SeedCommand extends Command {

    public SeedCommand() {
        super("Seed", new String[]{"RandomSeed"}, "Sets the client-side seed.", "Seed <Number>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (StringUtil.isLong(split[1], 10)) {
            final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
            if (serverData != null) {
                final WorldManager.WorldData worldData = Harakiri.get().getWorldManager().find(serverData.serverIP);
                if (worldData != null) {
                    final long seed = Long.parseLong(split[1]);
                    worldData.setSeed(seed);
                    Harakiri.get().logChat("Set " + serverData.serverIP + "'s seed to " + seed);
                } else {
                    final long seed = Long.parseLong(split[1]);
                    Harakiri.get().getWorldManager().getWorldDataList().add(new WorldManager.WorldData(serverData.serverIP, seed));
                    Harakiri.get().logChat("Set " + serverData.serverIP + "'s seed to " + seed);
                }
                Harakiri.get().getConfigManager().save(WorldConfig.class);
            } else {
                Harakiri.get().errorChat("Cannot set seed for localhost");
            }
        } else {
            Harakiri.get().errorChat("Unknown number " + "\247f\"" + split[1] + "\"");
        }
    }
}
