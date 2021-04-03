package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderTooltip;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Mouse;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class ShulkerPreviewModule extends Module {

    public final Value<Boolean> middleClick = new Value("MiddleClick", new String[]{"MC", "Mid"}, "Middle click to peek.", true);

    private boolean clicked;

    public ShulkerPreviewModule() {
        super("ShulkerPreview", new String[]{"SPreview", "ShulkerView"}, "Shows the insides of a shulker box.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRenderTooltip(EventRenderTooltip event) {
        if (event.getItemStack() == null)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        if (event.getItemStack().getItem() instanceof ItemShulkerBox) {
            ItemStack shulker = event.getItemStack();
            NBTTagCompound tagCompound = shulker.getTagCompound();
            if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10)) {
                NBTTagCompound blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag");
                if (blockEntityTag.hasKey("Items", 9)) {
                    event.setCanceled(true); // cancel rendering the old tooltip

                    NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(27, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist); // load the itemstacks from the tag to the list

                    // store mouse/event coords
                    int x = event.getX();
                    int y = event.getY();

                    // translate to mouse x, y
                    GlStateManager.translate(x + 10, y - 5, 0);

                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    // background
                    RenderUtil.drawRect(-3, -Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 4, 9 * 16 + 3, 3 * 16 + 3, 0x11101010); //0x99
                    RenderUtil.drawRect(-2, -Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 3, 9 * 16 + 2, 3 * 16 + 2, 0x77202020); //0xFF
                    RenderUtil.drawRect(0, 0, 9 * 16, 3 * 16, 0x66101010); //0xff

                    // text
                    Harakiri.get().getTTFFontUtil().drawStringWithShadow(shulker.getDisplayName(), 0, -Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 1, 0xFFFFFFFF);

                    GlStateManager.enableDepth();
                    mc.getRenderItem().zLevel = 150.0F;
                    RenderHelper.enableGUIStandardItemLighting();

                    // loop through items in shulker inventory
                    for (int i = 0; i < nonnulllist.size(); i++) {
                        ItemStack itemStack = nonnulllist.get(i);
                        int offsetX = (i % 9) * 16;
                        int offsetY = (i / 9) * 16;
                        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
                    }

                    RenderHelper.disableStandardItemLighting();
                    mc.getRenderItem().zLevel = 0.0F;
                    GlStateManager.enableLighting();

                    // reverse the translate
                    GlStateManager.translate(-(x + 10), -(y - 5), 0);
                }
            }

            if (this.middleClick.getValue()) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        final BlockShulkerBox shulkerBox = (BlockShulkerBox) Block.getBlockFromItem(shulker.getItem());
                        if (shulkerBox != null) {
                            final NBTTagCompound tag = shulker.getTagCompound();
                            if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
                                final NBTTagCompound entityTag = tag.getCompoundTag("BlockEntityTag");

                                final TileEntityShulkerBox te = new TileEntityShulkerBox();
                                te.setWorld(mc.world);
                                te.readFromNBT(entityTag);
                                mc.displayGuiScreen(new GuiShulkerBox(mc.player.inventory, te));
                            }
                        }
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        }
    }
}
