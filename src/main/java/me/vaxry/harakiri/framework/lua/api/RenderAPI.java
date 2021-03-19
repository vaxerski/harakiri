package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
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
        render.set( "render3DBox", new render3DBox() );
        render.set( "render3DLine", new render3DLine() );
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
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(text, x, y, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class renderString extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            String text = args.arg(1).checkjstring().replace("[&]", "\247");
            int x = args.arg(2).checkint();
            int y = args.arg(3).checkint();
            int col = args.arg(4).checkint();
            Harakiri.get().getTTFFontUtil().drawString(text, x, y, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class renderRect extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int x1 = args.arg(3).checkint();
            int y1 = args.arg(4).checkint();
            int col = args.arg(5).checkint();
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
            double thick = args.arg(5).checkdouble();
            int col = args.arg(6).checkint();
            RenderUtil.drawLine(x,y,x1, y1, (float)thick, col);
            return LuaValue.valueOf(1);
        }
    }

    protected static class render3DBox extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            RenderUtil.begin3D();

            final Minecraft mc = Minecraft.getMinecraft();
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int z = args.arg(3).checkint();
            int x1 = args.arg(4).checkint();
            int y1 = args.arg(5).checkint();
            int z1 = args.arg(6).checkint();
            double thick = args.arg(7).checkdouble();
            int col = args.arg(8).checkint();

            float alpha = (float) (col >> 24 & 255) / 255.0F;
            float red = (float) (col >> 16 & 255) / 255.0F;
            float green = (float) (col >> 8 & 255) / 255.0F;
            float blue = (float) (col & 255) / 255.0F;

            final AxisAlignedBB bb = new AxisAlignedBB(
                    x - mc.getRenderManager().viewerPosX,
                    y - mc.getRenderManager().viewerPosY,
                    z - mc.getRenderManager().viewerPosZ,
                    x1 - mc.getRenderManager().viewerPosX,
                    y1 - mc.getRenderManager().viewerPosY,
                    z1 - mc.getRenderManager().viewerPosZ);

            RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, alpha);

            RenderUtil.end3D();

            return LuaValue.valueOf(1);
        }
    }

    protected static class render3DLine extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            RenderUtil.begin3D();

            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int z = args.arg(3).checkint();
            int x1 = args.arg(4).checkint();
            int y1 = args.arg(5).checkint();
            int z1 = args.arg(6).checkint();
            double thick = args.arg(7).checkdouble();
            int col = args.arg(8).checkint();
            RenderUtil.drawLine3D(x,y,z,x1,y1,z1,(float)thick,col);

            RenderUtil.end3D();
            return LuaValue.valueOf(1);
        }
    }
}
