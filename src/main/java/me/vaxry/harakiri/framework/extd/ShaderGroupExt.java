package me.vaxry.harakiri.framework.extd;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ShaderGroupExt extends ShaderGroup {

    public ShaderGroupExt(TextureManager p_i1050_1_, IResourceManager resourceManagerIn, Framebuffer mainFramebufferIn, ResourceLocation p_i1050_4_) throws JsonException, IOException, JsonSyntaxException {
        super(p_i1050_1_, resourceManagerIn, mainFramebufferIn, p_i1050_4_);
    }

    public Shader addShader(String programName, Framebuffer framebufferIn, Framebuffer framebufferOut) throws JsonException, IOException {
        Shader shader = new ShaderExt(Minecraft.getMinecraft().getResourceManager(), programName, framebufferIn, framebufferOut);
        this.listShaders.add(this.listShaders.size(), shader);
        return shader;
    }
}
