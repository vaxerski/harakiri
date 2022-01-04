package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.Minecraft;

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
        chat.set( "raw", new raw() );
        env.set( "chat", chat );
        env.get("package").get("loaded").set("chat", chat);
        return chat;
    }

    protected static class logchat extends OneArgFunction {
        public LuaValue call(LuaValue message) {
            Harakiri.get().logChat(message.toString());
            return LuaValue.valueOf(1);
        }
    }

    protected static class raw extends OneArgFunction {
        public LuaValue call(LuaValue message) {
            Minecraft.getMinecraft().player.sendChatMessage(message.toString());
            return LuaValue.valueOf(1);
        }
    }
}
