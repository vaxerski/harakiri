package me.vaxry.harakiri.api.lua;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.lua.api.ChatAPI;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public final class LUAAPI {
    private static Globals JSEGlobals;

    public static class LuaModule{
        // Basic Lua module.

        private String luaname;
        private boolean hasErrors = false;

        public LuaModule(String luaName){
            luaname = luaName;
        }

        public String getLuaName(){
            return luaname;
        }

        public boolean hasErrors(){ return hasErrors; }
        public void setErrors(boolean s){ hasErrors = s; }
    }

    public static boolean runScript(String luaname){
        try {
            LuaValue chunk = JSEGlobals.loadfile(System.getenv("APPDATA") + "\\.minecraft\\Harakiri\\Lua\\" + luaname);

            chunk.call();
        }catch(Throwable t) {
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            //Harakiri.INSTANCE.logChat("StorageESP Threw an Error: " + errors.toString());
            JOptionPane.showMessageDialog(null, errors.toString(), "Error in a LUA file!", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    public static void loadAPIFunctions(){
        JSEGlobals = JsePlatform.standardGlobals();
        JSEGlobals.load(new ChatAPI());
    }

    public static void onRender2D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!runScript(lua.luaname))
                lua.setErrors(true);
        }
    }
}
