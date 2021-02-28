package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * created by noil on 9/1/2019 at 4:27 PM
 */
public final class TimeComponent extends DraggableHudComponent {

    public TimeComponent() {
        super("Time");
        this.setH(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String time = new SimpleDateFormat("h:mm a").format(new Date());

        this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(time));
        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(time, this.getX(), this.getY(), -1);
    }
}
