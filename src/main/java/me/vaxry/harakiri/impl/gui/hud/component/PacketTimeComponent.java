package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.DecimalFormat;

public final class PacketTimeComponent extends DraggableHudComponent {

    private final Timer timer = new Timer();

    public PacketTimeComponent() {
        super("PacketTime");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);

        Harakiri.get().getEventManager().addEventListener(this);
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() != null) {
                this.timer.reset();
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
            final String delay = Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor ? "\247f[ Server Lag ]" : "\247fServer Lag: " + "\2474" + new DecimalFormat("#.#").format(seconds) + "\247cs";

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(delay));
            if(seconds >= 1.0f || Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor)
                Harakiri.get().getTTFFontUtil().drawStringWithShadow(delay, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(packet delay)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(packet delay)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }
}
