package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Objects;

public final class PingComponent extends DraggableHudComponent {

    public PingComponent() {
        super("Ping");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world == null || mc.player == null) {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(ping)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(ping)", this.getX(), this.getY(), 0xFFAAAAAA);
            return;
        }

        if (mc.player.connection == null)
            return;

        final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());
        if (Objects.nonNull(playerInfo)) {
            final String ms = playerInfo.getResponseTime() != 0 ? playerInfo.getResponseTime() + "ms" : "?";
            final String ping = "Ping: " + ms;

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(ping));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(ping, this.getX(), this.getY(), -1);
        }
    }
}