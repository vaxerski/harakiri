package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventDestroyBlock;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.render.EventRenderBlockModel;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Notification;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

public final class BedrockFinder extends Module {

    private ICamera camera = new Frustum();

    private ArrayList<Coordinate> illegalBedrock = new ArrayList<>();
    private int foundBedrockLastS = 0;

    private Timer timer = new Timer();

    public BedrockFinder() {
        super("BedrockFinder", new String[]{"BedrockFinder", "BF", "Bedrock"}, "Searches for unnatural bedrock.", "NONE", -1, ModuleType.RENDER);
        timer.reset();
    }

    @Listener
    public void onRender(EventRender3D event) {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
        if(seconds > 1){
            timer.reset();

            if(foundBedrockLastS != 0)
                Harakiri.get().getNotificationManager().addNotification(new Notification("BedrockFinder", "Found " + foundBedrockLastS + " illegal bedrock."));
            foundBedrockLastS = 0;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        RenderUtil.begin3D();

        ArrayList<BlockPos> dirty = new ArrayList<>();

        for(Coordinate blockPos : illegalBedrock) {

            if(!mc.world.isBlockLoaded(new BlockPos(blockPos.x, blockPos.y, blockPos.z), false))
                dirty.add(new BlockPos(blockPos.x, blockPos.y, blockPos.z));


            final AxisAlignedBB bb = new AxisAlignedBB(
                    blockPos.getX() - mc.getRenderManager().viewerPosX,
                    blockPos.getY() - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                    blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {

                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, 0.8f, 0.1f, 0.1f, 1.0f);
            }
        }

        for(BlockPos d : dirty)
            removeBlock(d);

        RenderUtil.end3D();
    }

    @Override
    public void onToggle() {
        super.onToggle();
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
        this.illegalBedrock.clear();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            this.illegalBedrock.clear();
        }
    }

    @Listener
    public void onDestroyBlock(EventDestroyBlock event) {
        if (event.getPos() != null) {
            this.removeBlock(event.getPos());
        }
    }

    private void removeBlock(BlockPos x) {
        for (int i = this.illegalBedrock.size() - 1; i >= 0; i--) {
            Coordinate searchBlock = this.illegalBedrock.get(i);
            if (searchBlock.getX() == x.getX() && searchBlock.getY() == x.getY() && searchBlock.getZ() == x.getZ()) {
                this.illegalBedrock.remove(i);
                break;
            }
        }
    }

    private boolean isBlockAdded(BlockPos x) {
        for (int i = this.illegalBedrock.size() - 1; i >= 0; i--) {
            Coordinate searchBlock = this.illegalBedrock.get(i);
            if (searchBlock.getX() == x.getX() && searchBlock.getY() == x.getY() && searchBlock.getZ() == x.getZ()) {
                return true;
            }
        }
        return false;
    }

    @Listener
    public void onRenderBlock(EventRenderBlockModel event) {
        final BlockPos pos = event.getBlockPos();
        final IBlockState blockState = event.getBlockState();
        if(blockState.getBlock() == Blocks.BEDROCK){
            if(Minecraft.getMinecraft().player.dimension == 0){
                // Overworld, no bedrock > 5
                if(pos.getY() > 5){
                    if(!isBlockAdded(pos)) {
                        foundBedrockLastS += 1;
                        illegalBedrock.add(new Coordinate(pos.getX(), pos.getY(), pos.getZ()));
                    }
                }
            }
        }
    }

}
