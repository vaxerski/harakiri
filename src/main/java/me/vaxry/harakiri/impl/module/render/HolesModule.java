package me.vaxry.harakiri.impl.module.render;

import akka.japi.Pair;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glLineWidth;

public final class HolesModule extends Module {

    public enum MODES{
        BOX, PLANE
    }

    public final Value<MODES> mode = new Value<MODES>("Mode", new String[]{"Mode", "m"}, "Mode to draw.", MODES.BOX);
    public final Value<Integer> radius = new Value<Integer>("Radius", new String[]{"Radius", "Range", "Distance"}, "Radius in blocks to scan for holes.", 8, 0, 32, 1);
    public final Value<Boolean> fade = new Value<Boolean>("FadeIn", new String[]{"f"}, "Fades the opacity of the hole if close.", true);

    public final List<Pair<Hole, Integer>> holes = new ArrayList<>();

    private ICamera camera = new Frustum();

    public HolesModule() {
        super("HoleESP", new String[]{"Hole", "HoleESP"}, "Highlights Holes.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        forceHoleRecalc();
    }

    @Listener
    public void onRender(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderViewEntity() == null)
            return;

        RenderUtil.begin3D();
        for (Pair<Hole, Integer> h : this.holes) {
            Hole hole = h.first();

            int color = h.second();

            float red = (float) (color >> 16 & 255) / 255.0F;
            float green = (float) (color >> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;
            float alphaA = (float) (color >> 24 & 255) / 255.0F;

            if(alphaA < 1)
                continue;

            final AxisAlignedBB bb = new AxisAlignedBB(
                    hole.getX() - mc.getRenderManager().viewerPosX,
                    hole.getY() + 1 - mc.getRenderManager().viewerPosY,
                    hole.getZ() - mc.getRenderManager().viewerPosZ,
                    hole.getX() + 1 - mc.getRenderManager().viewerPosX,
                    hole.getY() + 2 - mc.getRenderManager().viewerPosY,
                    hole.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                glLineWidth(1.5f);
                final double dist = mc.player.getDistance(hole.getX() + 0.5f, hole.getY() + 0.5f, hole.getZ() + 0.5f) * 0.75f;
                float alpha = MathUtil.clamp((float) (dist * 255.0f / (this.radius.getValue()) / 255.0f), 0.0f, 0.3f);

                if(this.mode.getValue() == MODES.BOX) {
                    RenderGlobal.renderFilledBox(bb, red, green, blue, this.fade.getValue() ? alpha : 0.25f);
                    RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, red, green, blue, this.fade.getValue() ? alpha : 0.25f);
                } else {
                    final AxisAlignedBB bb2 = new AxisAlignedBB(
                            hole.getX() - mc.getRenderManager().viewerPosX,
                            hole.getY() + 1 - mc.getRenderManager().viewerPosY,
                            hole.getZ() - mc.getRenderManager().viewerPosZ,
                            hole.getX() + 1 - mc.getRenderManager().viewerPosX,
                            hole.getY() + 1 - mc.getRenderManager().viewerPosY,
                            hole.getZ() + 1 - mc.getRenderManager().viewerPosZ);
                    RenderGlobal.renderFilledBox(bb2, red, green, blue, this.fade.getValue() ? alpha : 0.25f);
                    RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY, bb.maxZ, red, green, blue, this.fade.getValue() ? alpha : 0.25f);
                }
                GlStateManager.popMatrix();
            }
        }
        RenderUtil.end3D();
    }

    private boolean isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (this.holes.contains(blockPos))
            return false;

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (Minecraft.getMinecraft().player.getDistanceSq(blockPos) < 1)
            return false;

        if (Minecraft.getMinecraft().world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (Minecraft.getMinecraft().world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) // ensure the area is tall enough for the player
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]{
                blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()
        };

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = Minecraft.getMinecraft().world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock()) {
                validHorizontalBlocks++;
            }
        }

        if (validHorizontalBlocks < 4)
            return false;

        return true;
    }

    private int getHoleColor(BlockPos bp){
        final BlockPos[] touchingBlocks = new BlockPos[]{
                bp.north(), bp.south(), bp.east(), bp.west(), bp.down()
        };

        boolean isFullBedrock = true;
        boolean isFullObby = true;
        boolean isValid = true;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = Minecraft.getMinecraft().world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.BEDROCK) {
                isFullObby = false;
            }
            if (touchingState.getBlock() == Blocks.OBSIDIAN) {
                isFullBedrock = false;
            }

            if (touchingState.getBlock() != Blocks.OBSIDIAN && touchingState.getBlock() != Blocks.BEDROCK) {
                isValid = false;
                isFullBedrock = false;
                isFullObby = false;
                break;
            }
        }

        if(isFullBedrock)
            return 0xFF00FF00;
        if(isFullObby)
            return 0xFFFF1111;
        if(!isValid)
            return 0x00000000;
        return 0xFFFFFF00; // Yellow for partial bedrock
    }

    public void forceHoleRecalc(){
        final Minecraft mc = Minecraft.getMinecraft();

        this.holes.clear();

        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);

        for (int x = playerPos.getX() - radius.getValue(); x < playerPos.getX() + radius.getValue(); x++) {
            for (int z = playerPos.getZ() - radius.getValue(); z < playerPos.getZ() + radius.getValue(); z++) {
                for (int y = playerPos.getY(); y > playerPos.getY() - 4; y--) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final IBlockState blockState = mc.world.getBlockState(blockPos);
                    if (this.isBlockValid(blockState, blockPos)) {
                        final IBlockState downBlockState = mc.world.getBlockState(blockPos.down());
                        if (downBlockState.getBlock() != Blocks.AIR) {
                            final BlockPos downPos = blockPos.down();
                            int color = getHoleColor(blockPos);
                            Hole holle = new Hole(downPos.getX(), downPos.getY(), downPos.getZ(), false);
                            this.holes.add(new Pair<Hole, Integer>(holle, color));
                        }
                    }
                }
            }
        }
    }

    public class Hole extends Vec3i {

        private boolean tall;

        Hole(int x, int y, int z) {
            super(x, y, z);
        }

        Hole(int x, int y, int z, boolean tall) {
            super(x, y, z);
            this.tall = true;
        }

        public boolean isTall() {
            return tall;
        }

        public void setTall(boolean tall) {
            this.tall = tall;
        }
    }
}