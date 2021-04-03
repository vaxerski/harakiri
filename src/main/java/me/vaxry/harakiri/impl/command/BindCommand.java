package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.impl.config.ModuleConfig;
import org.lwjgl.input.Keyboard;

/**
 * Author Seth
 * 4/16/2019 @ 10:22 PM.
 */
public final class BindCommand extends Command {

    private String[] clearAlias = new String[]{"Clear", "C"};

    public BindCommand() {
        super("Bind", new String[]{"B"}, "Allows you to bind modules.", "Bind <module> <key>\nBind Clear");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        if (equals(clearAlias, split[1])) {
            if (!this.verifyInput(input, 2, 2)) {
                this.printUsage();
                return;
            }

            int count = 0;

            for (Module mod : Harakiri.get().getModuleManager().getModuleList()) {
                if (mod.getType() != Module.ModuleType.HIDDEN && mod.getKey() != null && !mod.getKey().equals("NONE")) {
                    count++;
                    mod.setKey("NONE");
                }
            }

            if (count > 0) {
                Harakiri.get().logChat("Removed " + count + " Bind" + (count > 1 ? "s" : ""));
            } else {
                Harakiri.get().logChat("You have no binds");
            }
        } else {
            if (!this.verifyInput(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final Module mod = Harakiri.get().getModuleManager().find(split[1]);

            if (mod != null) {
                if (mod.getType() == Module.ModuleType.HIDDEN) {
                    Harakiri.get().errorChat("Cannot change bind of " + "\247f\"" + mod.getDisplayName() + "\"");
                } else {
                    if (split[2].equalsIgnoreCase(mod.getKey())) {
                        Harakiri.get().logChat("\247c" + mod.getDisplayName() + "'s\247f key is already " + split[2].toUpperCase());
                    } else {
                        if (split[2].equalsIgnoreCase("NONE")) {
                            Harakiri.get().logChat("Bound \247c" + mod.getDisplayName() + "\247f to " + split[2].toUpperCase());
                            mod.setKey(split[2].toUpperCase());
                            Harakiri.get().getConfigManager().save(ModuleConfig.class);
                        } else if (Keyboard.getKeyIndex(split[2].toUpperCase()) != Keyboard.KEY_NONE) {
                            Harakiri.get().logChat("Bound \247c" + mod.getDisplayName() + "\247f to " + split[2].toUpperCase());
                            mod.setKey(split[2].toUpperCase());
                            Harakiri.get().getConfigManager().save(ModuleConfig.class);
                        } else {
                            Harakiri.get().logChat("\247c" + split[2] + "\247f is not a valid key");
                        }
                    }
                }
            } else {
                Harakiri.get().errorChat("Unknown module: " + "\247f\"" + split[1] + "\"");
                final Module similar = Harakiri.get().getModuleManager().findSimilar(split[1]);
                if (similar != null) {
                    Harakiri.get().logChat("Did you mean " + "\247c" + similar.getDisplayName() + "\247f?");
                }
            }
        }
    }
}
