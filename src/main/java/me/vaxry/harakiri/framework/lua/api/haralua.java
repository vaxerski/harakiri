package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.framework.value.Value;
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
        hara.set( "clearvalues", new clearvalues() );
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

    protected static class clearvalues extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            LUAAPI.currentModuleHeader.clearValues();
            return LuaValue.valueOf(1);
        }
    }
}
