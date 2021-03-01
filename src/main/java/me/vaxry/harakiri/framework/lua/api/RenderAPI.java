package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class RenderAPI extends TwoArgFunction {

    public static RenderAPI RENDERAPI = null;

    public RenderAPI() { RENDERAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable render = new LuaTable(0,30);
        render.set( "renderStringShadow", new renderStringShadow() );
        render.set( "renderString", new renderString() );
        render.set( "renderRect", new renderRect() );
        render.set( "renderLine", new renderLine() );
        env.set( "render", render );
        env.get("package").get("loaded").set("render", render);
        return render;
    }

    protected static class renderStringShadow extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String text = args.arg(1).checkjstring().replace("[&]", "\247");
            int x = args.arg(2).checkint();
            int y = args.arg(3).checkint();
            int col = args.arg(4).checkint();
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(text, x, y, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class renderString extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String text = args.arg(1).checkjstring().replace("[&]", "\247");
            int x = args.arg(2).checkint();
            int y = args.arg(3).checkint();
            int col = args.arg(4).checkint();
            Harakiri.INSTANCE.getTTFFontUtil().drawString(text, x, y, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class renderRect extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int x1 = args.arg(3).checkint();
            int y1 = args.arg(4).checkint();
            int col = args.arg(4).checkint();
            RenderUtil.drawRect(x,y,x1, y1, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class renderLine extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int x1 = args.arg(3).checkint();
            int y1 = args.arg(4).checkint();
            double thick = args.arg(4).checkdouble();
            int col = args.arg(4).checkint();
            RenderUtil.drawLine(x,y,x1, y1, (float)thick, col);
            return LuaValue.valueOf(1);
        }
    }
}
