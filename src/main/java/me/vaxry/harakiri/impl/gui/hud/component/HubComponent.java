package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.hud.EventHubComponentClick;
import me.vaxry.harakiri.framework.gui.hud.component.HudComponent;
import me.vaxry.harakiri.framework.gui.hud.component.ResizableHudComponent;
import me.vaxry.harakiri.framework.texture.Texture;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.gui.hud.component.module.ModuleListComponent;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * created by noil on 9/29/2019 at 12:23 PM
 */
public final class HubComponent extends ResizableHudComponent {

    private int scroll;

    private int totalHeight;

    private final int SCROLL_WIDTH = 5;
    private final int BORDER = 2;
    private final int TEXT_GAP = 1;
    private final int TEXTURE_SIZE = 8;
    private final int TITLE_BAR_HEIGHT = mc.fontRenderer.FONT_HEIGHT + 1;

    private int ACCENT_COLOR = 0xFFCCFF66;
    private int ACCENT_COLOR_BG = 0x44CCFF66;

    private boolean useRainbow = false;
    private int rainbowCol = 0xFFFFFFFF;
    private int rainbowColBG = 0x45FFFFFF;

    private final Texture texture;

    public HubComponent() {
        super("Components", 100, 120, 125, 1000);
        this.texture = new Texture("module-components.png");

        this.setVisible(true);
        this.setSnappable(false);
        this.setW(100);
        this.setH(200);
        this.setX((mc.displayWidth / 2.0f) - (this.getW() / 2));
        this.setY((mc.displayHeight / 2.0f) - (this.getH() / 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (!(mc.currentScreen instanceof GuiHudEditor))
            return;

        final ScaledResolution sr = new ScaledResolution(mc);

        // Render Y pos offset (make this all modular eventually...)
        int offsetY = 0;

        // Scrolling
        this.handleScrolling(mouseX, mouseY);

        // No dragging inside box
        final boolean insideTitlebar = mouseY <= this.getY() + TITLE_BAR_HEIGHT + BORDER;
        if (!insideTitlebar) {
            this.setDragging(false);
        }

        // Is mouse inside
        final boolean mouseInside = this.isMouseInside(mouseX, mouseY);

        // clamp max width & height
        if (this.isResizeDragging()) {
            if (this.getH() > this.getTotalHeight()) {
                this.setH(this.getTotalHeight());
                this.setResizeDragging(false);
            }
        }

        final HudEditorModule hem = (HudEditorModule) Harakiri.INSTANCE.getModuleManager().find(HudEditorModule.class);
        ACCENT_COLOR = 0xFF000000 + hem.color.getValue().getRGB();
        ACCENT_COLOR_BG = 0x44000000 + hem.color.getValue().getRGB();

        final HudModule hm = (HudModule) Harakiri.INSTANCE.getModuleManager().find(HudModule.class);
        if(hm.rainbow.getValue())
            useRainbow = true;
        else
            useRainbow = false;

        rainbowCol = Harakiri.INSTANCE.getHudEditor().rainbowColor;
        rainbowColBG = 0x45000000 + Harakiri.INSTANCE.getHudEditor().rainbowColor - 0xFF000000;

        // Background & title
        //RenderUtil.begin2D();
        //RenderUtil.drawRoundedRect(this.getX() - 1, this.getY() - 1, this.getW() + 1, this.getH() + 1, 5,0x11101010); //0x99
        RenderUtil.drawRoundedRect(this.getX(), this.getY(), this.getW(), this.getH() + BORDER, 5, 0x22202020); //0xFF

        // Draw top area
        RenderUtil.drawRoundedRectTop(this.getX(), this.getY(), this.getW(), mc.fontRenderer.FONT_HEIGHT + BORDER, 5, this.useRainbow ? ColorUtil.changeAlpha(rainbowCol, 0x77) : ColorUtil.changeAlpha(ACCENT_COLOR, 0x77));

        //GlStateManager.enableBlend();
        //texture.bind();
        //texture.render(this.getX() + BORDER, this.getY() + BORDER, TEXTURE_SIZE, TEXTURE_SIZE);
        //GlStateManager.disableBlend();
        mc.fontRenderer.drawStringWithShadow(this.getName(), this.getX() - mc.fontRenderer.getStringWidth(this.getName())/2.f + this.getW() / 2.f, this.getY() + BORDER, 0xFFDDDDDD);
        offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;

        // Behind hub
        //RenderUtil.drawRoundedRect(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getW() - SCROLL_WIDTH - BORDER, this.getH() - BORDER, 5, 0x22101010); //0xff

        // Scrollbar bg
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x22101010); //0xff
        // Scrollbar highlights
        if (mouseInside) {
            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + offsetY + 8 + BORDER, 0x22909090, 0x22909090); //0xff
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + this.getH() - 8 - BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x22909090, 0x22909090); //0xff
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + (this.getH() / 2) + BORDER + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x11909090, 0x11909090); //0x90
                } else {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + (this.getH() / 2) + BORDER + BORDER, 0x11909090, 0x11909090); //0x90
                }
            }
        }
        // Scrollbar
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, MathHelper.clamp((this.getY() + offsetY + BORDER) + ((this.getH() * this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), this.getX() + this.getW() - BORDER, MathHelper.clamp((this.getY() + this.getH() - BORDER) - (this.getH() * (this.totalHeight - this.getH() - this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), 0x22909090); //0xff

        // Begin scissoring and render the component "buttons"
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.glScissor(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER - SCROLL_WIDTH, this.getY() + this.getH() - BORDER, sr);
        for (HudComponent component : Harakiri.INSTANCE.getHudManager().getComponentList()) {
            if (component != this && !(component instanceof ModuleListComponent) && !(component instanceof SwitchViewComponent)) {
                if(useRainbow)
                    RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, component.isVisible() ? rainbowColBG : 0x451F1C22);
                else
                    RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, component.isVisible() ? 0x45002e00 : 0x451F1C22);
                final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + 1) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll);
                if (insideComponent) {
                    RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x30909090, 0x30909090); //0x00101010
                }

                // draw button text
                if(useRainbow)
                    mc.fontRenderer.drawStringWithShadow(component.getName(), this.getX() + BORDER + TEXT_GAP + 1, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, component.isVisible() ? rainbowCol : 0xFFDDDDDD);
                else
                    mc.fontRenderer.drawStringWithShadow(component.getName(), this.getX() + BORDER + TEXT_GAP + 1, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, component.isVisible() ? 0xFF55FF55 : 0xFFDDDDDD);

                offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // figures up a "total height (pixels)" of the inside of the list area (for calculating scroll height)
        this.totalHeight = BORDER + TEXT_GAP + offsetY + BORDER;
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        final boolean inside = this.isMouseInside(mouseX, mouseY);
        final boolean insideTitlebar = mouseY <= this.getY() + BORDER + TITLE_BAR_HEIGHT;

        if (inside && button == 0 && !insideTitlebar) {
            int offsetY = BORDER;

            for (HudComponent component : Harakiri.INSTANCE.getHudManager().getComponentList()) {
                if (component != this && !(component instanceof ModuleListComponent) && !(component instanceof SwitchViewComponent)) {
                    final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + 1 + offsetY - this.scroll) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT * 2) + 1 + offsetY - this.scroll);
                    if (insideComponent) {
                        component.setVisible(!component.isVisible());
                        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventHubComponentClick(component.getName(), component.isVisible()));
                    }
                    offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                }
            }

            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    scroll += 10;
                } else {
                    scroll -= 10;
                }
                this.clampScroll();
            }
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        final boolean insideDragZone = mouseY <= this.getY() + TITLE_BAR_HEIGHT + BORDER || mouseY >= ((this.getY() + this.getH()) - CLICK_ZONE);
        if (insideDragZone) {
            super.mouseClick(mouseX, mouseY, button);
        }
    }

    private void clampScroll() {
        if (this.scroll < 0) {
            this.scroll = 0;
        }
        if (this.scroll > this.totalHeight - this.getH()) {
            this.scroll = this.totalHeight - (int) this.getH();
        }
    }

    private void handleScrolling(int mouseX, int mouseY) {
        if (this.isMouseInside(mouseX, mouseY) && Mouse.hasWheel()) {
            this.scroll += -(Mouse.getDWheel() / 5);
            this.clampScroll();
        }
    }

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.scroll = scroll;
    }

    public int getTotalHeight() {
        return totalHeight;
    }
}
