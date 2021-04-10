package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class HealthOverlayModule extends Module {

    public final Value<Integer> maxa = new Value<Integer>("MaxA", new String[]{"maxa", "ma"}, "Maximum Alpha", 150, 0, 255, 1);

    private Texture overlay;

    public HealthOverlayModule() {
        super("HealthOverlay", new String[]{"HealthOverlay"}, "Draws a red overlay when your health is low.", "NONE", -1, ModuleType.RENDER);
        overlay = new Texture("healthoverlay.png");
    }


    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        GL11.glDisable(GL11.GL_ALPHA_TEST);

        final float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        final float healthperc = health / 20F;

        if(healthperc > 0.75F) return;

        float alpha = healthperc * (4F/3F);
        alpha = 1 - alpha;
        alpha *= maxa.getValue() / 255F;

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);

        this.overlay.bind();
        this.overlay.render(0, 0, res.getScaledWidth(), res.getScaledHeight(), 0, 0, 1, 1, alpha);

        GlStateManager.disableTexture2D();
        // GlStateManager.disableBlend();
        // GlStateManager.disableAlpha();

        GL11.glEnable(GL11.GL_ALPHA_TEST);

        // fix opengl flags
        RenderUtil.drawRect(0,0,1,1,0x00FFFFFF);
    }
}
