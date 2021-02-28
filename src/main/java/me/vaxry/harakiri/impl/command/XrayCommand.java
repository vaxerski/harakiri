package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.impl.config.XrayConfig;
import me.vaxry.harakiri.impl.module.render.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * Author Seth
 * 4/16/2019 @ 10:42 PM.
 */
public final class XrayCommand extends Command {

    private final String[] addAlias = new String[]{"Add", "A"};
    private final String[] removeAlias = new String[]{"Remove", "Rem", "R", "Delete", "Del", "D"};
    private final String[] listAlias = new String[]{"List", "Lst"};
    private final String[] clearAlias = new String[]{"Clear", "C"};

    public XrayCommand() {
        super("Xray", new String[]{"Xray", "Xr"}, "Allows you to change what blocks are visible on xray",
                "Xray Add <Block_Name>\n" +
                        "Xray Add <ID>\n" +
                        "Xray Remove <Block_Name>\n" +
                        "Xray Remove <ID>\n" +
                        "Xray List\n" +
                        "Xray Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final XrayModule xray = (XrayModule) Harakiri.INSTANCE.getModuleManager().find(XrayModule.class);

        if (xray != null) {
            if (equals(addAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (block != null) {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                Harakiri.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            } else {
                                xray.add(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Harakiri.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Harakiri.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        } else {
                            Harakiri.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    } else {
                        Harakiri.INSTANCE.errorChat("Cannot add Air to xray");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Harakiri.INSTANCE.errorChat("Cannot add Air to xray");
                        } else {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                Harakiri.INSTANCE.logChat("Xray already contains " + block.getLocalizedName());
                            } else {
                                xray.add(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Harakiri.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Harakiri.INSTANCE.logChat("Added " + block.getLocalizedName() + " to xray");
                            }
                        }
                    } else {
                        Harakiri.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(removeAlias, split[1])) {
                if (!this.clamp(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (block != null) {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Harakiri.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Harakiri.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            } else {
                                Harakiri.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        } else {
                            Harakiri.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                        }
                    } else {
                        Harakiri.INSTANCE.errorChat("Cannot remove Air from xray");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Harakiri.INSTANCE.errorChat("Cannot remove Air from xray");
                        } else {
                            if (xray.contains(Block.getIdFromBlock(block))) {
                                xray.remove(Block.getIdFromBlock(block));
                                if (xray.isEnabled()) {
                                    xray.updateRenders();
                                }
                                Harakiri.INSTANCE.getConfigManager().save(XrayConfig.class);
                                Harakiri.INSTANCE.logChat("Removed " + block.getLocalizedName() + " from xray");
                            } else {
                                Harakiri.INSTANCE.logChat("Xray doesn't contain " + block.getLocalizedName());
                            }
                        }
                    } else {
                        Harakiri.INSTANCE.logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(listAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }

                if (xray.getIds().size() > 0) {
                    final TextComponentString msg = new TextComponentString("\2477Xray IDs: ");

                    for (int i : xray.getIds()) {
                        msg.appendSibling(new TextComponentString("\2477[\247a" + i + "\2477] ")
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(Block.getBlockById(i).getLocalizedName())))));
                    }

                    Harakiri.INSTANCE.logcChat(msg);
                } else {
                    Harakiri.INSTANCE.logChat("You don't have any xray ids");
                }
            } else if (equals(clearAlias, split[1])) {
                if (!this.clamp(input, 2, 2)) {
                    this.printUsage();
                    return;
                }
                xray.clear();
                if (xray.isEnabled()) {
                    xray.updateRenders();
                }
                Harakiri.INSTANCE.getConfigManager().save(XrayConfig.class);
                Harakiri.INSTANCE.logChat("Cleared all blocks from xray");
            } else {
                Harakiri.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        } else {
            Harakiri.INSTANCE.errorChat("Xray not present");
        }
    }
}
