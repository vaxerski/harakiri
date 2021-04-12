package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.player.EventDestroyBlock;
import me.vaxry.harakiri.framework.event.player.EventRightClickBlock;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.world.EventSetBlockState;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.util.BlockUtil;
import me.vaxry.harakiri.framework.util.EntityUtil;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.misc.NoGlitchBlocks;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NukerModule extends Module {

    float r = 0xFE;
    float g = 0;
    float b = 0;
    int stage = 1;

    final float rainSpeed = 5;

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The nuker mode to use.", Mode.SELECTION);

    public enum Mode {
        SELECTION, ALL, CREATIVE
    }

    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Dist", "D"}, "Maximum distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> fixed = new Value<Boolean>("FixedDist", new String[]{"Fixed", "fdist", "F"}, "Use vertical and horizontal distances in blocks instead of distances relative to the camera.", false);
    public final Value<Float> vDistance = new Value<Float>("Vertical", new String[]{"Vertical", "vdist", "VD"}, "Maximum vertical distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> hDistance = new Value<Float>("Horizontal", new String[]{"Horizontal", "hist", "HD"}, "Maximum horizontal distance in blocks the nuker will reach.", 3f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> flatten = new Value<Boolean>("Flatten", new String[]{"Flatten", "flat", "flt"}, "Flatten the region", false);
    public final Value<Boolean> drawMining = new Value<Boolean>("DrawMining", new String[]{"drawmining", "dm", "drawm"}, "Draw mined blocks' outlines", false);


    private final RotationTask rotationTask = new RotationTask("NukerTask", 2);

    private Block selected = null;
    private BlockPos currentPos = null;

    public NukerModule() {
        super("Nuker", new String[]{"Nuke"}, "Automatically mines blocks within reach.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.selected = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled())
            return;

        switch (event.getStage()) {
            case PRE:
                this.currentPos = null;

                switch (this.mode.getValue()) {
                    case SELECTION:
                        this.currentPos = this.getClosestBlock(true);
                        break;
                    case ALL:
                        this.currentPos = this.getClosestBlock(false);
                        break;
                }


                if (this.currentPos != null) {
                    Harakiri.get().getRotationManager().startTask(this.rotationTask);
                    if (this.rotationTask.isOnline()) {
                        final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.currentPos.getX() + 0.5f, this.currentPos.getY() + 0.5f, this.currentPos.getZ() + 0.5f));
                        Harakiri.get().getRotationManager().setPlayerRotations(angle[0], angle[1]);
                    }
                }
                break;
            case POST:
                if (this.mode.getValue().equals(Mode.CREATIVE)) {
                    /* the amazing creative 'nuker' straight from the latch hacked client */
                    for (double y = Math.round(mc.player.posY - 1) + this.vDistance.getValue(); y > Math.round(mc.player.posY - 1); y -= 1.0D) {
                        for (double x = mc.player.posX - this.hDistance.getValue(); x < mc.player.posX + this.hDistance.getValue(); x += 1.0D) {
                            for (double z = mc.player.posZ - this.hDistance.getValue(); z < mc.player.posZ + this.hDistance.getValue(); z += 1.0D) {
                                final BlockPos blockPos = new BlockPos(x, y, z);
                                final Block block = BlockUtil.getBlock(blockPos);
                                if (block == Blocks.AIR || !mc.world.getBlockState(blockPos).isFullBlock() || block == Blocks.BEDROCK)
                                    continue;

                                if(this.flatten.getValue() && blockPos.getY() < mc.player.posY)
                                    continue;

                                final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
                                final Vec3d posVec = new Vec3d(blockPos).add(0.5f, 0.5f, 0.5f);
                                double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);

                                for (EnumFacing side : EnumFacing.values()) {
                                    final Vec3d hitVec = posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5f));
                                    double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);

                                    // check if hitVec is within range (6 blocks)
                                    if (distanceSqHitVec > 36)
                                        continue;

                                    // check if side is facing towards player
                                    //if (distanceSqHitVec >= distanceSqPosVec)
                                        //continue;

                                    // face block
                                    final float[] rotations = EntityUtil.getRotations(hitVec.x, hitVec.y, hitVec.z);
                                    Harakiri.get().getRotationManager().setPlayerRotations(rotations[0], rotations[1]);

                                    // damage block
                                    if (mc.playerController.onPlayerDamageBlock(blockPos, side)) {
                                        mc.player.swingArm(EnumHand.MAIN_HAND);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (this.currentPos != null) {
                        if (this.rotationTask.isOnline()) {
                            if (this.canBreak(this.currentPos)) {
                                mc.playerController.onPlayerDamageBlock(this.currentPos, mc.player.getHorizontalFacing());
                                mc.player.swingArm(EnumHand.MAIN_HAND);
                            }
                        }
                    } else {
                        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
                    }
                }
                break;
        }
    }

    private ICamera camera = new Frustum();

    @Listener
    public void render3D(EventRender3D event) {

        if (!this.drawMining.getValue() || Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled()) return;

        switch (stage) {
            case 0:
                r += 0.5 * rainSpeed;
                b -= 0.5 * rainSpeed;
                if (r >= 0xFE) {
                    stage++;
                    r = 0xFE;
                    b = 0;
                }
                break;
            case 1:
                g += 0.5 * rainSpeed;
                r -= 0.5 * rainSpeed;
                if (g >= 0xFE) {
                    stage++;
                    g = 0xFE;
                    r = 0;
                }
                break;
            case 2:
                b += 0.5 * rainSpeed;
                g -= 0.5 * rainSpeed;
                if (b >= 0xFE) {
                    stage = 0;
                    b = 0xFE;
                    g = 0;
                }
                break;
        }

        // Normalize RGB
        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;
        if (r > 0xFF) r = 0xFF;
        if (g > 0xFF) g = 0xFF;
        if (b > 0xFF) b = 0xFF;
        // ---------- //


        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        BlockPos pos = null;
        final Vec3i playerPos = new Vec3i(mc.player.posX, mc.player.posY, mc.player.posZ);
        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        AxisAlignedBB bb;

        switch (this.mode.getValue()) {
            case SELECTION:
                pos = this.getClosestBlock(true);
                bb = new AxisAlignedBB(
                        pos.getX() - mc.getRenderManager().viewerPosX,
                        pos.getY() - mc.getRenderManager().viewerPosY,
                        pos.getZ() - mc.getRenderManager().viewerPosZ,
                        pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                        pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

                if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    RenderUtil.drawBoundingBox(bb, 2, r, g, b, 255);
                }
                break;
            case ALL:
                pos = this.getClosestBlock(false);
                bb = new AxisAlignedBB(
                        pos.getX() - mc.getRenderManager().viewerPosX,
                        pos.getY() - mc.getRenderManager().viewerPosY,
                        pos.getZ() - mc.getRenderManager().viewerPosZ,
                        pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                        pos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

                if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    RenderUtil.drawBoundingBox(bb, 2, r, g, b, 255);
                }
                break;
            case CREATIVE:
                for (double y = Math.round(mc.player.posY - 1) + this.vDistance.getValue(); y > Math.round(mc.player.posY - 1); y -= 1.0D) {
                    for (double x = mc.player.posX - this.hDistance.getValue(); x < mc.player.posX + this.hDistance.getValue(); x += 1.0D) {
                        for (double z = mc.player.posZ - this.hDistance.getValue(); z < mc.player.posZ + this.hDistance.getValue(); z += 1.0D) {
                            final BlockPos blockPos = new BlockPos(x, y, z);
                            final Block block = BlockUtil.getBlock(blockPos);
                            if (block == Blocks.AIR || !mc.world.getBlockState(blockPos).isFullBlock() || block == Blocks.BEDROCK)
                                continue;

                            if (this.flatten.getValue() && blockPos.getY() < mc.player.posY)
                                continue;

                            bb = new AxisAlignedBB(
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
                                RenderUtil.drawBoundingBox(bb, 2, r, g, b, 255);
                            }
                        }
                    }
                }
                break;
        }
    }

    @Listener
    public void clickBlock(EventRightClickBlock event) {
        if (this.mode.getValue() == Mode.SELECTION) {
            final Block block = Minecraft.getMinecraft().world.getBlockState(event.getPos()).getBlock();
            if (block != this.selected) {
                this.selected = block;
                Harakiri.get().logChat("Nuker block set to " + block.getLocalizedName());
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void ondestroy(EventDestroyBlock event) {
        if(this.mode.getValue() == Mode.CREATIVE) // Automatically remove lag when creative nuking.
            event.setCanceled(true);
    }

    @Listener
    public void setblockstate(EventSetBlockState event) {
        NoGlitchBlocks noGlitchBlocks = (NoGlitchBlocks) Harakiri.get().getModuleManager().find(NoGlitchBlocks.class);

        if(!noGlitchBlocks.isEnabled() && this.mode.getValue() == Mode.CREATIVE) // Automatically remove lag when creative nuking.
            noGlitchBlocks.setblockstate(event);
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

    private BlockPos getClosestBlock(boolean selection) {
        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos ret = null;

        if (this.fixed.getValue()) {
            float maxVDist = this.vDistance.getValue();
            float maxHDist = this.hDistance.getValue();
            for (float x = 0; x <= maxHDist; x++) {
                for (float y = 0; y <= maxVDist; y++) {
                    for (float z = 0; z <= maxHDist; z++) {
                        for (int revX = 0; revX <= 1; revX++, x = -x) {
                            for (int revZ = 0; revZ <= 1; revZ++, z = -z) {
                                final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                                if ((mc.world.getBlockState(pos).getBlock() != Blocks.AIR &&
                                        !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) &&
                                        this.canBreak(pos)) {
                                    if (selection) {
                                        if ((this.selected == null) || !mc.world.getBlockState(pos).getBlock().equals(this.selected)) {
                                            continue;
                                        }
                                    }

                                    ret = pos;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            float maxDist = this.distance.getValue();
            for (float x = maxDist; x >= -maxDist; x--) {
                for (float y = maxDist; y >= -maxDist; y--) {
                    for (float z = maxDist; z >= -maxDist; z--) {
                        final BlockPos pos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
                        final double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                        if (dist <= maxDist && (mc.world.getBlockState(pos).getBlock() != Blocks.AIR && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) && canBreak(pos)) {
                            if (selection) {
                                if ((this.selected == null) || !mc.world.getBlockState(pos).getBlock().equals(this.selected)) {
                                    continue;
                                }
                            }

                            if (pos.getY() < mc.player.posY)
                                continue;

                            maxDist = (float) dist;
                            ret = pos;
                        }
                    }
                }
            }
        }
        return ret;
    }

}
