package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.gui.hud.component.WatermarkComponent;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ModuleAPI extends TwoArgFunction {

    public static ModuleAPI MODULEAPI = null;

    public ModuleAPI() { MODULEAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable module = new LuaTable(0,30);
        module.set( "toggle", new toggle() );
        module.set( "setEnabled", new setEnabled() );
        module.set( "setValue", new setValue() );
        module.set( "getValue", new getValue() );
        env.set( "module", module );
        env.get("package").get("loaded").set("module", module);
        return module;
    }

    protected static class toggle extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                Harakiri.get().getModuleManager().find(modulename.toString()).toggle();
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setEnabled extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue enabled){
            try {
                Module mod = Harakiri.get().getModuleManager().find(modulename.toString());
                if(mod.isEnabled() == enabled.checkboolean()) return LuaValue.valueOf(1);

                mod.toggle();

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

            if(module.equalsIgnoreCase("watermark") && value.equalsIgnoreCase("text")){
                ((WatermarkComponent)Harakiri.get().getHudManager().findComponent(WatermarkComponent.class)).watermark = args.arg(3).checkjstring();
                return LuaValue.valueOf(1);
            }

            try {
                Value realVal = Harakiri.get().getModuleManager().find(module).findValue(value);
                if(realVal.getValue() instanceof Boolean){
                    realVal.setValue(args.arg(3).checkboolean());
                }else if(realVal.getValue() instanceof Integer){
                    realVal.setValue(args.arg(3).checkint());
                }else if(realVal.getValue() instanceof Float){
                    realVal.setValue((float)args.arg(3).checkdouble());
                }else if(realVal.getValue() instanceof Double){
                    realVal.setValue(args.arg(3).checkdouble());
                }else{
                    LuaValue.valueOf(0);
                }
            }catch(Throwable t){
                Harakiri.get().logChat(t.toString());
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class getValue extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue value){
            try {
                if(modulename.checkjstring().equalsIgnoreCase("watermark") && value.checkjstring().equalsIgnoreCase("text")){
                    return LuaValue.valueOf(((WatermarkComponent)Harakiri.get().getHudManager().findComponent(WatermarkComponent.class)).watermark);
                }

                Value realVal = Harakiri.get().getModuleManager().find(modulename.toString()).findValue(value.toString());
                if(realVal.getValue() instanceof Boolean){
                    return LuaValue.valueOf(Boolean.valueOf((Boolean)realVal.getValue()));
                }else if(realVal.getValue() instanceof Integer){
                    return LuaValue.valueOf(Integer.valueOf((Integer)realVal.getValue()));
                }else if(realVal.getValue() instanceof Float){
                    return LuaValue.valueOf(Float.valueOf((Float)realVal.getValue()));
                }else if(realVal.getValue() instanceof Double){
                    return LuaValue.valueOf(Double.valueOf((Double)realVal.getValue()));
                }else{
                    return LuaValue.NIL;
                }
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }
}
