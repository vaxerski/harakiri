package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 7/25/2019 @ 7:44 AM.
 */
public final class TpsComponent extends DraggableHudComponent {

    public TpsComponent() {
        super("Tps");
        this.setH(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null) {
            final String tps = String.format("\2477TPS:\247f %.2f", Harakiri.INSTANCE.getTickRateManager().getTickRate());
            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(tps));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(tps, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("(tps)"));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("(tps)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
