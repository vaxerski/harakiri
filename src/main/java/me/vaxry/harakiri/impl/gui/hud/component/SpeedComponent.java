package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;
import java.text.DecimalFormat;


/**
 * Author Seth
 * 7/27/2019 @ 7:46 PM.
 */
public final class SpeedComponent extends DraggableHudComponent {

    private Timer timer = new Timer();
    private Vector3f lastPos = new Vector3f(0,0,0);
    public float speed = 0;

    public SpeedComponent() {
        super("Speed");
        this.setH(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT);
    }

    private float getSpeed() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
        if(seconds < 0.1f)
            return speed;

        Vec3d pos = mc.player.getPositionVector();

        float deltaX = (float)Math.abs(lastPos.x - pos.x);
        float deltaZ = (float)Math.abs(lastPos.z - pos.z);

        float delta = (float)Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float mpers = delta / seconds;

        lastPos.x = (float)pos.x;
        lastPos.y = (float)pos.y;
        lastPos.z = (float)pos.z;

        timer.reset();

        speed = mpers * 3.6f;

        return speed; // km/h
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null) {
            final DecimalFormat df = new DecimalFormat("#.#");

            final String speed = ChatFormatting.GRAY + "Speed: " + ChatFormatting.RESET + df.format(getSpeed()) + ChatFormatting.GRAY + "km/h";

            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(speed));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(speed, this.getX(), this.getY(), -1);
        } else {
            final String speed = ChatFormatting.GRAY + "Speed: " + ChatFormatting.RESET + "0" + ChatFormatting.GRAY + "km/h";
            this.setW(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(speed));
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(speed, this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}