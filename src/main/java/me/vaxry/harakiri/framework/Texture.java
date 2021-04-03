package me.vaxry.harakiri.framework;

import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

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

    public void render(float x, float y, float width, float height, float u, float v, float t, float s, float a) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.bind();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + width, y, 0F).tex(t, v).color(1F,1F,1F,a).endVertex();
        bufferbuilder.pos(x, y, 0F).tex(u, v).color(1F,1F,1F,a).endVertex();
        bufferbuilder.pos(x, y + height, 0F).tex(u, s).color(1F,1F,1F,a).endVertex();
        bufferbuilder.pos(x, y + height, 0F).tex(u, s).color(1F,1F,1F,a).endVertex();
        bufferbuilder.pos(x + width, y + height, 0F).tex(t, s).color(1F,1F,1F,a).endVertex();
        bufferbuilder.pos(x + width, y, 0F).tex(t, v).color(1F,1F,1F,a).endVertex();
        tessellator.draw();
    }

    public void render(float x, float y, float width, float height) {
        this.render(x, y, width, height, 0, 0, 1, 1);
    }

    public void bind() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.textureLocation);
    }
}
