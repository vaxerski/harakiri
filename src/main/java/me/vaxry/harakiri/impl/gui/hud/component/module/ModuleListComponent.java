package me.vaxry.harakiri.impl.gui.hud.component.module;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.gui.hud.component.*;
import me.vaxry.harakiri.api.gui.hud.component.*;
import me.vaxry.harakiri.api.gui.hud.component.TextComponent;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.texture.Texture;
import me.vaxry.harakiri.api.util.ColorUtil;
import me.vaxry.harakiri.api.util.RenderUtil;
import me.vaxry.harakiri.api.value.Value;
import me.vaxry.harakiri.impl.config.ModuleConfig;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * created by noil on 11/4/19 at 12:02 PM
 */
public final class ModuleListComponent extends ResizableHudComponent {

    private Module.ModuleType type;

    private int scroll = 0;
    private float realScroll = 0;
    private int oldScroll = 0;
    private int totalHeight;
    private int componentY = 0;
    private static int scomponentY = 0;

    private final float SCALING = 1f;
    private final int SCROLL_WIDTH = 5;
    private final int BORDER = 2;
    private final int TEXT_GAP = 2;
    private final int TEXTURE_SIZE = 8;
    private final int TITLE_BAR_HEIGHT = mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;

    private String originalName = "";
    private String title = "";

    private final HudEditorModule hudEditorModule;
    private final Texture texture;
    //private final Texture gearTexture;

    private ToolTipComponent currentToolTip;
    private ModuleSettingsComponent currentSettings;

    private int rainbowCol = 0xFFFFFFFF;
    private int rainbowColBG = 0x45FFFFFF;
    private boolean useRainbow = false;

    public ModuleListComponent(Module.ModuleType type) {
        super(StringUtils.capitalize(type.name().toLowerCase()), 110, 150, 160, 400);
        this.type = type;
        this.originalName = StringUtils.capitalize(type.name().toLowerCase());
        this.hudEditorModule = (HudEditorModule) Harakiri.INSTANCE.getModuleManager().find(HudEditorModule.class);
        this.texture = new Texture("module-" + type.name().toLowerCase() + ".png");
        //this.gearTexture = new Texture("gear_wheel_modulelist.png");

        this.setSnappable(false);
        this.setLocked(true);
        this.setVisible(true);

        this.setX(20);
        this.setY(20);

    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        //mouseX /= SCALING;
        //mouseY /= SCALING;

        if (!(mc.currentScreen instanceof GuiHudEditor))
            return;

        // rainbow
        rainbowCol = Harakiri.INSTANCE.getHudEditor().rainbowColor;
        rainbowColBG = 0x45000000 + Harakiri.INSTANCE.getHudEditor().rainbowColor - 0xFF000000;

        final HudModule hm = (HudModule) Harakiri.INSTANCE.getModuleManager().find(HudModule.class);
        if(hm.rainbow.getValue())
            useRainbow = true;
        else
            useRainbow = false;

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

        // Lerp scroll
        this.scroll += (this.realScroll - this.scroll) / 2.f;

        // clamp max width & height
        /*if (this.isResizeDragging()) {
            if (this.getH() > this.getTotalHeight()) {
                this.setH(this.getTotalHeight());
                this.setResizeDragging(false);
            }
        } else if (!this.isLocked() && this.getH() > this.getTotalHeight()) {
            this.setH(this.getTotalHeight());
        } else if (this.getH() > this.getTotalHeight() && this.getTotalHeight() > this.getInitialHeight()) {
            this.setH(this.getTotalHeight());
        }*/

        GlStateManager.scale(SCALING, SCALING, SCALING);

        // Background & title
        //RenderUtil.begin2D();
        //RenderUtil.drawRoundedRect(this.getX() - 1, this.getY() - 1, this.getW() + 1, this.getH() + 1, 5,0x11101010); //0x99
        RenderUtil.drawRoundedRect(this.getX(), this.getY(), this.getW(), this.getH(), 5, 0x22202020); //0xFF
        GlStateManager.enableBlend();
        texture.bind();
        texture.render(this.getX() + BORDER, this.getY() + BORDER, TEXTURE_SIZE, TEXTURE_SIZE);
        GlStateManager.disableBlend();
        mc.fontRenderer.drawStringWithShadow(this.title, this.getX() + BORDER + /* texture width */ TEXTURE_SIZE + BORDER, this.getY() + BORDER, 0xFFDDDDDD);
        offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;

        // Behind hub
        //RenderUtil.drawRoundedRect(this.getX() + BORDER, this.getY() + offsetY + BORDER, this.getW() - SCROLL_WIDTH - BORDER, this.getH() - BORDER, 5, 0x22101010); //0xFF

        // Scrollbar bg
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x22101010); //0xFF
        // Scrollbar highlights
        if (this.isMouseInside(mouseX, mouseY)) {
            if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + offsetY + 8 + BORDER, 0x22909090, 0x00101010); //0xFF 0x00
                RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + this.getH() - 8 - BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x00101010, 0x22909090); //0x00 0xFF
                float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                if (mouseY > diffY) {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + (this.getH() / 2) + BORDER + BORDER, this.getX() + this.getW() - BORDER, this.getY() + this.getH() - BORDER, 0x00101010, 0x10909090); //0x00 0x90
                } else {
                    RenderUtil.drawGradientRect(this.getX() + this.getW() - SCROLL_WIDTH, this.getY() + offsetY + BORDER, this.getX() + this.getW() - BORDER, this.getY() + (this.getH() / 2) + BORDER + BORDER, 0x10909090, 0x00101010); //0x90 0x00
                }
            }
        }
        // Scrollbar
        RenderUtil.drawRect(this.getX() + this.getW() - SCROLL_WIDTH, MathHelper.clamp((this.getY() + offsetY + BORDER) + ((this.getH() * this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), this.getX() + this.getW() - BORDER, MathHelper.clamp((this.getY() + this.getH() - BORDER) - (this.getH() * (this.totalHeight - this.getH() - this.scroll) / this.totalHeight), (this.getY() + offsetY + BORDER), (this.getY() + this.getH() - BORDER)), 0x22909090); //0xFF

        // Begin scissoring and render the module "buttons"
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.glScissor((this.getX() + BORDER) * SCALING, (this.getY() + offsetY + BORDER) * SCALING, (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) * SCALING, (this.getY() + this.getH() - BORDER) * SCALING, sr);

        this.title = this.originalName;
        for (Module module : Harakiri.INSTANCE.getModuleManager().getModuleList(this.type)) {

            // draw module button bg
            if(useRainbow)
                RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, module.isEnabled() ? rainbowColBG : 0x451F1C22);
            else
                RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, module.isEnabled() ? 0x453B005F : 0x451F1C22);

            final boolean insideModule = mouseX >= (this.getX() + BORDER) * SCALING && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH - 1) * SCALING && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + TEXT_GAP + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + TEXT_GAP) * SCALING && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll) * SCALING;
            if (insideModule) { // draw options line
                final boolean isHoveringOptions = mouseX >= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH - 12) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH - 2) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + TEXT_GAP + offsetY - this.scroll - mc.fontRenderer.FONT_HEIGHT + TEXT_GAP) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT) + 1 + offsetY - this.scroll);

                // draw bg behind gear
                //RenderUtil.drawRect(this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 12, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x45202020);
                // dont draw gear Xd
                //this.gearTexture.bind();
                //this.gearTexture.render(this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 11, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll + 0.5f, 8, 8);
                //if (isHoveringOptions) { // draw options line hover gradient
                //    RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 12, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x50909090, 0x50909090); //0x50909090 0x00101010
                //}

                // draw hover gradient
                RenderUtil.drawGradientRect(this.getX() + BORDER + TEXT_GAP, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, this.getX() + BORDER + TEXT_GAP + this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2, this.getY() + offsetY + BORDER + TEXT_GAP + mc.fontRenderer.FONT_HEIGHT - this.scroll, 0x30909090, 0x30909090); //0x30909090 0x00101010

                if(module.xOffset < 4.f){
                    module.xOffset += 1.f;
                }else{
                    module.xOffset = 4.f;
                }
            } else {
                if(module.xOffset > 0.f)
                    module.xOffset -= 0.7f;
                else
                    module.xOffset = 0.f;
            }

            // draw module name

            if(useRainbow)
                mc.fontRenderer.drawStringWithShadow(module.getDisplayName(), this.getX() + BORDER + TEXT_GAP + 1 + module.xOffset, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, module.isEnabled() ? rainbowCol : 0xFFAAAAB7);
            else
                mc.fontRenderer.drawStringWithShadow(module.getDisplayName(), this.getX() + BORDER + TEXT_GAP + 1 + module.xOffset, this.getY() + offsetY + BORDER + TEXT_GAP - this.scroll, module.isEnabled() ? 0xFFC255FF : 0xFFAAAAB7);

            offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;

            if(this.currentSettings != null){
                if(this.currentSettings.module.getDisplayName().equalsIgnoreCase(module.getDisplayName())){
                    // DRAW YEET
                    float tempOffsetY = 0;

                    this.currentSettings.setX(this.getX() + BORDER);
                    this.currentSettings.setY(this.getY() + offsetY + BORDER - this.scroll);
                    this.currentSettings.setW(this.getW() - BORDER - SCROLL_WIDTH - BORDER - 2);
                    this.currentSettings.setH(this.getH() - BORDER);
                    this.currentSettings.render((int)(mouseX * SCALING), (int)(mouseY * SCALING), partialTicks);
                    for (HudComponent settingComponent : this.currentSettings.components) {
                        //if (settingComponent.getY() > this.getY() + this.currentSettings.getH())
                        tempOffsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                    }

                    // Fix height
                    this.currentSettings.setH(tempOffsetY);
                    this.currentSettings.effectiveH = tempOffsetY * this.currentSettings.percOpen;

                    // Slow opening.
                    offsetY += tempOffsetY * this.currentSettings.percOpen;

                    int LINE_HAS_ERRORS = 2;

                    // Draw outline to see where the stuff is easier
                    RenderUtil.drawLine(this.currentSettings.getX() + LINE_HAS_ERRORS, this.currentSettings.getY(), this.currentSettings.getX() + this.currentSettings.getW() + LINE_HAS_ERRORS, this.currentSettings.getY(), 0.5f, ColorUtil.changeAlpha(0xFFFFFFFF, this.currentSettings.alphaForBorder)); //top
                    RenderUtil.drawLine(this.currentSettings.getX() + this.currentSettings.getW() + LINE_HAS_ERRORS, this.currentSettings.getY(), this.currentSettings.getX() + this.currentSettings.getW() + LINE_HAS_ERRORS, this.currentSettings.getY() + this.currentSettings.getH(), 0.5f, ColorUtil.changeAlpha(0xFFFFFFFF, this.currentSettings.alphaForBorder)); //right
                    RenderUtil.drawLine(this.currentSettings.getX() + LINE_HAS_ERRORS, this.currentSettings.getY() + this.currentSettings.getH(), this.currentSettings.getX() + this.currentSettings.getW() + LINE_HAS_ERRORS, this.currentSettings.getY() + this.currentSettings.getH(), 0.5f, ColorUtil.changeAlpha(0xFFFFFFFF, this.currentSettings.alphaForBorder)); //bottom
                    RenderUtil.drawLine(this.currentSettings.getX() + LINE_HAS_ERRORS, this.currentSettings.getY(), this.currentSettings.getX() + LINE_HAS_ERRORS, this.currentSettings.getY() + this.currentSettings.getH(), 0.5f, ColorUtil.changeAlpha(0xFFFFFFFF, this.currentSettings.alphaForBorder)); //left
                }
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Handle tooltips
        if (this.hudEditorModule != null && this.hudEditorModule.tooltips.getValue() && !insideTitlebar) {
            if (this.isMouseInside(mouseX, mouseY)) {
                String tooltipText = "";
                int height = BORDER;

                if (this.currentSettings != null) {
                    for (HudComponent valueComponent : this.currentSettings.components) {
                        if (valueComponent.isMouseInside(mouseX, mouseY)) {
                            tooltipText = valueComponent.getTooltipText();
                        } else {
                            if (this.currentToolTip != null) {
                                if (this.currentToolTip.text.equals(valueComponent.getTooltipText())) {
                                    this.currentToolTip = null;
                                }
                            }
                        }
                        height += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                    }
                } else {
                    for (Module module : Harakiri.INSTANCE.getModuleManager().getModuleList(this.type)) {
                        final boolean insideComponent = mouseX >= (this.getX() + BORDER) && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH) && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + TEXT_GAP + height - this.scroll) && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT * 2) + 1 + height - this.scroll);
                        if (insideComponent) {
                            tooltipText = module.getDesc();
                        } else {
                            if (this.currentToolTip != null) {
                                if (this.currentToolTip.text.equals(module.getDesc())) {
                                    this.currentToolTip = null;
                                }
                            }
                        }
                        height += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                    }
                }

                if (!tooltipText.equals("")) {
                    if (this.currentToolTip == null) {
                        this.currentToolTip = new ToolTipComponent(tooltipText);
                    } else {
                        this.currentToolTip.render(mouseX, mouseY, partialTicks);
                    }
                } else {
                    this.removeTooltip();
                }
            } else {
                this.removeTooltip();
            }
        }
        //RenderUtil.end2D();

        GlStateManager.scale(1.f/SCALING, 1.f/SCALING, 1.f/SCALING);

        // figures up a "total height (pixels)" of the inside of the list area (for calculating scroll height)
        this.totalHeight = BORDER + TEXT_GAP + offsetY + BORDER;
    }

    @Override
    public void mouseRelease(int mouseX, int mouseY, int button) {
        super.mouseRelease(mouseX, mouseY, button);

        //mouseX /= SCALING;
        //mouseY /= SCALING;

        final boolean inside = this.isMouseInside(mouseX, mouseY);
        final int titleBarHeight = mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
        final boolean insideTitlebar = mouseY <= this.getY() + BORDER + titleBarHeight;

        if(this.currentSettings != null) {
            this.currentSettings.mouseRelease(mouseX, mouseY, button);
        }

        if (inside && !insideTitlebar && !isResizeDragging()) {
            int offsetY = BORDER;
            for (Module module : Harakiri.INSTANCE.getModuleManager().getModuleList(this.type)) {
                final boolean insideComponent = mouseX >= (this.getX() + BORDER) * SCALING && mouseX <= (this.getX() + this.getW() - BORDER - SCROLL_WIDTH - 1) * SCALING && mouseY >= (this.getY() + BORDER + mc.fontRenderer.FONT_HEIGHT + TEXT_GAP + offsetY - this.scroll) * SCALING && mouseY <= (this.getY() + BORDER + (mc.fontRenderer.FONT_HEIGHT * 2) + 1 + offsetY - this.scroll) * SCALING;
                if (insideComponent) {
                    switch (button) {
                        case 0:
                            module.toggle();
                            this.setDragging(false);
                            break;
                        case 1:
                            if(this.currentSettings != null){
                                if(this.currentSettings.module.getDisplayName().equalsIgnoreCase(module.getDisplayName())){
                                    this.currentSettings = null;
                                }else{
                                    this.currentSettings = null;
                                    this.currentSettings = new ModuleSettingsComponent(module, this, offsetY);
                                }
                            }else{
                                this.currentSettings = new ModuleSettingsComponent(module, this, offsetY);
                            }
                            this.removeTooltip();
                            break;
                    }
                }
                offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                if(this.currentSettings != null){
                    if(this.currentSettings.module.getDisplayName().equalsIgnoreCase(module.getDisplayName())){
                        componentY = offsetY;
                        scomponentY = offsetY;
                        for (HudComponent settingComponent : this.currentSettings.components) {
                            offsetY += mc.fontRenderer.FONT_HEIGHT + TEXT_GAP;
                        }
                    }
                }
            }

            if (button == 0) {
                if (mouseX >= (this.getX() + this.getW() - SCROLL_WIDTH) && mouseX <= (this.getX() + this.getW() - BORDER)) { // mouse is inside scroll area on x-axis
                    float diffY = this.getY() + TITLE_BAR_HEIGHT + ((this.getH() - TITLE_BAR_HEIGHT) / 2);
                    if (mouseY > diffY) {
                        realScroll += 10;
                    } else {
                        realScroll -= 10;
                    }
                } else {
                    //Harakiri.INSTANCE.getConfigManager().saveAll();
                }
            }
        }
    }

    @Override
    public void mouseClick(int mouseX, int mouseY, int button) {
        final boolean insideDragZone = mouseY <= (this.getY() + TITLE_BAR_HEIGHT + BORDER) * SCALING || mouseY >= ((this.getY() + this.getH()) - CLICK_ZONE) * SCALING;
        //mouseX /= SCALING;
        //mouseY /= SCALING;
        if (insideDragZone) {
            super.mouseClick(mouseX, mouseY, button);
        } else {
            if (this.currentSettings != null) {
                this.currentSettings.mouseClick(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        if (this.currentSettings != null) {
            this.currentSettings.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (this.currentToolTip != null) {
            this.currentToolTip = null;
        }
    }

    private void handleScrolling(int mouseX, int mouseY) {
        //mouseX *= SCALING;
        //mouseY *= SCALING;
        if (this.isMouseInside(mouseX, mouseY) && Mouse.hasWheel()) {
            this.realScroll += -(Mouse.getDWheel() / 4.f);

            if (this.realScroll < 0) {
                this.realScroll = 0;
            }

            if (this.realScroll > this.totalHeight - this.getH()) {
                this.realScroll = this.totalHeight - (int) this.getH();
            }
            if(this.totalHeight - this.getH() < 0){
                this.realScroll = 0;
            }

            if (this.getOldScroll() != 0) {
                if (this.currentSettings == null) {
                    this.setScroll(this.getOldScroll());
                    this.setOldScroll(0);
                }
            }
        }
    }

    public void removeTooltip() {
        if (this.currentToolTip != null)
            this.currentToolTip = null;
    }

    public Module.ModuleType getType() {
        return type;
    }

    public int getScroll() {
        return scroll;
    }

    public void setScroll(int scroll) {
        this.realScroll = scroll;
    }

    public int getOldScroll() {
        return oldScroll;
    }

    public void setOldScroll(int oldScroll) {
        this.oldScroll = oldScroll;
    }

    public int getTotalHeight() {
        return totalHeight;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getTitle() {
        return title;
    }

    public Texture getTexture() {
        return texture;
    }

    public ToolTipComponent getCurrentToolTip() {
        return currentToolTip;
    }

    public ModuleSettingsComponent getCurrentSettings() {
        return currentSettings;
    }

    public static class BackButtonComponent extends HudComponent {
        private final ModuleListComponent parentModuleList;

        public BackButtonComponent(ModuleListComponent parentModuleList) {
            super("Back", "Go back.");
            this.parentModuleList = parentModuleList;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);

            if (isMouseInside(mouseX, mouseY))
                RenderUtil.drawGradientRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x30909090, 0x30909090); //0x00101010

            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x45303030);
            Minecraft.getMinecraft().fontRenderer.drawString(this.getName(), (int) this.getX() + 1, (int) this.getY() + 1, -1);
        }

        @Override
        public void mouseRelease(int mouseX, int mouseY, int button) {
            super.mouseRelease(mouseX, mouseY, button);

            if (!this.isMouseInside(mouseX, mouseY) || button != 0)
                return;

            for (HudComponent component : Harakiri.INSTANCE.getHudManager().getComponentList()) {
                if (component instanceof ModuleListComponent) {
                    ModuleListComponent moduleList = (ModuleListComponent) component;
                    if (moduleList.getName().equals(parentModuleList.getName())) {
                        moduleList.currentSettings = null;
                        moduleList.removeTooltip();
                    }
                }
            }
        }
    }

    public static class ModuleSettingsComponent extends HudComponent {
        public final Module module;
        public final List<HudComponent> components;
        private final ModuleListComponent parentModuleList;
        public int alphaForBorder = 255;
        private boolean isAlphaDown = true;
        public float percOpen = 0;
        public float effectiveH = 0;

        public ModuleSettingsComponent(Module module, ModuleListComponent parentModuleList, int yoff) {
            super(module.getDisplayName());

            this.setY(this.getY() + yoff);

            this.module = module;
            this.components = new ArrayList<>();
            this.parentModuleList = parentModuleList;

            //components.add(new ButtonComponent(this.getName()));
            //components.add(new BackButtonComponent(parentModuleList));

            TextComponent keybindText = new TextComponent("Bind", module.getKey().toLowerCase(), false);
            keybindText.setTooltipText("An assigned key.");
            keybindText.textListener = new TextComponent.TextComponentListener() {
                @Override
                public void onKeyTyped(int keyCode) {
                    if (keyCode == Keyboard.KEY_ESCAPE) {
                        module.setKey("NONE");
                        keybindText.displayValue = "none";
                        keybindText.focused = false;
                        // re-open the hud editor
                        final HudEditorModule hudEditorModule = (HudEditorModule) Harakiri.INSTANCE.getModuleManager().find(HudEditorModule.class);
                        if (hudEditorModule != null) {
                            hudEditorModule.displayHudEditor();
                        }
                    } else {
                        String newKey = Keyboard.getKeyName(keyCode);
                        module.setKey(newKey);
                        keybindText.displayValue = newKey.length() == 1 /* is letter */ ? newKey.substring(1) : newKey.toLowerCase();
                        keybindText.focused = false;
                    }
                }
            };
            components.add(keybindText);

            /*ButtonComponent enabledButton = new ButtonComponent("Enabled");
            enabledButton.setTooltipText("Enables this module.");
            enabledButton.enabled = module.isEnabled();
            enabledButton.mouseClickListener = new ComponentListener() {
                @Override
                public void onComponentEvent() {
                    module.toggle();
                }
            };
            components.add(enabledButton);*/

            ButtonComponent hiddenButton = new ButtonComponent("Hidden");
            hiddenButton.setTooltipText("Hides this module from the enabled mods list.");
            hiddenButton.enabled = module.isHidden();
            hiddenButton.mouseClickListener = new ComponentListener() {
                @Override
                public void onComponentEvent() {
                    module.setHidden(hiddenButton.enabled);
                }
            };
            components.add(hiddenButton);

            /*ColorComponent colorComponent = new ColorComponent("List Color", module.getColor());
            colorComponent.setTooltipText("The color for this module in the enabled mods list.");
            colorComponent.returnListener = new ComponentListener() {
                @Override
                public void onComponentEvent() {
                    module.setColor(colorComponent.getCurrentColor().getRGB());
                    Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);
                }
            };
            components.add(colorComponent);*/

            for (Value value : module.getValueList()) {
                if (value.getValue() instanceof Boolean) {
                    ButtonComponent valueButton = new ButtonComponent(value.getName());
                    valueButton.setTooltipText(value.getDesc());
                    valueButton.enabled = (Boolean) value.getValue();
                    valueButton.mouseClickListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            value.setValue(valueButton.enabled);
                        }
                    };
                    components.add(valueButton);
                } else if (value.getValue() instanceof Number) {
                    /*TextComponent valueNumberText = new TextComponent(value.getName(), value.getValue().toString(), true);
                    valueNumberText.setTooltipText(value.getDesc() + " " + ChatFormatting.GRAY + "(" + value.getMin() + " - " + value.getMax() + ")");
                    valueNumberText.returnListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            try {
                                if (value.getValue() instanceof Integer) {
                                    value.setValue(Integer.parseInt(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Double) {
                                    value.setValue(Double.parseDouble(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Float) {
                                    value.setValue(Float.parseFloat(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Long) {
                                    value.setValue(Long.parseLong(valueNumberText.displayValue));
                                } else if (value.getValue() instanceof Byte) {
                                    value.setValue(Byte.parseByte(valueNumberText.displayValue));
                                harakiri
                                Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class); // save module configs
                            } caharakirimberFormatException e) {
                                Harakiri.INSTANCE.logfChat("%s - %s: Invalid number format.", module.getDisplayName(), value.getName());
                            }
                        }
                    };
                    components.add(valueNumberText);
                    this.addComponentToButtons(valueNumberText);*/
                    //TODO: after v3.1
                    SliderComponent sliderComponent = new SliderComponent(value.getName(), value);
                    sliderComponent.setTooltipText(value.getDesc() + " " + ChatFormatting.GRAY + "(" + value.getMin() + " - " + value.getMax() + ")");
                    components.add(sliderComponent);
                    this.addComponentToButtons(sliderComponent);
                } else if (value.getValue() instanceof Enum) {
                    final Enum val = (Enum) value.getValue();
                    final int size = val.getClass().getEnumConstants().length;
                    final StringBuilder options = new StringBuilder();

                    for (int i = 0; i < size; i++) {
                        final Enum option = val.getClass().getEnumConstants()[i];
                        options.append(option.name().toLowerCase()).append((i == size - 1) ? "" : ", ");
                    }

                    /*TextComponent valueText = new TextComponent(value.getName(), value.getValue().toString().toLowerCase(), false);
                    valueText.setTooltipText(value.getDesc() + " " + ChatFormatting.GRAY + "(" + options.toString() + ")");
                    valueText.returnListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            if (value.getEnum(valueText.displayValue) != -1) {
                                harakirietEnumValue(valueText.displayValue);
                                Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class); // save configs
                            } elharakiri
                                Harakiri.INSTANCE.logfChat("%s - %s: Invalid entry.", module.getDisplayName(), value.getName());
                            }
                        }
                    };
                    components.add(valueText);
                    this.addComponentToButtons(valueText);*/

                    CarouselComponent carouselComponent = new CarouselComponent(value.getName(), value);
                    carouselComponent.setTooltipText(value.getDesc() + " " + ChatFormatting.GRAY + "(" + options.toString() + ")");
                    components.add(carouselComponent);
                    this.addComponentToButtons(carouselComponent);
                } else if (value.getValue() instanceof String) {
                    TextComponent valueText = new TextComponent(value.getName(), value.getValue().toString().toLowerCase(), false);
                    valueText.setTooltipText(value.getDesc());
                    valueText.returnListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            if (valueText.displayValue.length() > 0) {
                                value.setValue(valueText.displayValue);
                                Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class); // save configs
                            } else {
                                Harakiri.INSTANCE.logfChat("%s - %s: Not enough input.", module.getDisplayName(), value.getName());
                            }
                        }
                    };
                    components.add(valueText);
                    this.addComponentToButtons(valueText);
                } else if (value.getValue() instanceof Color) {
                    ColorComponent valueColor = new ColorComponent(value.getName(), ((Color) value.getValue()).getRGB());
                    valueColor.setTooltipText("Edit the color of: " + value.getName());
                    valueColor.returnListener = new ComponentListener() {
                        @Override
                        public void onComponentEvent() {
                            value.setValue(valueColor.getCurrentColor());
                            Harakiri.INSTANCE.getConfigManager().save(ModuleConfig.class);
                        }
                    };
                    components.add(valueColor);
                    this.addComponentToButtons(valueColor);
                }
            }
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks) {
            super.render(mouseX, mouseY, partialTicks);

            // Pulse alpha
            if(isAlphaDown){
                if(alphaForBorder > 1){
                    alphaForBorder -= 2;
                }else{
                    alphaForBorder = 0;
                    isAlphaDown = false;
                }
            }else{
                if(alphaForBorder < 254){
                    alphaForBorder += 2;
                }else{
                    alphaForBorder = 255;
                    isAlphaDown = true;
                }
            }

            // Increase perc
            percOpen += 0.05f;
            if(percOpen > 1.f)
                percOpen = 1.f;

            int offsetY = 1;
            for (HudComponent component : this.components) {
                int offsetX = 4;

                for (HudComponent otherComponent : this.components) {
                    if (otherComponent == component || otherComponent.getName().equals(component.getName()))
                        continue;

                    if (otherComponent instanceof ButtonComponent) {
                        boolean isChildComponent = component.getName().toLowerCase().startsWith(otherComponent.getName().toLowerCase());
                        if (isChildComponent) {
                            offsetX += 5;
                        }
                    }
                }

                component.setY(this.getY() + offsetY);
                component.setX(this.getX() + 1 + offsetX);

                component.setW(this.getW() - offsetX);
                component.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
                component.render(mouseX, mouseY, partialTicks);

                offsetY += component.getH() + 1;

                if(offsetY > effectiveH) {
                    break;
                }
            }
        }

        @Override
        public void mouseClick(int mouseX, int mouseY, int button) {
            super.mouseClick(mouseX, mouseY, button);
            for (HudComponent component : this.components) {
                component.mouseClick(mouseX, mouseY, button);
            }
        }

        @Override
        public void mouseClickMove(int mouseX, int mouseY, int button) {
            super.mouseClickMove(mouseX, mouseY, button);
            for (HudComponent component : this.components) {
                component.mouseClickMove(mouseX, mouseY, button);
            }
        }

        @Override
        public void mouseRelease(int mouseX, int mouseY, int button) {
            super.mouseRelease(mouseX, mouseY, button);
            for (HudComponent component : this.components) {
                component.mouseRelease(mouseX, mouseY, button);
            }
        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {
            super.keyTyped(typedChar, keyCode);
            for (HudComponent component : this.components) {
                component.keyTyped(typedChar, keyCode);
            }
        }

        private void addComponentToButtons(HudComponent hudComponent) {
            for (HudComponent component : this.components) {
                if (component instanceof ButtonComponent) {
                    boolean similarName = hudComponent.getName().toLowerCase().startsWith(component.getName().toLowerCase());
                    if (similarName) {
                        if (((ButtonComponent) component).rightClickListener == null) {
                            ((ButtonComponent) component).rightClickListener = new ComponentListener() {
                                @Override
                                public void onComponentEvent() {
                                    ((ButtonComponent) component).rightClickEnabled = !((ButtonComponent) component).rightClickEnabled;
                                }
                            };
                        }
                    }
                }
            }
        }
    }
}
