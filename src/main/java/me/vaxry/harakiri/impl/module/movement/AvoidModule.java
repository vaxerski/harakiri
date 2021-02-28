package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class AvoidModule extends Module {

    public final Value<Boolean> unloaded = new Value<Boolean>("Unloaded", new String[]{"Unloaded", "chunks"}, "Unloaded chunks.", false);

    public AvoidModule() {
        super("Avoid", new String[]{"Avoid"}, "Avoids certain things.", "NONE", -1, ModuleType.MOVEMENT);
    }


    @Listener
    public void move(EventMove event) {
        fixMovevemt(event);

        // ezd
    }

    public void fixMovevemt(EventMove event){
        Minecraft mc = Minecraft.getMinecraft();

        // Check if we are colliding on 4 axes.

        BlockPos[] surround = new BlockPos[4];

        // +X
        surround[0] = new BlockPos(mc.player.getPosition().getX() + 1, mc.player.getPosition().getY(), mc.player.getPosition().getZ());
        // -X
        surround[1] = new BlockPos(mc.player.getPosition().getX() - 1, mc.player.getPosition().getY(), mc.player.getPosition().getZ());
        // +Z
        surround[2] = new BlockPos(mc.player.getPosition().getX(), mc.player.getPosition().getY(), mc.player.getPosition().getZ() + 1);
        // -Z
        surround[3] = new BlockPos(mc.player.getPosition().getX(), mc.player.getPosition().getY(), mc.player.getPosition().getZ() - 1);

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
