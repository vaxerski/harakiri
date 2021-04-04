package me.vaxry.harakiri.impl.gui.hud;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.gui.HudComponent;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.gui.anchor.AnchorPoint;
import me.vaxry.harakiri.impl.gui.hud.component.PlexusComponent;
import me.vaxry.harakiri.impl.gui.hud.component.SwitchViewComponent;
import me.vaxry.harakiri.impl.gui.hud.component.special.ModuleListComponent;
import me.vaxry.harakiri.impl.gui.hud.component.special.ModuleSearchComponent;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;

public final class GuiHudEditor extends GuiScreen {

    float rainSpeed = 0.1f; // +0.1 default

    protected Texture bg = null;

    public int rainbowColor = 0x00000000;
    private float hue = 0;

    private Timer timer = new Timer();

    private ArrayList<HudComponent> hudComponentsSorted = new ArrayList<>();

    // Color picker handle
    public boolean isColorPickerOpen = false;
    public float colorPickerX = 0;
    public float colorPickerY = 0;
    public final float colorPickerXY = 70;
    public HudComponent colorPickerParent = null;
    public String colorPickerName = "";
    public boolean specialColorClick = false;
    public boolean forceCloseColorPicker = false;

    //FadeInOut Smart
    private Framebuffer GuiFramebuffer;
    private Timer fadeTimer = new Timer();
    private float FADE_SPEED = 100F;
    private boolean isClosing = false;
    private boolean isFading = false;
    private boolean wasClosed = true;
    public float curAlphaFade = 0;


    public GuiHudEditor(){
        hudComponentsSorted.addAll(Harakiri.get().getHudManager().getComponentList());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        final HudEditorModule mod = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);

        if (mod != null) {
            if (keyCode == Keyboard.getKeyIndex(mod.getKey())) {
                if (mod.isOpen()) {
                    mod.setOpen(false);
                } else {
                    Minecraft.getMinecraft().displayGuiScreen(null);
                }
            }
        }

        for (HudComponent component : Harakiri.get().getHudManager().getComponentList()) {
            if (component.isVisible()) {
                component.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);

        final ScaledResolution sr = new ScaledResolution(mcIn);
        for (AnchorPoint anchorPoint : Harakiri.get().getHudManager().getAnchorPoints()) {
            anchorPoint.updatePosition(sr);
        }
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = FADE_SPEED;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, desiredTimePerSecond);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        if(wasClosed){
            // First run.
            this.timer.reset();

            //this.GuiFramebuffer = new Framebuffer(res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor(), true);
            //this.GuiFramebuffer.createFramebuffer(res.getScaledWidth() * res.getScaleFactor(), res.getScaledHeight() * res.getScaleFactor());
            //GlStateManager.enableAlpha();
            //GlStateManager.enableBlend();
            //this.GuiFramebuffer.bindFramebuffer(false);

            this.curAlphaFade = 0;

            this.isFading = true;
        }

        if(!wasClosed && this.isFading){
            if(!this.isClosing){
                //this.curAlphaFade = Math.min(this.curAlphaFade + getJitter(), 100F);
                this.curAlphaFade = (float)MathUtil.parabolic(this.curAlphaFade, 100F, MathUtil.TIME_TO_INCLINE / getJitter());
                if(this.curAlphaFade >= 99.8F) {
                    this.isFading = false;
                    this.curAlphaFade = 100F;
                }
            }else{
                //this.curAlphaFade = Math.max(this.curAlphaFade - getJitter(), 0F);
                this.curAlphaFade = (float)MathUtil.parabolic(this.curAlphaFade, 0F, MathUtil.TIME_TO_INCLINE / getJitter());
                if(this.curAlphaFade == 0F) {
                    this.isFading = false;
                    this.curAlphaFade = 0F;
                }
            }

            /*final Minecraft mc = Minecraft.getMinecraft();

            if (OpenGlHelper.isFramebufferEnabled()) {
                //GlStateManager.color(1.0F, 1.0F, 1.0F, this.curAlphaFade / 100F);
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
                GlStateManager.glBlendEquation(GL_FUNC_ADD);

                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1F);

                this.GuiFramebuffer.bindFramebufferTexture();

                final float offX = (1 - this.curAlphaFade/100F) * (res.getScaledWidth() / 2F);
                final float offY = (1 - this.curAlphaFade/100F) * (res.getScaledHeight() / 2F);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos(offX, (double)res.getScaledHeight() - offY, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos((double)res.getScaledWidth() - offX, (double)res.getScaledHeight() - offY, 0.0D).tex((double)1, 0.0D).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos((double)res.getScaledWidth() - offX, offY, 0.0D).tex((double)1, (double)1).color(255, 255, 255, 255).endVertex();
                bufferbuilder.pos(offX, offY, 0.0D).tex(0.0D, (double)1).color(255, 255, 255, 255).endVertex();
                tessellator.draw();

                GlStateManager.disableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
            }
            GlStateManager.disableBlend();*/
        }

        if(!wasClosed) {
            // Init plexus
            if (Harakiri.get().getPlexusEffect() == null)
                Harakiri.get().initPlexusEffect((PlexusComponent) Harakiri.get().getHudManager().findComponent(PlexusComponent.class));

            rainbowColor = ColorUtil.changeAlpha(Harakiri.get().getHudManager().rainbowColor, (int)(this.curAlphaFade/100F * 255F));

            super.drawScreen(mouseX, mouseY, partialTicks);
            //this.drawDefaultBackground();
            RenderUtil.drawRect(0,0,res.getScaledWidth(), res.getScaledHeight(), ColorUtil.changeAlpha(0xFF050505, (int)((this.curAlphaFade/100F) * 0xAA)));

            // Draw the text

            if (this.bg == null)
                this.bg = new Texture("harabackground.png");

            GlStateManager.enableAlpha();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, this.curAlphaFade/100F);

            this.bg.bind();
            this.bg.render(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0, 0, 1, 1, this.curAlphaFade/100F);

            GlStateManager.disableTexture2D();
            //GlStateManager.disableBlend();
            //GlStateManager.disableAlpha();

            // Plexus
            Harakiri.get().getPlexusEffect().render(mouseX, mouseY);

            final float halfWidth = res.getScaledWidth() / 2.0f;
            final float halfHeight = res.getScaledHeight() / 2.0f;

            // Rainbow Border
            RenderUtil.drawLine(0, 0,0, res.getScaledHeight(), 2, rainbowColor); // Left
            RenderUtil.drawLine(0, 0, res.getScaledWidth(), 0, 2, rainbowColor); // Top
            RenderUtil.drawLine(0, res.getScaledHeight() - 1, res.getScaledWidth(), res.getScaledHeight() - 1, 1, rainbowColor); // Bottom
            RenderUtil.drawLine(res.getScaledWidth(), res.getScaledHeight(),res.getScaledWidth(), 0, 2, rainbowColor); // Right
        }

        if(wasClosed){
            //this.GuiFramebuffer.unbindFramebuffer();
            //Minecraft.getMinecraft().framebuffer.bindFramebuffer(false);
            wasClosed = false;
            return;
        }

        final float scale = this.curAlphaFade / 100F;
        final float offX = res.getScaledWidth() / 2F - (res.getScaledWidth() * scale) / 2F;
        final float offY = res.getScaledHeight() / 2F - (res.getScaledHeight() * scale) / 2F;
        if(!wasClosed && this.isFading) {
            GlStateManager.translate(offX, offY, 0F);
            GlStateManager.scale(scale, scale, scale);
        }

        if(Harakiri.get().getUsername().equalsIgnoreCase(""))
            Harakiri.get().getApiManager().killThisThing(); // Anti crack, some sort of

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.get().getHudManager().findComponent(SwitchViewComponent.class);

        for (int i = 0; i < this.hudComponentsSorted.size(); ++i) {

            this.isColorPickerOpen = false;

            // Render from bottom.
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible()) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, this.curAlphaFade/100F);
                component.render(mouseX, mouseY, partialTicks);

                if (component instanceof DraggableHudComponent) {
                    DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging()) {
                        int SIZE = 2;
                        if (draggable.getW() < 12 || draggable.getH() < 12)
                            SIZE = 1;
                        else if (draggable.getW() <= 0 || draggable.getH() <= 0)
                            SIZE = 0;

                        boolean colliding = false;

                        for (HudComponent other : Harakiri.get().getHudManager().getComponentList()) {
                            if (other instanceof DraggableHudComponent) {
                                DraggableHudComponent otherDraggable = (DraggableHudComponent) other;
                                if (other != draggable && draggable.collidesWith(otherDraggable) && otherDraggable.isVisible() && draggable.isSnappable() && otherDraggable.isSnappable() && !isShiftKeyDown()) {
                                    colliding = true;
                                    RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, 0x00000000, ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawBorderedRect(other.getX() - 1, other.getY() - 1, other.getX() + other.getW() + 1, other.getY() + other.getH() + 1, 1, 0x00000000, ColorUtil.changeAlpha(rainbowColor, 0x33));
                                    RenderUtil.drawRect(other.getX(), other.getY(), other.getX() + other.getW(), other.getY() + other.getH(), ColorUtil.changeAlpha(rainbowColor, 0x33));
                                }
                            }
                        }

                        // render white borders if snapable and is not colliding with any components
                        if (draggable.isSnappable() && !colliding) {
                            int snappableBackgroundColor = 0x00000000;
                            if (draggable.findClosest(mouseX, mouseY) != null) { // has an anchor point nearby
                                snappableBackgroundColor = 0x35FFFFFF;
                            }
                            RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getW() + draggable.getX(), draggable.getH() + draggable.getY(), 0x22DDDDDD);
                            //RenderUtil.drawBorderedRect(draggable.getX() - 1, draggable.getY() - 1, draggable.getX() + draggable.getW() + 1, draggable.getY() + draggable.getH() + 1, 1, snappableBackgroundColor, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + SIZE, draggable.getY() + SIZE, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + SIZE, 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX(), (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + SIZE, draggable.getY() + draggable.getH(), 0x90FFFFFF);
                            //RenderUtil.drawRect(draggable.getX() + draggable.getW() - SIZE, (draggable.getY() + draggable.getH()) - SIZE, draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x90FFFFFF);
                        }

                        // dragging highlight
                        //RenderUtil.drawRect(draggable.getX(), draggable.getY(), draggable.getX() + draggable.getW(), draggable.getY() + draggable.getH(), 0x35FFFFFF);
                    }
                }
            }
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, this.curAlphaFade/100F);
        swc.render(mouseX, mouseY, partialTicks);

        if(!wasClosed && this.isFading) {
            GlStateManager.translate(-offX, -offY, 0F);
            GlStateManager.scale(1F/scale, 1F/scale, 1F/scale);
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1F);
    }

    HudComponent componentMoving = null;

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.get().getHudManager().findComponent(SwitchViewComponent.class);

        if(this.isColorPickerOpen){
            if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                if(colorPickerParent != null){
                    specialColorClick = true;
                    colorPickerParent.mouseClickMove(mouseX, mouseY, clickedMouseButton);
                    specialColorClick = false;
                    return;
                }
            }
        }

        for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

            // From top!!
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                component.mouseClickMove(mouseX, mouseY, clickedMouseButton);
                // bring component to top
                componentMoving = component;
                this.bringComponentToTopOfScreen(component);
                break;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            SwitchViewComponent swc = (SwitchViewComponent)Harakiri.get().getHudManager().findComponent(SwitchViewComponent.class);
            swc.mouseClicked(mouseX, mouseY, mouseButton);
            Harakiri.get().getPlexusEffect().onMouseClicked();

            if(this.isColorPickerOpen){
                if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                    if(colorPickerParent != null){
                        specialColorClick = true;
                        colorPickerParent.mouseClick(mouseX,mouseY,mouseButton);
                        specialColorClick = false;
                        return;
                    }
                }
            }

            for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

                // From top!!
                HudComponent component = this.hudComponentsSorted.get(i);

                if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                    continue;

                if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                    continue;

                if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                    component.mouseClick(mouseX, mouseY, mouseButton);
                    componentMoving = component;
                    this.bringComponentToTopOfScreen(component);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        SwitchViewComponent swc = (SwitchViewComponent)Harakiri.get().getHudManager().findComponent(SwitchViewComponent.class);

        if(this.isColorPickerOpen){
            if(mouseX >= colorPickerX && mouseX <= colorPickerX + colorPickerXY && mouseY >= colorPickerY && mouseY <= colorPickerY + colorPickerXY){
                if(colorPickerParent != null){
                    specialColorClick = true;
                    colorPickerParent.mouseRelease(mouseX, mouseY, state);
                    specialColorClick = false;
                    return;
                }
            }
        }


        for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {

            // From top!!
            HudComponent component = this.hudComponentsSorted.get(i);

            if((component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && !swc.isModules)
                continue;

            if(!(component instanceof ModuleListComponent || component instanceof ModuleSearchComponent) && swc.isModules && component != swc)
                continue;

            if (component.isVisible() && component.isMouseInside(mouseX, mouseY)) {
                if(componentMoving != null && component != componentMoving)
                    componentMoving.mouseRelease(mouseX, mouseY, state);
                else
                    component.mouseRelease(mouseX, mouseY, state);
                break;
            }
        }

        for (int i = this.hudComponentsSorted.size() - 1; i >= 0; --i) {
            HudComponent component = this.hudComponentsSorted.get(i);
            if(component instanceof DraggableHudComponent){
                DraggableHudComponent component2 = (DraggableHudComponent)this.hudComponentsSorted.get(i);
                if(component2.isDragging())
                    component2.mouseRelease(mouseX, mouseY, state);
            }
        }


        try {
            swc.mouseReleased(mouseX, mouseY, state);

        }catch(Throwable t){
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        //Harakiri.get().getConfigManager().saveAll();

        final HudEditorModule hudEditorModule = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);
        if (hudEditorModule != null) {
            if (hudEditorModule.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.stopUseShader();
                }
            }
        }

        for (HudComponent component : Harakiri.get().getHudManager().getComponentList()) {
            if (component instanceof DraggableHudComponent) {
                if (component.isVisible()) {
                    final DraggableHudComponent draggable = (DraggableHudComponent) component;
                    if (draggable.isDragging()) {
                        draggable.setDragging(false);
                    }

                    component.onClosed();
                }
            }
        }

        wasClosed = true;

        // go back to previous screen
        super.onGuiClosed();
    }

    public void unload() {
        // empty
    }

    private void bringComponentToTopOfScreen(HudComponent component){
        this.hudComponentsSorted.remove(component);
        this.hudComponentsSorted.add(component);
        this.forceCloseColorPicker = true;
    }
}
