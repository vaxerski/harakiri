package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.notification.Notification;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.gui.hud.anchor.AnchorPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/**
 * created by noil on 8/17/2019 at 4:39 PM
 */
public final class NotificationsComponent extends DraggableHudComponent {

    public NotificationsComponent(AnchorPoint anchorPoint) {
        super("Notifications");
        this.setAnchorPoint(anchorPoint); // by default anchors in the top center
        this.setVisible(true);
    }

    public NotificationsComponent() {
        super("Notifications");
        this.setVisible(true);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        WatermarkComponent watermarkComponent = (WatermarkComponent)Harakiri.INSTANCE.getHudManager().findComponent(WatermarkComponent.class);

        int offsetY = watermarkComponent.isVisible() ? Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4 : 1;
        float maxWidth = 0;

        final ScaledResolution sr = new ScaledResolution(mc);

        for (Notification notification : Harakiri.INSTANCE.getNotificationManager().getNotifications()) {

            float offsetX = 0;

            if (this.getAnchorPoint() != null) {
                switch (this.getAnchorPoint().getPoint()) {
                    case TOP_CENTER:
                    case BOTTOM_CENTER:
                        offsetX = (this.getW() - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(notification.getText())) / 2;
                        break;
                    case TOP_LEFT:
                    case BOTTOM_LEFT:
                        offsetX = 0;
                        break;
                    case TOP_RIGHT:
                    case BOTTOM_RIGHT:
                        offsetX = this.getW() - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(notification.getText());
                        break;
                }
            }

            notification.setX(this.getX() + offsetX);
            notification.setY(this.getY() + offsetY);
            notification.setWidth(Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(notification.getText()) + 1);
            notification.setHeight(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 5);

            float firstRectPerc = notification.percFracBack;

            RenderUtil.drawRect(notification.getX() - 1, notification.getY(), notification.getX() + notification.getWidth() * firstRectPerc, notification.getY() + notification.getHeight(), 0xFFCC00CC);
            RenderUtil.drawRect(notification.getX() - 1, notification.getY(), notification.getX() + notification.getWidth() * notification.percFrac, notification.getY() + notification.getHeight(), 0xFF000000);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.glScissor(notification.getX() - 1, notification.getY(), notification.getX() + notification.getWidth() * notification.percFrac, notification.getY() + notification.getHeight(), sr);
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(notification.getText(), notification.getX(), notification.getY() + 2.9F, 0xFFDDDDDD);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            //RenderUtil.drawRect(notification.getTransitionX() - 1, notification.getY(), notification.getTransitionX() + notification.getWidth() + 1, notification.getY() + notification.getHeight(), (int)Math.min(alpha * 0x10000000, 0x65000000) + 0x101010);
            //RenderUtil.drawRect(notification.getTransitionX() - 1, notification.getY(), notification.getTransitionX() + notification.getWidth() + 1, (notification.getY() + 1), notification.getType().getColor() - 0xFF000000 + (int)(alpha * 0x10000000));
            //Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(notification.getText(), notification.getTransitionX(), notification.getY() + 4.0F, (int)(alpha * 0x10000000) + 0xFFFFFF);

            final float width = notification.getWidth();
            if (width >= maxWidth) {
                maxWidth = width;
            }

            offsetY += notification.getHeight();
        }

        if (Harakiri.INSTANCE.getNotificationManager().getNotifications().isEmpty()) {
            if (mc.currentScreen instanceof GuiHudEditor) {
                final String placeholder = "(notifications display)";
                maxWidth = Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(placeholder);
                offsetY = Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT;
                Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(placeholder, this.getX(), this.getY(), 0xFFAAAAAA);
            } else {
                maxWidth = 0;
                offsetY = 0;
                this.setEmptyH(Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT);
            }
        }

        this.setW(maxWidth);
        this.setH(offsetY);
    }

}
