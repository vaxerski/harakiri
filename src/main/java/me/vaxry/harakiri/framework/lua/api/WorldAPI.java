package me.vaxry.harakiri.framework.lua.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class WorldAPI extends TwoArgFunction {

    public static WorldAPI WORLDAPI = null;

    public WorldAPI() { WORLDAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable world = new LuaTable(0,30);
        world.set( "scanRadius", new scanRadius() );
        world.set( "isSourceBlock", new isSourceBlock() );
        world.set( "getScanPos", new getScanPos() );
        env.set( "world", world );
        env.get("package").get("loaded").set("world", world);
        return world;
    }

    public static class BlockScanResult {
        public int[][][] returnBlocks;

        public BlockScanResult(int rx, int ry, int rz){
            final Minecraft mc = Minecraft.getMinecraft();

            returnBlocks = new int[rx*2 + 2][ry*2 + 2][rz*2 + 2];

            BlockPos playerPos = mc.player.getPosition();

            for(int x = 0; x < rx * 2 + 1; ++x){
                for(int y = 0; y < ry * 2 + 1; ++y){
                    for(int z = 0; z < rz * 2 + 1; ++z){
                        IBlockState blockState = mc.world.getBlockState(new BlockPos(x + playerPos.getX() - rx,y + playerPos.getY() - ry,z + playerPos.getZ() - rz));
                        Block block = blockState.getBlock();

                        returnBlocks[x][y][z] = Block.getIdFromBlock(block);

                        if(z + 1 > rz*2+2)
                            break;
                    }
                    if(y + 1 > ry*2+2)
                        break;
                }
                if(x + 1 > rx*2+2)
                    break;
            }
        }
    }

    protected static class scanRadius extends VarArgFunction {
        public LuaValue invoke(Varargs args) {
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int z = args.arg(3).checkint();

            return CoerceJavaToLua.coerce(new BlockScanResult(x,y,z));
        }
    }

    protected static class getScanPos extends ZeroArgFunction {
        public LuaValue call() {
            BlockPos playerPos = Minecraft.getMinecraft().player.getPosition();
            int[] scanCenter = new int[3];
            scanCenter[0] = playerPos.getX();
            scanCenter[1] = playerPos.getY();
            scanCenter[2] = playerPos.getZ();
            return CoerceJavaToLua.coerce(scanCenter);
        }
    }


    protected static class isSourceBlock extends VarArgFunction {
        public LuaValue invoke(Varargs args) {
            int x = args.arg(1).checkint();
            int y = args.arg(2).checkint();
            int z = args.arg(3).checkint();

            IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(new BlockPos(x,y,z));

            if(blockState.getBlock() != Blocks.LAVA && blockState.getBlock() != Blocks.WATER)
                return LuaValue.valueOf(false);

            if(((Integer)blockState.getValue(BlockLiquid.LEVEL)).intValue() != 0)
                return LuaValue.valueOf(false);

            return LuaValue.valueOf(true);
        }
    }
}
