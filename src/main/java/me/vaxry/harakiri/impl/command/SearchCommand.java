package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.impl.config.SearchConfig;
import me.vaxry.harakiri.impl.module.render.SearchModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

/**
 * @author noil
 */
public final class SearchCommand extends Command {

    private final String[] addAlias = new String[]{"Add"};
    private final String[] removeAlias = new String[]{"Remove", "Rem", "Delete", "Del"};
    private final String[] listAlias = new String[]{"List", "Lst"};
    private final String[] clearAlias = new String[]{"Clear", "clr"};

    public SearchCommand() {
        super("Search", new String[]{"find", "locate"}, "Allows you to change what blocks are visible on search",
                "Search Add <block_name>\n" +
                        "Search Add <id>\n" +
                        "Search Remove <block_name>\n" +
                        "Search Remove <id>\n" +
                        "Search List\n" +
                        "Search Clear");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 3)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final SearchModule searchModule = (SearchModule) Harakiri.get().getModuleManager().find(SearchModule.class);

        if (searchModule != null) {
            if (equals(addAlias, split[1])) {
                if (!this.verifyInput(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (searchModule.contains(Block.getIdFromBlock(block))) {
                            Harakiri.get().logChat("Search already contains " + block.getLocalizedName());
                        } else {
                            if(block == Blocks.BED){
                                searchModule.add("bed");
                            }
                            searchModule.add(Block.getIdFromBlock(block));
                            if (searchModule.isEnabled()) {
                                searchModule.clearBlocks();
                                searchModule.updateRenders();
                            }
                            Harakiri.get().getConfigManager().save(SearchConfig.class);
                            Harakiri.get().logChat("Added " + block.getLocalizedName() + " to search");
                        }
                    } else {
                        Harakiri.get().errorChat("Cannot add Air to search");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Harakiri.get().errorChat("Cannot add Air to search");
                        } else {
                            if (searchModule.contains(Block.getIdFromBlock(block))) {
                                Harakiri.get().logChat("Search already contains " + block.getLocalizedName());
                            } else {
                                if(block == Blocks.BED){
                                    searchModule.add("bed");
                                }
                                searchModule.add(Block.getIdFromBlock(block));
                                if (searchModule.isEnabled()) {
                                    searchModule.clearBlocks();
                                    searchModule.updateRenders();
                                }
                                Harakiri.get().getConfigManager().save(SearchConfig.class);
                                Harakiri.get().logChat("Added " + block.getLocalizedName() + " to search");
                            }
                        }
                    } else {
                        Harakiri.get().logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(removeAlias, split[1])) {
                if (!this.verifyInput(input, 3, 3)) {
                    this.printUsage();
                    return;
                }

                if (StringUtil.isInt(split[2])) {
                    final int id = Integer.parseInt(split[2]);

                    if (id > 0) {
                        final Block block = Block.getBlockById(id);

                        if (searchModule.contains(Block.getIdFromBlock(block))) {
                            if(block == Blocks.BED){
                                searchModule.remove("bed");
                            }
                            searchModule.remove(Block.getIdFromBlock(block));
                            if (searchModule.isEnabled()) {
                                searchModule.clearBlocks();
                                searchModule.updateRenders();
                            }
                            Harakiri.get().getConfigManager().save(SearchConfig.class);
                            Harakiri.get().logChat("Removed " + block.getLocalizedName() + " from search");
                        } else {
                            Harakiri.get().logChat("Search doesn't contain " + block.getLocalizedName());
                        }
                    } else {
                        Harakiri.get().errorChat("Cannot remove Air from search");
                    }
                } else {
                    final Block block = Block.getBlockFromName(split[2].toLowerCase());

                    if (block != null) {
                        if (block == Blocks.AIR) {
                            Harakiri.get().errorChat("Cannot remove Air from search");
                        } else {
                            if (searchModule.contains(Block.getIdFromBlock(block))) {
                                if(block == Blocks.BED){
                                    searchModule.remove("bed");
                                }
                                searchModule.remove(Block.getIdFromBlock(block));
                                if (searchModule.isEnabled()) {
                                    searchModule.clearBlocks();
                                    searchModule.updateRenders();
                                }
                                Harakiri.get().getConfigManager().save(SearchConfig.class);
                                Harakiri.get().logChat("Removed " + block.getLocalizedName() + " from search");
                            } else {
                                Harakiri.get().logChat("Search doesn't contain " + block.getLocalizedName());
                            }
                        }
                    } else {
                        Harakiri.get().logChat("\247c" + split[2] + "\247f is not a valid block");
                    }
                }
            } else if (equals(listAlias, split[1])) {
                if (!this.verifyInput(input, 2, 2)) {
                    this.printUsage();
                    return;
                }

                if (searchModule.getIds().size() > 0) {
                    final TextComponentString msg = new TextComponentString("\2477Search IDs: ");

                    for (int i : searchModule.getIds()) {
                        msg.appendSibling(new TextComponentString("\2477[\247a" + i + "\2477] ")
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(Block.getBlockById(i).getLocalizedName())))));
                    }

                    Harakiri.get().logcChat(msg);
                } else {
                    Harakiri.get().logChat("You don't have any search ids");
                }
            } else if (equals(clearAlias, split[1])) {
                if (!this.verifyInput(input, 2, 2)) {
                    this.printUsage();
                    return;
                }
                searchModule.clear();
                if (searchModule.isEnabled()) {
                    searchModule.clearBlocks();
                    searchModule.updateRenders();
                }
                Harakiri.get().getConfigManager().save(SearchConfig.class);
                Harakiri.get().logChat("Cleared all blocks from search");
            } else {
                Harakiri.get().errorChat("Unknown input " + "\247f\"" + input + "\"");
                this.printUsage();
            }
        } else {
            Harakiri.get().errorChat("Search not present");
        }
    }
}

