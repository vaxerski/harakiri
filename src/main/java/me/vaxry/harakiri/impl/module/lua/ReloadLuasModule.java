package me.vaxry.harakiri.impl.module.lua;

import com.yworks.yguard.test.A;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.framework.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.util.ArrayList;

public class ReloadLuasModule extends Module {

    ArrayList<LUAAPI.LuaModule> loadedLuas = new ArrayList<>();
    ArrayList<LUAAPI.LuaModule> enabledLuas = new ArrayList<>();

    public ReloadLuasModule() {
        super("Reload LUAs", new String[]{"ReloadLUAs", "ReloadLUA"}, "Reload the list of LUAs", "NONE", -1, ModuleType.LUA);
        //this.reloadLuas();
        //Harakiri.INSTANCE.getEventManager().addEventListener(this);
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
        LUAAPI.loadAPIFunctions();
        reloadLuas();
        super.onEnable();
    }

    public void updateEnabledLuas(){
        for(Module mod : Harakiri.INSTANCE.getModuleManager().getModuleList(ModuleType.LUA)){
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

        File f = new File(System.getenv("APPDATA") + "\\.minecraft\\Harakiri\\Lua");

        pathnames = f.list();

        for (LUAAPI.LuaModule lm : loadedLuas){
            deleteModuleForLua(lm.getLuaName());
        }
        
        loadedLuas.clear();

        for (String pathname : pathnames) {
            createModuleForLua(pathname);
        }

        Harakiri.INSTANCE.logChat("Reloaded " + pathnames.length + " luas.");
    }

    private void deleteModuleForLua(String path){
        if(Harakiri.INSTANCE.getModuleManager().findLua(path) != null) {
            Harakiri.INSTANCE.getModuleManager().removeLuaModule(path);
        }
    }

    private void createModuleForLua(String path){
        // Find if exists
        if(Harakiri.INSTANCE.getModuleManager().findLua(path) != null)
            return;

        Harakiri.INSTANCE.getModuleManager().add(new Module(path, ModuleType.LUA));
        loadedLuas.add(new LUAAPI.LuaModule(path));
    }
}
