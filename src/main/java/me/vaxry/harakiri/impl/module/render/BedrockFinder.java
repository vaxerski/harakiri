package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.api.event.render.EventRender3D;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import me.vaxry.harakiri.api.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class BedrockFinder extends Module {
    public final Value<Integer> radius = new Value<Integer>("Radius", new String[]{"Radius", "Range", "Distance"}, "Radius in blocks to scan for.", 32, 0, 64, 1);
    public final Value<Integer> opacity = new Value<Integer>("Opacity", new String[]{"Opacity", "Transparency", "Alpha"}, "Opacity of the rendered esp.", 128, 0, 255, 1);

    private ICamera camera = new Frustum();

    public BedrockFinder() {
        super("Bedrock Finder", new String[]{"BedrockFinder", "BF", "Bedrock"}, "Searches for unnatural bedrock.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void render3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = playerPos.getX() - radius.getValue(); x < playerPos.getX() + radius.getValue(); x++) {
            for (int z = playerPos.getZ() - radius.getValue(); z < playerPos.getZ() + radius.getValue(); z++) {
                for (int y = playerPos.getY() - radius.getValue(); y < playerPos.getY() + radius.getValue(); y++) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final IBlockState blockState = mc.world.getBlockState(blockPos);
                    if (blockState.getBlock() == Blocks.BEDROCK && y > 7 && y < 125) {

                        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

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


                            //RenderUtil.drawBoundingBox(bb, 0.5f, 255,0,0,255);
                        }
                    }
                }
            }
        }
    }

}
