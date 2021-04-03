package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.framework.Macro;

import java.util.ArrayList;
import java.util.List;

public final class MacroManager {

    private List<Macro> macroList = new ArrayList<>();

    public MacroManager() {
        //if (Harakiri.get().getConfigManager().isFirstLaunch()) {
        //    this.addMacro("HudEditorToggle", "RSHIFT", ".toggle hudeditor");
        //}
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
