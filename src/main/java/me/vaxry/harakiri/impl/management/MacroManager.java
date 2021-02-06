package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.macro.Macro;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/7/2019 @ 4:33 AM.
 */
public final class MacroManager {

    private List<Macro> macroList = new ArrayList<>();

    public MacroManager() {
        if (Harakiri.INSTANCE.getConfigManager().isFirstLaunch()) {
            this.addMacro("HudEditorToggle", "RSHIFT", ".toggle hudeditor");
        }
    }

    public Macro find(String name) {
        for (Macro macro : this.macroList) {
            if (macro.getName().equalsIgnoreCase(name)) {
                return macro;
            }
        }
        return null;
    }

    public void addMacro(String name, String key, String macro) {
        this.macroList.add(new Macro(name, key, macro));
    }

    public void unload() {
        this.macroList.clear();
    }

    public List<Macro> getMacroList() {
        return macroList;
    }

    public void setMacroList(List<Macro> macroList) {
        this.macroList = macroList;
    }
}
