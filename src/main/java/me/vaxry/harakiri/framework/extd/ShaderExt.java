package me.vaxry.harakiri.framework.extd;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.util.JsonException;

import java.io.IOException;

public class ShaderExt extends Shader {
    public ShaderExt(IResourceManager resourceManager, String programName, Framebuffer framebufferInIn, Framebuffer framebufferOutIn) throws JsonException, IOException {
        super(resourceManager, programName, framebufferInIn, framebufferOutIn);
        this.manager = new ShaderManagerExt(resourceManager, programName);
    }

}
