package me.vaxry.harakiri.framework.extd;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.client.util.JsonException;

import java.io.IOException;

public class ShaderManagerExt extends ShaderManager {
    public ShaderManagerExt(IResourceManager resourceManager, String programName) throws JsonException, IOException {
        super(resourceManager, programName);
    }
}
