package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class AvoidModule extends Module {

    public final Value<Boolean> unloaded = new Value<Boolean>("Unloaded", new String[]{"Unloaded", "chunks"}, "Unloaded chunks.", false);

    public AvoidModule() {
        super("Avoid", new String[]{"Avoid"}, "Avoids certain things.", "NONE", -1, ModuleType.MOVEMENT);
    }


    @Listener
    public void move(EventMove event) {
        if(!Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled())
            fixMovevemt(event, Minecraft.getMinecraft().player);

        // ezd
    }

    public void fixMovevemt(EventMove event, EntityPlayerSP player){
        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are colliding on 4 axes.

        BlockPos[] surround = new BlockPos[4];

        // +X
        surround[0] = new BlockPos(player.getPosition().getX() + 1, player.getPosition().getY(), player.getPosition().getZ());
        // -X
        surround[1] = new BlockPos(player.getPosition().getX() - 1, player.getPosition().getY(), player.getPosition().getZ());
        // +Z
        surround[2] = new BlockPos(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ() + 1);
        // -Z
        surround[3] = new BlockPos(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ() - 1);

        // Check all
        if(isBlockUnloaded(surround[0])){
            if(event.getX() > 0)
                event.setX(0);
        }
        if(isBlockUnloaded(surround[1])){
            if(event.getX() < 0)
                event.setX(0);
        }
        if(isBlockUnloaded(surround[2])){
            if(event.getZ() > 0)
                event.setZ(0);
        }
        if(isBlockUnloaded(surround[3])){
            if(event.getZ() < 0)
                event.setZ(0);
        }
    }

    private boolean isBlockUnloaded(BlockPos bloc){
        return !(Minecraft.getMinecraft().world.isBlockLoaded(bloc, false));
    }



}
