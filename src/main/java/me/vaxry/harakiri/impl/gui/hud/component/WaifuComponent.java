package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.ResizableHudComponent;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public final class WaifuComponent extends ResizableHudComponent {

    protected Texture waifu;
    final int TEXW = 505;
    final int TEXH = 833;

    final float SCALE = 0.1f;

    public WaifuComponent() {
        super("Waifu", 505 * 0.05f, 833 * 0.05f, 505 * 0.7f, 833 * 0.7f);
        this.setW(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
        waifu = new Texture("waifu.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor))
            return;

        GlStateManager.enableAlpha();
        // Don't do this!! It's not needed, and it causes graphical glitches.
        //GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        //GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        this.waifu.bind();
        this.waifu.render(this.getX(), this.getY(), this.getW(), this.getH());

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }

}