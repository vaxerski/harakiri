package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.impl.config.ModuleConfig;

/**
 * Author Ice
 * 5/06/2020 @ 18:14 PM.
 */
public final class RenameModuleCommand extends Command {

    public RenameModuleCommand() {
        super("RenameModule", new String[]{"rm", "renamemod", "renamemodule"}, "Rename modules.", "renamemodule <module> <name>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 3, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String originalModuleName = split[1];
        final String newModuleName = split[2];

        if (Harakiri.INSTANCE.getModuleManager().find(originalModuleName) != null) {
            final Module mod = Harakiri.INSTANCE.getModuleManager().find(originalModuleName);
            if (mod != null) {
                mod.setDisplayName(newModuleName);

                Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);
                Harakiri.INSTANCE.logChat("Set " + originalModuleName + " custom alias to " + newModuleName);
            }
        } else {
            Harakiri.INSTANCE.logChat(originalModuleName + " does not exist!");
        }


    }
}

