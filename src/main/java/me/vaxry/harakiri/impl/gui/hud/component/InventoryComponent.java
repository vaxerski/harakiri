package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public final class InventoryComponent extends DraggableHudComponent {

    public InventoryComponent() {
        super("Inventory");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        this.setW(16 * 9);
        this.setH(16 * 3);

        if (mc.player == null) {
            if (mc.currentScreen instanceof GuiHudEditor) {
                Harakiri.get().getTTFFontUtil().drawStringWithShadow("(inventory)", this.getX(), this.getY(), 0xFFAAAAAA);
            }
            return;
        }

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010); // background
        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = mc.player.inventory.mainInventory.get(i + 9);
            int offsetX = (int) this.getX() + (i % 9) * 16;
            int offsetY = (int) this.getY() + (i / 9) * 16;
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
        }
        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.popMatrix();

        //top
        RenderUtil.drawLine(this.getX(), this.getY(), this.getX() + this.getW(), this.getY(), 0.7f, 0xAA000000);
        //Right
        RenderUtil.drawLine(this.getX() + this.getW(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0.7f, 0xAA000000);
        //Bott
        RenderUtil.drawLine(this.getX(), this.getY() + this.getH(), this.getX() + this.getW(), this.getY() + this.getH(), 0.7f, 0xAA000000);
        //Left
        RenderUtil.drawLine(this.getX(), this.getY(), this.getX(), this.getY() + this.getH(), 0.7f, 0xAA000000);
    }
}
