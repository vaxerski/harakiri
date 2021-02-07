package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;

/**
 * Author Seth
 * 12/5/2019 @ 3:00 PM.
 */
public final class PlayerCountComponent extends DraggableHudComponent {

    public PlayerCountComponent() {
        super("PlayerCount");
        this.setH(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (mc.player != null) {
            final String playerCount = "Online: " + mc.player.connection.getPlayerInfoMap().size();

            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(playerCount));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(playerCount, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("(player count)"));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("(player count)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}
