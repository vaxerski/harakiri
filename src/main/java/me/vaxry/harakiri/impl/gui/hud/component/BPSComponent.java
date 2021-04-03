package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;


public final class BPSComponent extends DraggableHudComponent {

    public BPSComponent() {
        super("BPS");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null) {
            final DecimalFormat df = new DecimalFormat("#.#");

            final double deltaX = mc.player.posX - mc.player.prevPosX;
            final double deltaZ = mc.player.posZ - mc.player.prevPosZ;
            final float tickRate = (mc.timer.tickLength / 1000.0f);

            final String bps = ChatFormatting.GRAY + "BPS: " + ChatFormatting.RESET + df.format((MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ) / tickRate));

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(bps));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(bps, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(BPS Speed)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(BPS Speed)", this.getX(), this.getY(), 0xFFCCCCCC);
        }
    }

}