package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;


public final class ArmorComponent extends DraggableHudComponent {

    private static final int ITEM_SIZE = 18;

    public ArmorComponent() {
        super("Armor");
        this.setSnappable(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;
        int itemSpacingWidth = 0;
        boolean playerHasArmor = false;

        boolean fixedFirstEnchant = false;

        if (mc.player != null) {
            for (int i = 0; i <= 3; i++) {
                final ItemStack stack = mc.player.inventoryContainer.getSlot(8 - i).getStack();
                if (!stack.isEmpty()) {
                    GlStateManager.pushMatrix();
                    RenderHelper.disableStandardItemLighting(); // fix boots
                    RenderHelper.enableGUIStandardItemLighting();

                    if(stack.hasEffect() && !fixedFirstEnchant){
                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) -10000, (int) -10000);
                        fixedFirstEnchant = true;
                    }

                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) this.getX() + itemSpacingWidth, (int) this.getY());
                    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int) this.getX() + itemSpacingWidth, (int) this.getY());
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();
                    itemSpacingWidth += ITEM_SIZE;
                    playerHasArmor = true;
                }
            }
        }

        if (!playerHasArmor) {
            if (isInHudEditor) {
                Harakiri.get().getTTFFontUtil().drawString("(armor)", this.getX() + this.getW() / 2.f - Harakiri.get().getTTFFontUtil().getStringWidth("(armor)")/2.f, (int) this.getY() + this.getH() / 2.f - Harakiri.get().getTTFFontUtil().FONT_HEIGHT / 2.f, 0xFFAAAAAA);
                itemSpacingWidth = ITEM_SIZE * 4; // simulate 4 slots of armor (for a placeholder in hud editor)
            } else {
                //this.setW(0);
                //this.setH(0);
                //this.setEmptyH(16);
                return;
            }
        }

        this.setW(itemSpacingWidth);
        this.setH(16);
    }

}
