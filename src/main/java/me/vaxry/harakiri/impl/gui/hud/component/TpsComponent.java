package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 7/25/2019 @ 7:44 AM.
 */
public final class TpsComponent extends DraggableHudComponent {

    public TpsComponent() {
        super("Tps");
        this.setH(mc.fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null && mc.getCurrentServerData() != null) {
            final String tps = String.format(ChatFormatting.WHITE + "TPS: %.2f", Harakiri.INSTANCE.getTickRateManager().getTickRate());
            this.setW(mc.fontRenderer.getStringWidth(tps));
            mc.fontRenderer.drawStringWithShadow(tps, this.getX(), this.getY(), -1);
        } else {
            this.setW(mc.fontRenderer.getStringWidth("(tps)"));
            mc.fontRenderer.drawStringWithShadow("(tps)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
