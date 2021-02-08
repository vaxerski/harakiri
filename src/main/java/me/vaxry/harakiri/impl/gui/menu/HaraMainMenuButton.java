package me.vaxry.harakiri.impl.gui.menu;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class HaraMainMenuButton extends GuiButton {

    private float hoverPerc = 0;
    private Timer timer = new Timer();

    private float BAR_HEIGHT = 2;

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = 1;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    public HaraMainMenuButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, 200, 20, buttonText);
        timer.reset();
    }

    public HaraMainMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        timer.reset();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        //super.drawButton(mc, mouseX, mouseY, partialTicks);

        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        final float jitter = getJitter();

        if(hovered){
            hoverPerc = Math.min(hoverPerc + 2.5f * jitter, 1);
        }else {
            hoverPerc = Math.max(hoverPerc - 2.5f * jitter, 0);
        }

        RenderUtil.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x88FFFFFF + (int)(hoverPerc * 0x77) * 0x1000000);
        Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(this.displayString, this.x + this.width / 2.f -
                        Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(this.displayString)/2.f,
                this.y + this.height / 2.f - Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT / 2.f,
                0xFF00CCFF);
        RenderUtil.drawRect(this.x, this.y + this.height - this.BAR_HEIGHT, this.x + ((float)this.width * hoverPerc), this.height + this.y, 0xFF00CCFF);

        GlStateManager.disableBlend();
    }
}
