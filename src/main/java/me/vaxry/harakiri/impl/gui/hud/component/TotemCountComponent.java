package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class TotemCountComponent extends DraggableHudComponent {

    public TotemCountComponent() {
        super("TotemCount");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null) {
            final String totemCount = "Totems: " + this.getTotemCount();
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(totemCount));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(totemCount, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(totem count)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(totem count)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    private int getTotemCount() {
        int totems = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }
        return totems;
    }
}