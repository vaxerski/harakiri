package me.vaxry.harakiri.framework.gui.hud.component;

import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author noil
 */
public class ToolTipComponent extends HudComponent {

    public String text;
    public int alpha;

    public ToolTipComponent(String text) {
        this.text = text;
        this.alpha = 0;

        final int tooltipWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        final int tooltipHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        this.setW(tooltipWidth);
        this.setH(tooltipHeight);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (this.alpha < 0xFF/*max alpha*/) {
            this.alpha += 8/* arbitrary value, the speed at which it fades in essentially */;
            if(this.alpha > 0xFF)
                this.alpha = 0xFF;
        }

        if (this.alpha > 0x8A/* another arbitrary value, the alpha hex value at which it begins to show on screen*/) {
            final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            this.setX(res.getScaledWidth() - this.getW() - 3);
            this.setY(res.getScaledHeight() - this.getH() - 3);

            // clamp tooltip to stay inside the screen bounds
            //this.clamp();

            // background
            //RenderUtil.drawRect(this.getX() - 2, this.getY() - 2, this.getX() + this.getW() + 2, this.getY() + this.getH() + 2, ColorUtil.changeAlpha(0x80202020, this.alpha / 4));
            RenderUtil.drawRoundedRect(this.getX() - 2, this.getY() - 2, this.getW() + 2, this.getH() + 2, 5, ColorUtil.changeAlpha(0x80202020, this.alpha / 4));
            RenderUtil.drawRoundedRect(this.getX() - 1, this.getY() - 1, this.getW() + 1, this.getH() + 1, 5, ColorUtil.changeAlpha(0xAD111111, this.alpha / 4));
            //RenderUtil.drawRect(this.getX() - 1, this.getY() - 1, this.getX() + this.getW() + 1, this.getY() + this.getH() + 1, ColorUtil.changeAlpha(0xAD111111, this.alpha / 4));
            // text
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(this.text, this.getX() + 1, this.getY() + 1, ColorUtil.changeAlpha(0xFFAAAAB7, this.alpha)); //0xFFFFFFFF
            GlStateManager.disableBlend();
        }
    }

    private void clamp() {
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        if (this.getX() <= 0) {
            this.setX(2);
        }

        if (this.getY() <= 0) {
            this.setY(2);
        }

        if (this.getX() + this.getW() >= sr.getScaledWidth() - 2) {
            this.setX(sr.getScaledWidth() - 2 - this.getW());
        }

        if (this.getY() + this.getH() >= sr.getScaledHeight() - 2) {
            this.setY(sr.getScaledHeight() - 2 - this.getH());
        }
    }
}
