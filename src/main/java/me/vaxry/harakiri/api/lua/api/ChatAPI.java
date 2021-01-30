package me.vaxry.harakiri.api.lua.api;

import me.vaxry.harakiri.Harakiri;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

public class ChatAPI extends TwoArgFunction {

    public static ChatAPI CHATAPI = null;

    public ChatAPI() { CHATAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable chat = new LuaTable(0,30);
        chat.set( "log", new logchat() );
        env.set( "chat", chat );
        env.get("package").get("loaded").set("chat", chat);
        return chat;
    }

    protected static class logchat extends OneArgFunction {
        public LuaValue call(LuaValue message){
            Harakiri.INSTANCE.logChat(message.toString());
            return LuaValue.valueOf(1);
        }
    }
}
