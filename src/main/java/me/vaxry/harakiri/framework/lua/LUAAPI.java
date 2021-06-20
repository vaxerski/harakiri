package me.vaxry.harakiri.framework.lua;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.api.*;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.client.Minecraft;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public final class LUAAPI {
    public static Module currentModuleHeader = null;
    public static LuaModule currentLuaModuleHeader = null;

    private enum EVENTCODE {
        EVENT_NONE, EVENT_HEADER, EVENT_SCRIPT, EVENT_SCRIPT_PRELOAD
    }

    public enum EVENTFUN {
        EVENT_NONE, EVENT_RENDER2D, EVENT_RENDER3D, EVENT_ENABLED, EVENT_DISABLED
    }

    public static class LuaModule{
        // Lua module class.
        // Handles execution -> Globals are split to avoid conflicts.

        public Globals JSEGlobals;

        private String luaname;
        private boolean hasErrors = false;
        private int updateMs = 0;
        private HashMap<EVENTFUN, Timer> updateTimers = new HashMap<>();

        private ArrayList<EVENTFUN> registeredForEvents = new ArrayList<>();

        public LuaModule(String luaName){
            luaname = luaName;

            loadAPIFunctions();

            if(!parseLUAScript(luaName, this))
                return;

            currentModuleHeader = Harakiri.get().getModuleManager().findLua(luaName);
            currentLuaModuleHeader = this;
            applyLUAHeader();

            // So we dont run the entire thing later on, just the func we are interested in.
            this.runScript(this.rawDataScript, EVENTCODE.EVENT_SCRIPT_PRELOAD, EVENTFUN.EVENT_NONE);

            currentModuleHeader = null;
            currentLuaModuleHeader = null;

            // Create timers
            for(EVENTFUN ef : registeredForEvents) {
                Timer timer = new Timer();
                timer.reset();
                updateTimers.put(ef, timer);
            }
        }

        public void loadAPIFunctions(){
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
            JSEGlobals.load(new RenderAPI());
            JSEGlobals.load(new ModuleAPI());
            JSEGlobals.load(new ComponentAPI());
            JSEGlobals.load(new EntityAPI());
            JSEGlobals.load(new WorldAPI());
        }

        public boolean runScript(String rawdata, EVENTCODE ec, EVENTFUN ef){
            try {

                if(ec == EVENTCODE.EVENT_HEADER || ec == EVENTCODE.EVENT_SCRIPT_PRELOAD)
                    JSEGlobals.load(rawdata, "luascript").call();

                if(ec != EVENTCODE.EVENT_NONE && ef != EVENTFUN.EVENT_NONE && ef != null) {
                    // running script
                    LuaValue fun = null;

                    if(!this.isRegisteredForEvent(ef))
                        return true;

                    if(!this.updateTimers.get(ef).passed(this.updateMs))
                        return true;

                    // If the timeout has passed, reset the timer
                    this.updateTimers.get(ef).reset();

                    switch(ef){
                        case EVENT_NONE:
                            return true;
                        case EVENT_RENDER2D:
                            fun = JSEGlobals.get("EventRender2D");
                            break;
                        case EVENT_RENDER3D:
                            fun = JSEGlobals.get("EventRender3D");
                            break;
                        case EVENT_DISABLED:
                            fun = JSEGlobals.get("EventDisabled");
                            break;
                        case EVENT_ENABLED:
                            fun = JSEGlobals.get("EventEnabled");
                            break;
                    }

                    if(fun != null && fun != LuaValue.NIL)
                        fun.call();
                }

            }catch(Throwable t) {
                try {
                    Harakiri.get().logChat("Your script contains errors!\nStage: " + ec.name() + "\n" + t.toString().substring(t.toString().contains("luaj") ? t.toString().indexOf(':') + 1 : 0));
                }catch (Throwable t2){
                    // Throws when game loading
                }
                return false;
            }
            return true;
        }

        public void applyLUAHeader() {
            // Add provided globals
            String header = "thislua = \"" + this.luaname.substring(0, this.luaname.indexOf(".lua")) + "\"\n";

            header += this.header;

            if(!runScript(header, EVENTCODE.EVENT_HEADER, EVENTFUN.EVENT_NONE))
                this.setErrors(true);
        }

        public void registerForEvent(EVENTFUN ef){
            this.registeredForEvents.add(ef);
        }

        private boolean isRegisteredForEvent(EVENTFUN ef){
            for(EVENTFUN eff : this.registeredForEvents){
                if(eff == ef)
                    return true;
            }
            return false;
        }

        public void setUpdateMs(int ms){
            this.updateMs = ms;
        }

        public int getUpdateMs(){
            return this.updateMs;
        }

        //--------------Code Data--------------//
        private String header = "";

        private String rawDataScript = "";
        //-------------------------------------//

        public String getLuaName(){ return luaname; }
        public boolean hasErrors(){ return hasErrors; }
        public void setErrors(boolean s){ hasErrors = s; }
    }

    public static boolean parseLUAScript(String name, LuaModule luaModule){
        String filepath = Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/Lua/" : "\\harakiri\\Lua\\") + name;
        File luafile = new File(filepath);

        String rawdata = "";

        try {
            Scanner reader = new Scanner(luafile);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                rawdata += "\n" + data;
            }
        }catch(Throwable t){
            return false;
        }

        // Parse the header

        if(!rawdata.contains("-- LUAHEADER")){
            Harakiri.get().logChat(luaModule.luaname + ": Lua has no LUAHEADER. Please read the documentation on how to create one.");
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
            try {
                Harakiri.get().logChat("Error while parsing " + name + ": " + t.toString());
            }catch(Throwable t2){
                //Throws when game load
            }
            return false;
        }

        // We got all done, exit.
        return true;
    }

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    public static void onRender2D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!lua.runScript(lua.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_RENDER2D))
                lua.setErrors(true);
        }
    }

    public static void onRender3D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!lua.runScript(lua.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_RENDER3D))
                lua.setErrors(true);
        }
    }

    public static void onEnabled(LuaModule luaModule){
        if(!luaModule.runScript(luaModule.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_ENABLED))
            luaModule.setErrors(true);
    }

    public static void onDisabled(LuaModule luaModule){
        if(!luaModule.runScript(luaModule.rawDataScript, EVENTCODE.EVENT_SCRIPT, EVENTFUN.EVENT_DISABLED))
            luaModule.setErrors(true);
    }
}
