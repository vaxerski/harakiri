package me.vaxry.harakiri.framework.lua;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.api.ChatAPI;
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
        EVENT_NONE, EVENT_HEADER, EVENT_2D, EVENT_3D
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

        private String rawDataEvent2D = "";
        private String rawDataEvent3D = "";

        //-------------------------------------//

        public String getLuaName(){ return luaname; }
        public boolean hasErrors(){ return hasErrors; }
        public void setErrors(boolean s){ hasErrors = s; }
    }

    public static boolean runScript(String rawdata, EVENTCODE ec){
        try {
            LuaValue chunk = JSEGlobals.load(rawdata);

            chunk.call();
        }catch(Throwable t) {
            Harakiri.INSTANCE.logChat("Your script contains errors!\nStage: " + ec.name() + "\n" + t.toString());
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
                if(line.indexOf("-- E") != -1){
                    //This is an event
                    insideEvent = true;

                    switch(line){
                        case "-- ERender2D":
                            writeToEvent = EVENTCODE.EVENT_2D;
                            break;
                        case "-- ERender3D":
                            writeToEvent = EVENTCODE.EVENT_3D;
                            break;
                    }
                }
                if(line.contains("-- end")){
                    insideEvent = false;
                    writeToEvent = EVENTCODE.EVENT_NONE;
                }

                if(insideEvent){
                    switch (writeToEvent){
                        case EVENT_2D:
                            luaModule.rawDataEvent2D += "\n" + line;
                            break;
                        case EVENT_3D:
                            luaModule.rawDataEvent3D += "\n" + line;
                            break;
                    }
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
        if(!runScript(luaModule.header, EVENTCODE.EVENT_HEADER))
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

        // other libs
        JSEGlobals.load(new ChatAPI());
    }

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    public static void onRender2D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!runScript(lua.rawDataEvent2D, EVENTCODE.EVENT_2D))
                lua.setErrors(true);
        }
    }

    public static void onRender3D(ArrayList<LuaModule> enabledluas){
        for(LuaModule lua : enabledluas){
            if(!runScript(lua.rawDataEvent3D, EVENTCODE.EVENT_3D))
                lua.setErrors(true);
        }
    }
}
