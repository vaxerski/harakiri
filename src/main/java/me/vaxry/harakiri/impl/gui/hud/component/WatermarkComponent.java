package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.gui.hud.component.HudComponent;
import me.vaxry.harakiri.api.texture.Texture;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.module.ui.WatermarkModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Author Seth
 * 7/25/2019 @ 4:55 AM.
 */
public final class WatermarkComponent extends HudComponent {

    private final String WATERMARK = ChatFormatting.BOLD + "Harakiri | " + ChatFormatting.LIGHT_PURPLE + harakiriMod.VERSION;

    protected Texture watermarkTex;

    public WatermarkComponent() {
        super("Watermark");
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        this.setH(res.getScaledHeight());
        this.setW(res.getScaledWidth());
        watermarkTex = new Texture("harawatermark.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if((Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor))
            return;

        WatermarkModule watermarkModule = (WatermarkModule) Harakiri.INSTANCE.getModuleManager().find(WatermarkModule.class);
        watermarkModule.setWMOnState(this.isVisible());

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(WATERMARK, 1, watermarkModule.Xoff.getValue(), 0xFF9C00FF);

        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        this.watermarkTex.bind();
        this.watermarkTex.render(0, 0, res.getScaledWidth(), res.getScaledHeight());

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }
}
