package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.framework.Value;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class haralua extends TwoArgFunction {
    public static haralua HARALUA = null;

    public haralua() { HARALUA = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable hara = new LuaTable(0,30);
        hara.set( "addint", new addint() );
        hara.set( "readint", new readint() );
        hara.set( "addbool", new addbool() );
        hara.set( "readbool", new readbool() );
        hara.set( "addfloat", new addfloat() );
        hara.set( "readfloat", new readfloat() );
        hara.set( "clearvalues", new clearvalues() );
        hara.set( "registerfor", new registerfor() );
        hara.set( "setTimeout", new setTimeout() );
        env.set( "haralua", hara );
        env.get("package").get("loaded").set("haralua", hara);
        return hara;
    }

    protected static class addint extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String name = args.arg(1).checkjstring();
            int defaultVal = args.arg(2).checkint();
            int minVal = args.arg(3).checkint();
            int maxVal = args.arg(4).checkint();

            String[] aliases = new String[1];
            aliases[0] = name;

            LUAAPI.currentModuleHeader.addValueInt(name, aliases, defaultVal, minVal, maxVal);
            return LuaValue.valueOf(1);
        }
    }

    protected static class readint extends TwoArgFunction {
        public LuaValue call(LuaValue lua, LuaValue name) {
            try {
                Value<Integer> val = Harakiri.get().getModuleManager().findLuaShort(lua.toString()).findValue(name.toString());
                return LuaValue.valueOf(val.getValue());
            }catch(Throwable t){
                // oops
            }
            return LuaValue.NIL;
        }
    }

    protected static class addbool extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String name = args.arg(1).checkjstring();
            boolean defaultVal = args.arg(2).checkboolean();

            String[] aliases = new String[1];
            aliases[0] = name;

            LUAAPI.currentModuleHeader.addValueBool(name, aliases, defaultVal);
            return LuaValue.valueOf(1);
        }
    }

    protected static class readbool extends TwoArgFunction {
        public LuaValue call(LuaValue lua, LuaValue name) {
            try {
                Value<Boolean> val = Harakiri.get().getModuleManager().findLuaShort(lua.toString()).findValue(name.toString());
                return LuaValue.valueOf(val.getValue());
            }catch(Throwable t){
                // oops
            }
            return LuaValue.NIL;
        }
    }

    protected static class addfloat extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String name = args.arg(1).checkjstring();
            double defaultVal = args.arg(2).checkdouble();
            double minVal = args.arg(3).checkdouble();
            double maxVal = args.arg(4).checkdouble();

            String[] aliases = new String[1];
            aliases[0] = name;

            LUAAPI.currentModuleHeader.addValueFloat(name, aliases, (float)defaultVal, (float)minVal, (float)maxVal);
            return LuaValue.valueOf(1);
        }
    }

    protected static class readfloat extends TwoArgFunction {
        public LuaValue call(LuaValue lua, LuaValue name) {
            try {
                Value<Float> val = Harakiri.get().getModuleManager().findLuaShort(lua.toString()).findValue(name.toString());
                return LuaValue.valueOf(val.getValue());
            }catch(Throwable t){
                // oops
            }
            return LuaValue.NIL;
        }
    }

    protected static class clearvalues extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            LUAAPI.currentModuleHeader.clearValues();
            return LuaValue.valueOf(1);
        }
    }

    protected static class registerfor extends OneArgFunction {
        public LuaValue call(LuaValue arg) {

            String event = arg.toString();

            if(event.equalsIgnoreCase("Render2D")){
                LUAAPI.currentLuaModuleHeader.registerForEvent(LUAAPI.EVENTFUN.EVENT_RENDER2D);
            }else if(event.equalsIgnoreCase("Render3D")){
                LUAAPI.currentLuaModuleHeader.registerForEvent(LUAAPI.EVENTFUN.EVENT_RENDER3D);
            }else if(event.equalsIgnoreCase("onEnable")){
                LUAAPI.currentLuaModuleHeader.registerForEvent(LUAAPI.EVENTFUN.EVENT_ENABLED);
            }else if(event.equalsIgnoreCase("onDisable")){
                LUAAPI.currentLuaModuleHeader.registerForEvent(LUAAPI.EVENTFUN.EVENT_DISABLED);
            }

            return LuaValue.valueOf(1);
        }
    }

    protected static class setTimeout extends OneArgFunction {
        public LuaValue call(LuaValue arg) {

            int event = arg.checkint();

            LUAAPI.currentLuaModuleHeader.setUpdateMs(event);

            return LuaValue.valueOf(1);
        }
    }
}
