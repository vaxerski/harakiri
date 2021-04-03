package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;

public class CustomFontModule extends Module {

    public CustomFontModule() {
        super("CustomFont", new String[]{"CustomFont"}, "Enables a custom TTF Font.", "NONE", -1, Module.ModuleType.RENDER);
        this.setEnabled(true);
        this.onEnable();
        this.setHidden(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Harakiri.get().getTTFFontUtil().isTTF = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getTTFFontUtil().isTTF = false;
    }
}
