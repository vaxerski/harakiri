package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import me.vaxry.harakiri.framework.util.BlockUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.module.player.AutoEatModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import me.vaxry.harakiri.framework.Value;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class FillLavaModule extends Module {

    public boolean isWorking = false;

    public String fillStatus = "idle";
    private int filledblocks = 0;

    private List<BlockPos> toConfirm = new LinkedList<BlockPos>();

    public FillLavaModule() {
        super("FillLava", new String[] { "FillLava" }, "Fills lava pockets.", "NONE", -1, Module.ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        filledblocks = 0;
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        // first get a list of blocks we need to fill
        isWorking = false;

        ArrayList<BlockPos> blocksToFill = new ArrayList<BlockPos>();
        final Minecraft mc = Minecraft.getMinecraft();

        fillStatus = "filled " + ChatFormatting.GREEN + Integer.toString(filledblocks);

        // eat compat
        final AutoEatModule aem = ((AutoEatModule) Harakiri.get().getModuleManager().find(AutoEatModule.class));
        if (mc.player.getFoodStats().getFoodLevel() < aem.hunger.getValue() || mc.player.getHealth() < aem.health.getValue()) {
            isWorking = false;
            return;
        }

        // Check filled blocks
        if (!toConfirm.isEmpty()) {
            isWorking = true;
            fillStatus = "confirming, " + Integer.toString(toConfirm.size()) + " left.";

            List<BlockPos> toRemove = new LinkedList<>();
            for (BlockPos p : toConfirm) {
                Block b = BlockUtil.getBlock(p);
                if (b != Blocks.LAVA) {
                    toRemove.add(p);
                    filledblocks++;
                }
            }

            for (BlockPos r : toRemove)
                toConfirm.remove(r);
        }


        for (double y = Math.round(mc.player.posY - 1) + 4; y > Math.round(mc.player.posY - 2); y -= 1.0D) {
            for (double x = mc.player.posX - 4; x < mc.player.posX + 4; x += 1.0D) {
                for (double z = mc.player.posZ - 4; z < mc.player.posZ + 4; z += 1.0D) {
                    final BlockPos blockPos = new BlockPos(x, y, z);
                    final Block block = BlockUtil.getBlock(blockPos);

                    if ((block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) && blockPos != new BlockPos(mc.player.getPositionVector()))
                        blocksToFill.add(blockPos);
                }
            }
        }

        // now for all blocks check

        if (blocksToFill.isEmpty()) {
            if (toConfirm.isEmpty())
                isWorking = false;
            return;
        }

        isWorking = true;
        fillStatus = "filling, " + Integer.toString(blocksToFill.size()) + " left.";

        ArrayList<BlockPos> validBlocksToFill = new ArrayList<BlockPos>();

        for (BlockPos b : blocksToFill) {
            if (BlockInteractionUtil.isValidPlaceBlockState(b)) {
                validBlocksToFill.add(b);
            }
        }

        // Check stacks
        ItemStack stack = mc.player.getHeldItemMainhand();

        int prevSlot = -1;

        if (!BlockInteractionUtil.verifyStack(stack)) {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);

                if (BlockInteractionUtil.verifyStack(stack)) {
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }

        if (!BlockInteractionUtil.verifyStack(stack)){
            Harakiri.get().logChat("Can't fill! No blocks in hotbar!");
            isWorking = false;
            return;
        }

        // and fill
        if (!validBlocksToFill.isEmpty()) {
            BlockPos blockToFill = validBlocksToFill.get(0);

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);

            for (final EnumFacing side : EnumFacing.values()) {
                final BlockPos neighbor = blockToFill.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor),
                        false)) {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5)
                            .add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f) {
                        float[] rotations = BlockInteractionUtil.getFacingRotations(blockToFill.getX(), blockToFill.getY(), blockToFill.getZ(), side);

                        event.setCanceled(true);
                        BlockInteractionUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }

            if (BlockInteractionUtil.place(blockToFill, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.Placed) {
                // swinging is already in the place function.
                toConfirm.add(blockToFill);
            }

        } else {
            isWorking = false;
        }
    }

}
