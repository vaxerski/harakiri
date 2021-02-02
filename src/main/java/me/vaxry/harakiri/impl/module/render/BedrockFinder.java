package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.player.EventDestroyBlock;
import me.vaxry.harakiri.api.event.render.EventRender3D;
import me.vaxry.harakiri.api.event.render.EventRenderBlockModel;
import me.vaxry.harakiri.api.event.world.EventLoadWorld;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.notification.Notification;
import me.vaxry.harakiri.api.util.Timer;
import me.vaxry.harakiri.api.value.Value;
import me.vaxry.harakiri.api.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

public final class BedrockFinder extends Module {
    public final Value<Integer> radius = new Value<Integer>("Radius", new String[]{"Radius", "Range", "Distance"}, "Radius in blocks to scan for.", 32, 0, 64, 1);
    public final Value<Integer> opacity = new Value<Integer>("Opacity", new String[]{"Opacity", "Transparency", "Alpha"}, "Opacity of the rendered esp.", 128, 0, 255, 1);

    private ICamera camera = new Frustum();

    private ArrayList<BlockPos> illegalBedrock;
    private int foundBedrockLastS = 0;

    private Timer timer = new Timer();

    public BedrockFinder() {
        super("Bedrock Finder", new String[]{"BedrockFinder", "BF", "Bedrock"}, "Searches for unnatural bedrock.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void render3D(EventRender3D event) {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
        if(seconds > 1){
            timer.reset();

            if(foundBedrockLastS != 0)
                Harakiri.INSTANCE.getNotificationManager().addNotification(new Notification("BedrockFinder", "Found " + foundBedrockLastS + " illegal bedrock."));
            foundBedrockLastS = 0;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        for(BlockPos blockPos : illegalBedrock) {

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
            BlockPos searchBlock = this.illegalBedrock.get(i);
            if (searchBlock.getX() == x.getX() && searchBlock.getY() == x.getY() && searchBlock.getZ() == x.getZ())
                this.illegalBedrock.remove(i);
        }
    }

    @Listener
    public void onRenderBlock(EventRenderBlockModel event) {
        if(!this.isEnabled())
            return;
        final BlockPos pos = event.getBlockPos();
        final IBlockState blockState = event.getBlockState();
        if(blockState.getBlock() == Blocks.BEDROCK){
            if(Minecraft.getMinecraft().player.dimension == 0){
                // Overworld, no bedrock > 7
                if(pos.getY() > 7){
                    illegalBedrock.add(pos);
                    foundBedrockLastS += 1;
                }
            }
        }
    }

}
