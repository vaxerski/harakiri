package me.vaxry.harakiri.framework.texture;

import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * created by noil on 11/5/19 at 8:51 PM
 */
public class Texture {

    private final ResourceLocation textureLocation;

    public Texture(String name) {
        this.textureLocation = new ResourceLocation("harakirimod", "textures/" + name);
        this.bind();
    }

    public void render(float x, float y, float width, float height, float u, float v, float t, float s) {
        this.bind();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderUtil.drawTexture(x, y, width, height, u, v, t, s);
    }

    public void render(float x, float y, float width, float height) {
        this.render(x, y, width, height, 0, 0, 1, 1);
    }

    public void bind() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.textureLocation);
    }
}