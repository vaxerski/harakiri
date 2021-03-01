package me.vaxry.harakiri.framework.lua;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.api.ChatAPI;
import me.vaxry.harakiri.framework.lua.api.GlobalAPI;
import me.vaxry.harakiri.framework.lua.api.haralua;
import me.vaxry.harakiri.framework.module.Module;
import net.minecraftforge.fml.common.Mod;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class LUAAPI {
    private static Globals JSEGlobals;
    public static Module currentModuleHeader = null;

    private enum EVENTCODE {
        EVENT_NONE, EVENT_HEADER, EVENT_SCRIPT
    }

    private enum EVENTFUN {
        EVENT_NONE, EVENT_RENDER2D, EVENT_RENDER3D
    }

    public static class LuaModule{
        // Basic Lua module.

        private String luaname;
        private boolean hasErrors = false;

        public LuaModule(String luaName){
            luaname = luaName;
            if(!parseLUAScript(luaName, this))
                return;
            currentModuleHeader = Harakiri.INSTANCE.getModuleManager().findLua(luaName);
            if(currentModuleHeader == null)
                Harakiri.INSTANCE.logChat("CMH is null!!!!!!");
            applyLUAHeader(this);
            currentModuleHeader = null;
        }

        //-------------Event Data--------------//
        private String header = "";

        private String rawDataScript = "";

        //-------------------------------------//

        public String getLuaName(){ return luaname; }
        public boolean hasErrors(){ return hasErrors; }
        public void setErrors(boolean s){ hasErrors = s; }
    }

    public static boolean runScript(String rawdata, EVENTCODE ec, EVENTFUN ef){
        try {

            JSEGlobals.load(rawdata, "luascript").call();

            if(ec != EVENTCODE.EVENT_NONE && ef != EVENTFUN.EVENT_NONE && ef != null) {
                // running script
                LuaValue fun = null;

                switch(ef){
                    case EVENT_NONE:
                        return true;
                    case EVENT_RENDER2D:
                        fun = JSEGlobals.get("EventRender2D");
                        break;
                    case EVENT_RENDER3D:
                        fun = JSEGlobals.get("EventRender3D");
                        break;
                }

                if(fun != null && fun != LuaValue.NIL)
                    fun.call();
            }

        }catch(Throwable t) {
            Harakiri.INSTANCE.logChat("Your script contains errors!\nStage: " + ec.name() + "\n" + t.toString().substring(t.toString().contains("luaj") ? t.toString().indexOf(':') + 1 : 0));
            return false;
        }
        return true;
    }

    public static boolean parseLUAScript(String name, LuaModule luaModule){
        String filepath = System.getenv("APPDATA") + "\\.minecraft\\Harakiri\\Lua\\" + name;
        File luafile = new File(filepath);

        String rawdata = "";

        try {
            Scanner reader = new Scanner(luafile);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                rawdata += "\n" + data;
            }
        }catch(Throwable t){
            JOptionPane.showMessageDialog(null, t.toString(), "Error while parsing " + name, JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        // Parse the header

        if(!rawdata.contains("-- LUAHEADER")){
            Harakiri.INSTANCE.logChat(luaModule.luaname + ": Lua has no LUAHEADER. Please read the documentation on how to create one.");
            return false;
        }

        luaModule.header = rawdata.substring(rawdata.indexOf("-- LUAHEADER"), rawdata.indexOf("-- endheader"));

        // end

        // Now, split into events
        BufferedReader bufReader = new BufferedReader(new StringReader(rawdata));

        boolean insideEvent = false;
        EVENTCODE writeToEvent = EVENTCODE.EVENT_NONE;
        try {
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                if(line.indexOf("-- Script") != -1){
                    //This is an event
                    insideEvent = true;
                }
                if(line.contains("-- end")){
                    insideEvent = false;
                    writeToEvent = EVENTCODE.EVENT_NONE;
                }

                if(insideEvent){
                    luaModule.rawDataScript += "\n" + line;
                }
            }
        }catch (Throwable t){
            Harakiri.INSTANCE.logChat("Error while parsing " + name + ": " + t.toString());
            return false;
        }

        // We got all done, exit.
        return true;
    }

    public static void applyLUAHeader(LuaModule luaModule){
        if(!runScript(luaModule.header, EVENTCODE.EVENT_HEADER, EVENTFUN.EVENT_NONE))
            luaModule.setErrors(true);
    }

    public static void loadAPIFunctions(){
        Globals newglobals = new Globals();
        newglobals.load(new JseBaseLib());
        newglobals.load(new PackageLib());
        newglobals.load(new StringLib());
        newglobals.load(new JseMathLib());
        LoadState.install(newglobals);
        LuaC.install(newglobals);
        JSEGlobals = newglobals;

        // header lib
        JSEGlobals.load(new haralua());

        // harakiri other libs
        JSEGlobals.load(new ChatAPI());
        JSEGlobals.load(new GlobalAPI());
    }

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    public static void onRender2D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!runScript(lua.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_RENDER2D))
                lua.setErrors(true);
        }
    }

    public static void onRender3D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!runScript(lua.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_RENDER3D))
                lua.setErrors(true);
        }
    }
}