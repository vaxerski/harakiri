package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class GuiPlusModule extends Module {

    public final Value<Boolean> removeBackground = new Value<Boolean>("RemoveBackground", new String[]{"RemoveBackground", "rb"}, "Do not draw the background in GUIs.", false);

    public GuiPlusModule(){
        super("Gui+", new String[]{"GuiPlus"}, "Adjusts default GUI behaviour.", "NONE", -1, ModuleType.RENDER);
    }

}
