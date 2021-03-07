package me.vaxry.harakiri.framework.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.texture.Texture;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.value.Value;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.renderer.GlStateManager;
import org.locationtech.jts.geom.Coordinate;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorComponent extends TextComponent {

    private Color currentColor;
    private HudComponent parent;

    private static final int BORDER = 1;
    private static final int TEXT_BLOCK_PADDING = 1;
    private static final int COLOR_SIZE = 7;

    private Texture HueCircleTex;

    // Hue circle
    private static final int XYDIM = 70;
    private static final int BORDEROFF = 4;

    private String customDisplayValue;

    private boolean dragging = false;

    private float hue;
    private float sat;
    private float val;

    //private final Texture gearTexture;
    //private final Texture gearTextureEnabled;

    public ColorComponent(String name, int defaultColor, HudComponent parent) {
        super(name, String.valueOf(defaultColor), false);
        this.currentColor = new Color(defaultColor);

        float[] hsv = new float[3];
        Color.RGBtoHSB(this.currentColor.getRed(), this.currentColor.getGreen(), this.currentColor.getBlue(), hsv);

        hue = hsv[0];
        sat = hsv[1];
        val = hsv[2];

        HueCircleTex = new Texture("huecircle.png");
        this.parent = parent;

        this.setH(9);
    }

    public ColorComponent(String name, int defaultColor, String customDisplayValue, HudComponent parent) {
        this(name, defaultColor, parent);
        this.customDisplayValue = customDisplayValue;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        //super.render(mouseX, mouseY, partialTicks);
        /*if (this.focused) {
            this.setH(50);
        } else {
            this.setH(9);
        }*/

        if(Harakiri.INSTANCE.getHudEditor().forceCloseColorPicker || !Harakiri.INSTANCE.getHudEditor().colorPickerName.equalsIgnoreCase(this.getName())){
            this.focused = false;
            Harakiri.INSTANCE.getHudEditor().forceCloseColorPicker = false;
            dragging = false;
        }

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x30909090);

        // draw bg rect
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW() - (this.focused ? 20 : 10), this.getY() + this.getH(), 0x45303030);

        // draw color rect
        RenderUtil.drawRect(this.getX() + this.getW() - BORDER - COLOR_SIZE, this.getY() + BORDER, this.getX() + this.getW() - BORDER, this.getY() + BORDER + COLOR_SIZE, ColorUtil.changeAlpha(this.currentColor.getRGB(), 0xFF));

        Harakiri.INSTANCE.getTTFFontUtil().drawString(this.getName(), (int) this.getX() + BORDER, (int) this.getY() + BORDER, this.focused ? 0xFFFFFFFF : 0xFFAAAAAA);

        if(this.focused){
            GuiHudEditor guiHudEditor = Harakiri.INSTANCE.getHudEditor();
            guiHudEditor.isColorPickerOpen = true;
            guiHudEditor.colorPickerParent = this.parent;
            guiHudEditor.colorPickerName = this.getName();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            // Draw the hue picker circle

            // Get the color as HSV
            float[] hsv = new float[3];
            hsv[0] = this.hue;
            hsv[1] = this.sat;
            hsv[2] = this.val;

            final float beginX = this.getX() + this.getW();
            final float beginY = this.getY() + this.getH() / 2.f - XYDIM / 2.f;
            guiHudEditor.colorPickerX = beginX;
            guiHudEditor.colorPickerY = beginY;

            RenderUtil.drawRoundedRect(beginX, beginY, XYDIM, XYDIM, 3, 0xAA050505);

            // Draw the hue circle
            GlStateManager.enableAlpha();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            this.HueCircleTex.bind();
            this.HueCircleTex.render(beginX + BORDEROFF,beginY + BORDEROFF, XYDIM - 2*BORDEROFF, XYDIM - 2*BORDEROFF);

            GlStateManager.disableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();

            final float HUE_CIRCLE_THICK = 5;

            // Draw the quad
            int RGBFullCol = Color.HSBtoRGB(hsv[0], 1, 1);
            float sizee = (XYDIM - 2*BORDEROFF - 2*HUE_CIRCLE_THICK) - 20;
           // RenderUtil.drawHueQuad(beginX + XYDIM/2.f - sizee / 2F, beginY + XYDIM/2.f - sizee / 2F, sizee, RGBFullCol);
            RenderUtil.drawHueQuad(beginX + XYDIM/2.f - sizee / 2F, beginY + XYDIM/2.f - sizee / 2F, beginX + XYDIM/2.f - sizee / 2F + sizee,beginY + XYDIM/2.f - sizee / 2F + sizee, RGBFullCol);

            // Calculate and draw the dot 2
            final float dotX = clamp(hsv[1], 0, 1) * sizee + beginX + XYDIM/2.f - sizee / 2F;
            final float dotY = (1 - clamp(hsv[2], 0, 1)) * sizee + beginY + XYDIM/2.f - sizee / 2F;

            // Draw the current
            Coordinate curSel = new Coordinate(beginX + XYDIM/2.f, beginY + BORDEROFF + HUE_CIRCLE_THICK / 2.f);
            Coordinate hueCircleMiddle = new Coordinate(beginX + XYDIM/2.f, beginY + XYDIM/2.f);

            curSel = rotate_point(hueCircleMiddle, curSel, this.hue * 2 * (float)Math.PI);

            final float CIRCLE_RADIUS = 2;
            RenderUtil.drawCircle((float)curSel.getX() - CIRCLE_RADIUS/4F, (float)curSel.getY() - CIRCLE_RADIUS/4F, (int)CIRCLE_RADIUS, 0xFFFFFFFF);

            RenderUtil.drawCircle(dotX, dotY, (int)CIRCLE_RADIUS, 0xFFFFFFFF);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        if(this.dragging)
            updateColorFromMouse(mouseX, mouseY);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if(Harakiri.INSTANCE.getHudEditor().specialColorClick && Harakiri.INSTANCE.getHudEditor().colorPickerName.equalsIgnoreCase(this.getName())){
            this.focus();
        }

        if (!this.focused) // must be focused
            return;

        this.updateColorFromMouse(mouseX, mouseY);

        this.dragging = false;

        this.returnListener.onComponentEvent();
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        super.mouseClick(mouseX, mouseY, button);
        if(this.isMouseInside(mouseX, mouseY) || (Harakiri.INSTANCE.getHudEditor().specialColorClick  && Harakiri.INSTANCE.getHudEditor().colorPickerName.equalsIgnoreCase(this.getName()))) {
            this.focus();
            this.dragging = true;
            Harakiri.INSTANCE.getHudEditor().colorPickerName = this.getName();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button) {
        super.mouseClickMove(mouseX, mouseY, button);

        if (!this.focused) // must be focused
            return;

        this.updateColorFromMouse(mouseX, mouseY);
    }

    public void updateColorFromMouse(int mouseX, int mouseY){
        final float beginX = this.getX() + this.getW();
        final float beginY = this.getY() + this.getH() / 2.f - XYDIM / 2.f;

        if(mouseX >= beginX && mouseX <= beginX + XYDIM && mouseY >= beginY && mouseY <= beginY + XYDIM) {
            // Check which thing to do
            final double d = Math.sqrt(Math.pow(mouseX - (beginX + XYDIM/2.f), 2) + Math.pow(mouseY - (beginY + XYDIM/2.f), 2));
            if (d > XYDIM/2.f - BORDEROFF - 5){
                // Outside the inner circle
                final float xoff = mouseX - (beginX + XYDIM/2.f);
                final float yoff = mouseY - (beginY + XYDIM/2.f);

                double angleRad;

                if(xoff > 0){
                    // First or fourth quarter
                    angleRad = Math.atan(xoff/yoff);
                    angleRad = -angleRad + Math.PI / 2.f;
                } else {
                    // Second or third quarter
                    angleRad = Math.atan(xoff/yoff);
                    angleRad = -angleRad + Math.PI * 1.5F;
                }

                if(xoff > 0 && yoff > 0){
                    angleRad -= Math.PI * 0.5F;
                }else if(xoff < 0 && yoff > 0){
                    angleRad += Math.PI * 0.5F;
                }else if(xoff < 0 && yoff < 0){
                    angleRad -= Math.PI * 0.5F;
                }else if(xoff > 0 && yoff < 0){
                    angleRad += Math.PI * 0.5F;
                }else{
                    if(yoff < 0){
                        angleRad = 0;
                    }else{
                        angleRad = -Math.PI;
                    }
                }

                angleRad += Math.PI;

                if(angleRad == Math.PI)
                    angleRad = 0;
                else if(angleRad == 0)
                    angleRad = Math.PI;

                if(angleRad > 2 * Math.PI)
                    angleRad -= 2 * Math.PI;

                final float angleDeg = (float)Math.toDegrees(angleRad);

                // Change the color
                float[] hsv = new float[3];
                hsv[0] = this.hue;
                hsv[1] = this.sat;
                hsv[2] = this.val;

                this.hue = angleDeg / 360F;

                int RGBCol = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
                this.currentColor = new Color(RGBCol);

                //Harakiri.INSTANCE.logChat("AngleRad: " + angleRad / Math.PI + "pi, angleDeg: " + angleDeg + " hsv[0]: " + hsv[0]);
            }else{
                // Inside
                final float HUE_CIRCLE_THICK = 5;
                float sizee = (XYDIM - 2*BORDEROFF - 2 * HUE_CIRCLE_THICK) - 20;
                if(!(mouseX >= beginX + XYDIM/2F - sizee/2F && mouseX <= beginX + XYDIM/2F + sizee/2F && mouseY >= beginY + XYDIM/2F - sizee/2F && mouseY <= beginY + XYDIM/2F + sizee/2F))
                    return;

                // Calc distances

                final float x1 = beginX + XYDIM/2F + sizee/2F - mouseX;
                final float y1 = beginY + XYDIM/2F + sizee/2F - mouseY;

                float sat = (sizee - x1) / sizee;
                float val = 1 - ((sizee - y1) / sizee);

                // Apply to HSV

                float[] hsv = new float[3];
                hsv[0] = this.hue;
                hsv[1] = this.sat;
                hsv[2] = this.val;

                this.sat = sat;
                this.val = val;

                int RGBCol = Color.HSBtoRGB(hsv[0], sat, val);
                this.currentColor = new Color(RGBCol);
            }
        }
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public String getCustomDisplayValue() {
        return customDisplayValue;
    }

    public void setCustomDisplayValue(String customDisplayValue) {
        this.customDisplayValue = customDisplayValue;
    }

    private Coordinate rotate_point(Coordinate around, Coordinate point, float theta) {
        double p1x = (Math.cos(theta) * (point.x - around.x) - Math.sin(theta) * (point.y - around.y) + around.x);
        double p2x = (Math.sin(theta) * (point.x - around.x) + Math.cos(theta) * (point.y - around.y) + around.y);
        return new Coordinate(p1x, p2x);
    }

    private float calc2DDistance(Coordinate A, Coordinate B){
        return (float)(Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2)));
    }

    private float clamp(float v, float min, float max){
        return Math.max(0, Math.min(v, max));
    }
}
