package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class CustomFontModule extends Module {

    private FontRenderer defaultRenderer;

    public CustomFontModule() {
        super("CustomFont", new String[]{"CustomFont"}, "Enables a custom TTF Font.", "NONE", -1, Module.ModuleType.RENDER);
        defaultRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Minecraft.getMinecraft().fontRenderer = Harakiri.INSTANCE.getFontRendererExtd();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Minecraft.getMinecraft().fontRenderer = defaultRenderer;
    }
}
