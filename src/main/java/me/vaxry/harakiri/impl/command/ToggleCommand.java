package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import me.vaxry.harakiri.framework.module.Module;
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
    public void exec(String input) {
        if (!this.clamp(input, 2, 5)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Harakiri.INSTANCE.getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Harakiri.INSTANCE.errorChat("Can't toggle " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                mod.toggle();
                Harakiri.INSTANCE.logChat("Toggled " + (mod.isEnabled() ? "\247a" : "\247c") + mod.getDisplayName());
            }
            Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);
        } else {
            Harakiri.INSTANCE.errorChat("Unknown module: " + "\247f\"" + split[1] + "\"");
            final Module similar = Harakiri.INSTANCE.getModuleManager().findSimilar(split[1]);

            if (similar != null) {
                Harakiri.INSTANCE.logChat("Did you mean: " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }

}
