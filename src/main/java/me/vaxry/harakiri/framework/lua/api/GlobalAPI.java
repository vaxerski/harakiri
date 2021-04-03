package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Notification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class GlobalAPI extends TwoArgFunction {

    public static GlobalAPI GLOBALAPI = null;

    public GlobalAPI() { GLOBALAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable global = new LuaTable(0,30);
        global.set( "curtime", new curtime() );
        global.set( "curhumantime", new curhumantime() );
        global.set( "scaledX", new scaledX() );
        global.set( "scaledY", new scaledY() );
        global.set( "addNotification", new addNotification() );
        env.set( "global", global );
        env.get("package").get("loaded").set("global", global);
        return global;
    }

    private static class HumanTimeReturn {
        public int hour = 0;
        public int minute = 0;
        public int second = 0;
    }

    protected static class curtime extends ZeroArgFunction {
        public LuaValue call(){
            return LuaValue.valueOf(System.currentTimeMillis());
        }
    }

    protected static class curhumantime extends ZeroArgFunction {
        public LuaValue call(){
            HumanTimeReturn humanTimeReturn = new HumanTimeReturn();
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date

            humanTimeReturn.hour = calendar.get(Calendar.HOUR_OF_DAY);
            humanTimeReturn.minute = calendar.get(Calendar.MINUTE);
            humanTimeReturn.second = calendar.get(Calendar.SECOND);

            return CoerceJavaToLua.coerce(humanTimeReturn);
        }
    }

    protected static class scaledX extends ZeroArgFunction {
        public LuaValue call(){
            ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            return LuaValue.valueOf(res.getScaledWidth());
        }
    }

    protected static class scaledY extends ZeroArgFunction {
        public LuaValue call(){
            ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            return LuaValue.valueOf(res.getScaledHeight());
        }
    }

    protected static class addNotification extends TwoArgFunction {
        public LuaValue call(LuaValue message, LuaValue duration){
            String messag = message.checkjstring();
            double dur = duration.checkdouble();

            Harakiri.get().getNotificationManager().addNotification(messag, messag, Notification.Type.INFO, (int)dur);

            return LuaValue.valueOf(1);
        }
    }
}
