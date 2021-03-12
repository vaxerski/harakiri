package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ComponentAPI extends TwoArgFunction {

    public static ComponentAPI COMPONENTAPI = null;

    public ComponentAPI() { COMPONENTAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable component = new LuaTable(0,30);
        component.set( "setEnabled", new setEnabled() );
        component.set( "isEnabled", new isEnabled() );
        component.set( "setX", new setX() );
        component.set( "setY", new setY() );
        component.set( "setW", new setW() );
        component.set( "setH", new setH() );
        component.set( "getX", new getX() );
        component.set( "getX", new getX() );
        component.set( "getX", new getX() );
        component.set( "getX", new getX() );
        env.set( "component", component );
        env.get("package").get("loaded").set("component", component);
        return component;
    }

    protected static class setEnabled extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue enabled){
            try {
                Harakiri.get().getHudManager().findComponent(modulename.toString()).setVisible(enabled.checkboolean());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class isEnabled extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                return LuaValue.valueOf(Harakiri.get().getHudManager().findComponent(modulename.toString()).isVisible());
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }

    protected static class setX extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue x){
            try {
                Harakiri.get().getHudManager().findComponent(modulename.toString()).setX((float)x.checkdouble());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setY extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue x){
            try {
                Harakiri.get().getHudManager().findComponent(modulename.toString()).setY((float)x.checkdouble());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setW extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue x){
            try {
                Harakiri.get().getHudManager().findComponent(modulename.toString()).setW((float)x.checkdouble());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class setH extends TwoArgFunction {
        public LuaValue call(LuaValue modulename, LuaValue x){
            try {
                Harakiri.get().getHudManager().findComponent(modulename.toString()).setH((float)x.checkdouble());
            }catch(Throwable t){
                return LuaValue.valueOf(0);
            }
            return LuaValue.valueOf(1);
        }
    }

    protected static class getX extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                return LuaValue.valueOf(Harakiri.get().getHudManager().findComponent(modulename.toString()).getX());
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }

    protected static class getY extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                return LuaValue.valueOf(Harakiri.get().getHudManager().findComponent(modulename.toString()).getY());
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }

    protected static class getW extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                return LuaValue.valueOf(Harakiri.get().getHudManager().findComponent(modulename.toString()).getW());
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }

    protected static class getH extends OneArgFunction {
        public LuaValue call(LuaValue modulename){
            try {
                return LuaValue.valueOf(Harakiri.get().getHudManager().findComponent(modulename.toString()).getH());
            }catch(Throwable t){
                return LuaValue.NIL;
            }
        }
    }
}
