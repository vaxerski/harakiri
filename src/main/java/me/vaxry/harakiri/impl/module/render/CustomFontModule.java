package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class CustomFontModule extends Module {

    public CustomFontModule() {
        super("CustomFont", new String[]{"CustomFont"}, "Enables a custom TTF Font.", "NONE", -1, Module.ModuleType.RENDER);
        //this.setEnabled(true);
        Harakiri.INSTANCE.getTTFFontUtil().isTTF = this.isEnabled();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Harakiri.INSTANCE.getTTFFontUtil().isTTF = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.INSTANCE.getTTFFontUtil().isTTF = false;
    }
}
