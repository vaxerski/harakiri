package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;

public final class PlayerCountComponent extends DraggableHudComponent {

    public PlayerCountComponent() {
        super("PlayerCount");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (mc.player != null) {
            final String playerCount = "Online: " + mc.player.connection.getPlayerInfoMap().size();

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(playerCount));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(playerCount, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(player count)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(player count)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
