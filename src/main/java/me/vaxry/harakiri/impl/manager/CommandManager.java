package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.command.*;
import me.vaxry.harakiri.impl.config.ModuleConfig;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CommandManager {

    private List<Command> commandList = new ArrayList<>();

    public CommandManager() {
        this.commandList.add(new ToggleCommand());
        this.commandList.add(new VClipCommand());
        this.commandList.add(new HClipCommand());
        this.commandList.add(new BindCommand());
        this.commandList.add(new CreateConfigCommand());
        this.commandList.add(new XrayCommand());
        this.commandList.add(new FixKeybindsCommand());
        this.commandList.add(new FriendCommand());
        this.commandList.add(new PeekCommand());
        this.commandList.add(new SpectateCommand());
        this.commandList.add(new YawCommand());
        this.commandList.add(new PitchCommand());
        this.commandList.add(new SayCommand());
        this.commandList.add(new NameCommand());
        this.commandList.add(new MacroCommand());
        this.commandList.add(new SeedCommand());
        this.commandList.add(new FakeChatCommand());
        this.commandList.add(new SignBookCommand());
        this.commandList.add(new SearchCommand());

        //create commands for every value within every module
        loadValueCommands();

        //load our external commands
        //loadExternalCommands();

        commandList.sort(Comparator.comparing(Command::getDisplayName));
    }

    public void loadValueCommands() {
        for (final Module module : Harakiri.get().getModuleManager().getModuleList()) {
            if (module.getValueList().size() > 0) {
                this.commandList.add(new Command(module.getDisplayName(), module.getAlias(), module.getDesc() != null ? module.getDesc() : "There is no description for this command", module.toUsageTextComponent()) {

                    @Override
                    public TextComponentString getTextComponentUsage() {
                        return module.toUsageTextComponent();
                    }

                    @Override
                    public void run(String input) {
                        if (!this.verifyInput(input, 2, 3)) {
                            this.printUsage();
                            return;
                        }

                        final String[] split = input.split(" ");

                        final Value v = module.findValue(split[1]);

                        if (v != null) {
                            if (v.getValue() instanceof Boolean) {
                                if (split.length == 3) {
                                    if (split[2].equalsIgnoreCase("true") || split[2].equalsIgnoreCase("false") || split[2].equalsIgnoreCase("1") || split[2].equalsIgnoreCase("0")) {
                                        if (split[2].equalsIgnoreCase("1")) {
                                            v.setValue(true);
                                            Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247atrue");
                                            Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                        } else if (split[2].equalsIgnoreCase("0")) {
                                            v.setValue(false);
                                            Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247cfalse");
                                            Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                        } else {
                                            v.setValue(Boolean.parseBoolean(split[2]));
                                            Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to " + ((Boolean) v.getValue() ? "\247a" : "\247c") + v.getValue());
                                            Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                        }
                                    } else {
                                        Harakiri.get().errorChat("Invalid input " + "\"" + split[2] + "\" expected true/false");
                                    }
                                } else {
                                    v.setValue(!((Boolean) v.getValue()));
                                    Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to " + ((Boolean) v.getValue() ? "\247a" : "\247c") + v.getValue());
                                    Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                }
                            }

                            if (v.getValue() instanceof String) {
                                if (!this.verifyInput(input, 3, 3)) {
                                    this.printUsage();
                                    return;
                                }
                                v.setValue(split[2]);
                                Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to " + split[2]);
                                Harakiri.get().getConfigManager().save(ModuleConfig.class);
                            }

                            if (v.getValue() instanceof Number && !(v.getValue() instanceof Enum)) {
                                if (!this.verifyInput(input, 3, 3)) {
                                    this.printUsage();
                                    return;
                                }
                                if (v.getValue().getClass() == Float.class) {
                                    if (StringUtil.isFloat(split[2])) {
                                        v.setValue(Float.parseFloat(split[2]));
                                        Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247b" + Float.parseFloat(split[2]));
                                        Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                    } else {
                                        Harakiri.get().errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                                if (v.getValue().getClass() == Double.class) {
                                    if (StringUtil.isDouble(split[2])) {
                                        v.setValue(Double.parseDouble(split[2]));
                                        Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247b" + Double.parseDouble(split[2]));
                                        Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                    } else {
                                        Harakiri.get().errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                                if (v.getValue().getClass() == Integer.class) {
                                    if (StringUtil.isInt(split[2])) {
                                        v.setValue(Integer.parseInt(split[2]));
                                        Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247b" + Integer.parseInt(split[2]));
                                        Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                    } else {
                                        Harakiri.get().errorChat("Invalid input " + "\"" + split[2] + "\" expected a number");
                                    }
                                }
                            }

                            if (v.getValue() instanceof Enum) {
                                if (!this.verifyInput(input, 3, 3) || split[2].matches("-?\\d+(\\.\\d+)?")) { // is a number?
                                    this.printUsage();
                                    return;
                                }

                                final int op = v.getEnum(split[2]);

                                if (op != -1) {
                                    v.setEnumValue(split[2]);
                                    Harakiri.get().logChat(module.getDisplayName() + " \2477" + v.getName() + "\247f set to \247e" + ((Enum) v.getValue()).name().toLowerCase());
                                    Harakiri.get().getConfigManager().save(ModuleConfig.class);
                                } else {
                                    Harakiri.get().errorChat("Invalid input " + "\"" + split[2] + "\" expected a string");
                                }
                            }
                        } else {
                            Harakiri.get().errorChat("Invalid input " + "\"" + split[1] + "\"");
                            this.printUsage();
                        }
                    }
                });
            }
        }
    }

    /**
     * Returns a given command based on display name or alias
     *
     * @param alias
     * @return
     */
    public Command find(String alias) {
        for (Command cmd : this.getCommandList()) {
            for (String s : cmd.getAlias()) {
                if (alias.equalsIgnoreCase(s) || alias.equalsIgnoreCase(cmd.getDisplayName())) {
                    return cmd;
                }
            }
        }
        return null;
    }

    /**
     * Returns the most similar command based on display name or alias
     *
     * @param input
     * @return
     */
    public Command findSimilar(String input) {
        Command cmd = null;
        double similarity = 0.0f;

        for (Command command : this.getCommandList()) {
            final double currentSimilarity = StringUtil.levenshteinDistance(input, command.getDisplayName());

            if (currentSimilarity >= similarity) {
                similarity = currentSimilarity;
                cmd = command;
            }
        }

        return cmd;
    }

    public void unload() {
        for (Command cmd : this.commandList) {
            Harakiri.get().getEventManager().removeEventListener(cmd);
        }
        this.commandList.clear();
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<Command> commandList) {
        this.commandList = commandList;
    }
}
