package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.util.math.MathHelper;

public final class DirectionComponent extends DraggableHudComponent {

    private final Timer directionTimer = new Timer();
    private String direction = "";

    public DirectionComponent() {
        super("Direction");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.world != null) {
            if (this.directionTimer.passed(250)) { // 250ms
                direction = String.format("%s" + ChatFormatting.GRAY + " [" + ChatFormatting.GRAY + "%s]", this.getFacing(), this.getTowards());
                this.directionTimer.reset();
            }

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(direction));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(direction, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(direction)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(direction)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

    private String getFacing() {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
                return "South";
            case 1:
                return "South West";
            case 2:
                return "West";
            case 3:
                return "North West";
            case 4:
                return "North";
            case 5:
                return "North East";
            case 6:
                return "East";
            case 7:
                return "South East";
        }
        return "Error";
    }

    private String getTowards() {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
                return "+Z";
            case 1:
                return "-X +Z";
            case 2:
                return "-X";
            case 3:
                return "-X -Z";
            case 4:
                return "-Z";
            case 5:
                return "+X -Z";
            case 6:
                return "+X";
            case 7:
                return "+X +Z";
        }
        return "Invalid";
    }
}