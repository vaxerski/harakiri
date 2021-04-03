package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.impl.config.ModuleConfig;

/**
 * Author Seth
 * 4/16/2019 @ 9:01 PM.
 */
public final class ToggleCommand extends Command {

    public ToggleCommand() {
        super("Toggle", new String[]{"T", "Tog"}, "Allows you to toggle modules or between two mode options", "Toggle <Module>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 5)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Harakiri.get().getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Harakiri.get().errorChat("Can't toggle " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                mod.toggle();
                Harakiri.get().logChat("Toggled " + (mod.isEnabled() ? "\247a" : "\247c") + mod.getDisplayName());
            }
            Harakiri.get().getConfigManager().save(ModuleConfig.class);
        } else {
            Harakiri.get().errorChat("Unknown module: " + "\247f\"" + split[1] + "\"");
            final Module similar = Harakiri.get().getModuleManager().findSimilar(split[1]);

            if (similar != null) {
                Harakiri.get().logChat("Did you mean: " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }

}
