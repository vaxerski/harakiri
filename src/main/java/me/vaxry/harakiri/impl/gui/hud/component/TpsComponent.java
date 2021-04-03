package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;

public final class TpsComponent extends DraggableHudComponent {

    public TpsComponent() {
        super("Tps");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null) {
            final String tps = String.format("\2477TPS:\247f %.2f", Harakiri.get().getTickRateManager().getTickRate());
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(tps));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(tps, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(tps)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(tps)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
