package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

public class ModuleAPI extends TwoArgFunction {

    public static ModuleAPI MODULEAPI = null;

    public ModuleAPI() { MODULEAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable(0,30);
        module.set( "toggle", new toggle() );
        module.set( "setEnabled", new setEnabled() );
        module.set( "setValue", new setValue() );
        env.set( "module", module );
        env.get("package").get("loaded").set("module", module);
        return module;
    }

    protected static class toggle extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                Harakiri.INSTANCE.getModuleManager().find(modulename.toString()).toggle();
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setEnabled extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue enabled){
            try {
                Harakiri.INSTANCE.getModuleManager().find(modulename.toString()).setEnabled(enabled.checkboolean());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setValue extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String module = args.arg(1).checkjstring();
            String value = args.arg(2).checkjstring();
            double setval = args.arg(3).checkdouble();
            try {
                // Check if the value is an int, if it is, set it as an int.
                if(args.arg(3).checkdouble() != (int)args.arg(3).checkdouble())
                    Harakiri.INSTANCE.getModuleManager().find(module).findValue(value).setValue((float)setval);
                else
                    Harakiri.INSTANCE.getModuleManager().find(module).findValue(value).setValue((int)setval);
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }
}
