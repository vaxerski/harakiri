package me.vaxry.harakiri.framework.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;

public class ButtonComponent extends HudComponent {

    public boolean enabled, rightClickEnabled;
    public ComponentListener mouseClickListener, rightClickListener;

    private int rainbowCol = 0xFFFFFFFF;
    private int rainbowColBG = 0xFFFFFFFF;

    private int ACCENT_COLOR = 0xFFCCFF66;
    private int ACCENT_COLOR_BG = 0x44CCFF66;

    private boolean useRainbow = false;

    public ButtonComponent(String name) {
        super(name);
        this.enabled = false;
        this.rightClickEnabled = false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final HudModule hm = (HudModule) Harakiri.get().getModuleManager().find(HudModule.class);
        if(hm.rainbow.getValue())
            useRainbow = true;
        else
            useRainbow = false;

        final HudEditorModule hem = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);
        ACCENT_COLOR = 0xFF000000 + hem.color.getValue().getRGB();
        ACCENT_COLOR_BG = 0x44000000 + hem.color.getValue().getRGB();

        rainbowCol = Harakiri.get().getHudEditor().rainbowColor;
        rainbowColBG = 0x45000000 + Harakiri.get().getHudEditor().rainbowColor - 0xFF000000;

        if (isMouseInside(mouseX, mouseY))
            RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x30909090);

        // draw bg
        if(this.enabled)
            RenderUtil.drawGradientRectLeftRight(this.getX(), this.getY(), this.getX() + (this.rightClickListener != null ? this.getW() - 8 : this.getW()), this.getY() + this.getH(), this.useRainbow ? this.rainbowColBG : ACCENT_COLOR_BG, 0x00001F00);
        else
            RenderUtil.drawGradientRectLeftRight(this.getX(), this.getY(), this.getX() + (this.rightClickListener != null ? this.getW() - 8 : this.getW()), this.getY() + this.getH(), 0x001F0000, 0x001F0000);

        if (this.rightClickListener != null) {
            final boolean isMousingHoveringDropdown = mouseX >= this.getX() + this.getW() - 8 && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();

            // draw bg behind triangles
            //RenderUtil.drawRect(this.getX() + this.getW() - 8, this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45202020);

            // draw right click box
            /*if (this.rightClickEnabled) {
                RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, 180, 0xFF6D55FF);
                if (isMousingHoveringDropdown)
                    RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, 180, 0x50FFFFFF);
            } else {
                RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, -90, 0x75909090);
                if (isMousingHoveringDropdown)
                    RenderUtil.drawTriangle(this.getX() + this.getW() - 4, this.getY() + 4, 3, -90, 0x50FFFFFF);
            }*/
        }

        // draw text
        Harakiri.get().getTTFFontUtil().drawString(this.getName(), (int) this.getX() + 1, (int) this.getY() + 1, this.enabled ? this.useRainbow ? rainbowCol : ACCENT_COLOR : 0xFFAAAAB7);
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        if (!this.isMouseInside(mouseX, mouseY))
            return;

        if (button == 0) {
            // handle clicking the right click button
            if (this.rightClickListener != null) {
                // is inside button
                if (mouseX >= this.getX() + this.getW() - 8 && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH()) {
                    this.rightClickListener.onComponentEvent();
                    return; // cancel normal action
                }
            }

            // enable / disable normally

            this.enabled = !this.enabled;

            if (this.mouseClickListener != null)
                this.mouseClickListener.onComponentEvent();

        } else if (button == 1) {
            if (this.rightClickListener != null)
                this.rightClickListener.onComponentEvent();
        }
    }
}
