package me.vaxry.harakiri.impl.module.lua;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.gui.HudComponent;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.impl.config.LuaConfig;
import me.vaxry.harakiri.impl.gui.hud.component.special.ModuleListComponent;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.util.ArrayList;

public class ReloadLuasModule extends Module {

    boolean luaInit = true;

    public ArrayList<LUAAPI.LuaModule> loadedLuas = new ArrayList<>();
    public ArrayList<LUAAPI.LuaModule> enabledLuas = new ArrayList<>();

    public ReloadLuasModule() {
        super("Reload LUAs", new String[]{"ReloadLUAs", "ReloadLUA"}, "Reload the list of LUAs", "NONE", -1, ModuleType.LUA);
        //this.reloadLuas();
        //Harakiri.get().getEventManager().addEventListener(this);
    }


    // ------------------------------------------------ //
    //                    LUA EVENTS                    //
    // ------------------------------------------------ //

    @Listener
    public void render2D(EventRender2D event){
        // Update LUAs
        updateEnabledLuas();

        LUAAPI.onRender2D(enabledLuas);
    }

    @Listener
    public void render3D(EventRender3D event){
        // Update LUAs
        updateEnabledLuas();

        LUAAPI.onRender3D(enabledLuas);
    }

    // ------------------------------------------------ //
    // ------------------------------------------------ //
    // ------------------------------------------------ //
    // ------------------------------------------------ //
    // ------------------------------------------------ //

    @Override
    public void onEnable() {
        this.setEnabled(false);
        reloadLuas();
        super.onEnable();
    }

    // Used for init-ting luas when we first launch the game.
    public void loadLuas() {
        this.setEnabled(false);
        reloadLuas();
        super.onEnable();
    }

    public void updateEnabledLuas(){
        for(Module mod : Harakiri.get().getModuleManager().getModuleList(ModuleType.LUA)){
            if(mod instanceof ReloadLuasModule)
                continue;
            if(!mod.isEnabled() && isLuaEnabled(mod.luaName)) {
                disableLuaByName(mod.luaName);
            }
            if(mod.isEnabled() && !isLuaEnabled(mod.luaName)){
                enableLuaByName(mod.luaName);
            }
        }
    }

    public LUAAPI.LuaModule getLuaModuleByName(String name){
        for(LUAAPI.LuaModule lm : loadedLuas) {
            if (lm.getLuaName().equalsIgnoreCase(name))
                return lm;
        }
        return null;
    }

    private boolean isLuaLoaded(String path){
        for(LUAAPI.LuaModule lm : loadedLuas) {
            if (lm.getLuaName().equalsIgnoreCase(path))
                return true;
        }
        return false;
    }

    private boolean isLuaEnabled(String path){
        for(LUAAPI.LuaModule lm : enabledLuas) {
            if (lm.getLuaName().equalsIgnoreCase(path))
                return true;
        }
        return false;
    }

    private void disableLuaByName(String name) {
        LUAAPI.LuaModule del = null;
        for(LUAAPI.LuaModule lm : enabledLuas) {
            if (lm.getLuaName().equalsIgnoreCase(name)){
                del = lm;
                break;
            }
        }
        if(del == null)
            return;
        del.setErrors(false);
        enabledLuas.remove(del);
    }

    private void enableLuaByName(String name) {
        LUAAPI.LuaModule add = null;
        for(LUAAPI.LuaModule lm : loadedLuas) {
            if (lm.getLuaName().equalsIgnoreCase(name)){
                add = lm;
                break;
            }
        }
        if(add == null)
            return;

        enabledLuas.add(add);
    }

    private void reloadLuas(){
        String[] pathnames;

        LuaConfig config = null;
        for(Configurable c : Harakiri.get().getConfigManager().getConfigurableList()){
            if(c instanceof LuaConfig)
                config = (LuaConfig)c;
        }

        // Save config
        if(config != null)
            config.onSave();

        File f = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/Lua" : "\\harakiri\\Lua"));

        pathnames = f.list();

        for (LUAAPI.LuaModule lm : loadedLuas){
            deleteModuleForLua(lm.getLuaName());
        }

        loadedLuas.clear();

        for (String pathname : pathnames) {
            createModuleForLua(pathname);
        }

        try {
            Harakiri.get().logChat("Reloaded " + pathnames.length + " luas.");
        }catch (Throwable t){
            // Throws when loading the game.
        }

        // Load config
        if(config != null)
            config.onLoad();

        // Clean open settings
        for(HudComponent hc : Harakiri.get().getHudManager().getComponentList()){
            if(!(hc instanceof ModuleListComponent))
                continue;
            if(((ModuleListComponent) hc).getType() != ModuleType.LUA)
                continue;

            ((ModuleListComponent) hc).cleanCurrentSettings();
            break;
        }
    }

    private void deleteModuleForLua(String path){
        if(Harakiri.get().getModuleManager().findLua(path) != null) {
            Harakiri.get().getModuleManager().removeLuaModule(path);
        }
    }

    private void createModuleForLua(String path){
        // Find if exists
        if(Harakiri.get().getModuleManager().findLua(path) != null)
            return;

        Harakiri.get().getModuleManager().add(new Module(path, ModuleType.LUA));
        loadedLuas.add(new LUAAPI.LuaModule(path));
    }
}
