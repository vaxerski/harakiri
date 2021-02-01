package me.vaxry.harakiri.impl.module.movement;

import com.yworks.yguard.test.A;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.player.EventMove;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.MathUtil;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.util.math.BlockPos;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

public class AvoidModule extends Module {

    public final Value<Boolean> unloaded = new Value<Boolean>("Unloaded", new String[]{"Unloaded", "chunks"}, "Unloaded chunks.", false);

    public AvoidModule() {
        super("Avoid", new String[]{"Avoid"}, "Avoids certain things.", "NONE", -1, ModuleType.MOVEMENT);
    }

    private ArrayList<Coordinate> loadedChunks = new ArrayList<Coordinate>();

    @Listener
    public void move(EventMove event) {
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

        // ezd
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChunkData) {
                SPacketChunkData packet = (SPacketChunkData)event.getPacket();
                loadedChunks.add(new Coordinate(packet.getChunkX(), packet.getChunkZ()));
            }
            if(event.getPacket() instanceof SPacketUnloadChunk){
                SPacketUnloadChunk packet = (SPacketUnloadChunk)event.getPacket();
                removeChunkByXZ(packet.getX(), packet.getZ());
            }
        }
    }

    private void removeChunkByXZ(int x, int z){
        for(int i = 0; i < loadedChunks.size(); ++i){
            Coordinate c = loadedChunks.get(i);
            if(c.x == x && c.y == z){
                loadedChunks.remove(i);
                break;
            }
        }
    }

    private boolean isBlockUnloaded(BlockPos bloc){
        for(Coordinate chunk : loadedChunks){
            if(bloc.getX() >= chunk.getX() + 16 && bloc.getX() <= chunk.getX() + 16
                && bloc.getZ() >= chunk.getZ() + 16 && bloc.getZ() <= chunk.getZ() + 16)
                return false;
        }
        return true;
    }



}
