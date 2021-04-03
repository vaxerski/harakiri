package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class TimeComponent extends DraggableHudComponent {

    public TimeComponent() {
        super("Time");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String time = new SimpleDateFormat("h:mm a").format(new Date());

        this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(time));
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(time, this.getX(), this.getY(), -1);
    }
}
