package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.StringUtil;
import me.vaxry.harakiri.impl.config.ModuleConfig;

/**
 * Author Seth
 * 4/16/2019 @ 10:17 PM.
 */
public final class ColorCommand extends Command {

    public ColorCommand() {
        super("Color", new String[]{"Col", "Colour"}, "Allows you to change arraylist colors", "Color <Module> <Hex>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 3, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final Module mod = Harakiri.INSTANCE.getModuleManager().find(split[1]);

        if (mod != null) {
            if (mod.getType() == Module.ModuleType.HIDDEN) {
                Harakiri.INSTANCE.errorChat("Cannot change color of " + "\247f\"" + mod.getDisplayName() + "\"");
            } else {
                if (StringUtil.isLong(split[2], 16)) {
                    Harakiri.INSTANCE.logChat("\247c" + mod.getDisplayName() + "\247f color has been set to " + split[2].toUpperCase());
                    mod.setColor((int) Long.parseLong(split[2], 16));
                    Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);
                } else {
                    Harakiri.INSTANCE.errorChat("Invalid input " + "\"" + split[2] + "\" expected a hex value");
                }
            }
        } else {
            Harakiri.INSTANCE.errorChat("Unknown module " + "\247f\"" + split[1] + "\"");
            final Module similar = Harakiri.INSTANCE.getModuleManager().findSimilar(split[1]);
            if (similar != null) {
                Harakiri.INSTANCE.logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
            }
        }
    }
}
