package me.vaxry.harakiri.framework.lua.api;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class EntityAPI extends TwoArgFunction {

    public static EntityAPI ENTITYAPI = null;

    public EntityAPI() { ENTITYAPI = this; }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable entity = new LuaTable(0,30);
        entity.set( "getLocalPlayer", new getLocalPlayer() );
        entity.set( "getLoadedPlayerList", new getLoadedPlayerList() );
        env.set( "entity", entity );
        env.get("package").get("loaded").set("entity", entity);
        return entity;
    }

    public static class LUAEntityPlayer {
        public double x = 0;
        public double y = 0;
        public double z = 0;

        public float health = 0;
        public float absorptionAmount = 0;

        public String name = "";
        public boolean friend = false;

        // Inventory
        public String[] itemSlots = new String[41];
    }

    public static LUAEntityPlayer getLuaPlayerFromEntity(EntityPlayer e){
        LUAEntityPlayer le = new LUAEntityPlayer();

        le.x = e.posX;
        le.y = e.posY;
        le.z = e.posZ;

        le.health = e.getHealth();
        le.absorptionAmount = e.getAbsorptionAmount();

        le.name = e.getName();
        le.friend = Harakiri.get().getFriendManager().isFriend(e) != null;

        // Inventory
        for(int i = 0; i < 41; ++i){
            final ItemStack stack = e.inventory.getStackInSlot(i);
            le.itemSlots[i] = stack.getItem().getItemStackDisplayName(stack);
        }

        return le;
    }

    protected static class getLocalPlayer extends ZeroArgFunction {
        public LuaValue call() {
            return CoerceJavaToLua.coerce(getLuaPlayerFromEntity(Minecraft.getMinecraft().player));
        }
    }

    protected static class getLoadedPlayerList extends ZeroArgFunction {
        public LuaValue call() {

            ArrayList<LuaValue> entities = new ArrayList<>();

            for(Entity e : Minecraft.getMinecraft().world.loadedEntityList){
                if(e instanceof EntityPlayer)
                    entities.add(CoerceJavaToLua.coerce(getLuaPlayerFromEntity((EntityPlayer)e)));
            }

            LuaValue players[] = entities.toArray(new LuaValue[entities.size()]);

            LuaTable table = new LuaTable(null, players, LuaValue.NIL);

            return table;
        }
    }
}
