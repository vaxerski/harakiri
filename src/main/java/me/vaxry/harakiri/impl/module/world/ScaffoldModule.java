package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class ScaffoldModule extends Module {

    enum MODE {
        CLASSIC, EXTRAPOLATE
    }

    public final Value<MODE> mode = new Value<MODE>("Mode", new String[]{"Mode", "m"}, "Mode to use.", MODE.CLASSIC);
    public final Value<Integer> reach = new Value<Integer>("Reach", new String[]{"Reach", "range", "r"}, "The reach in blocks from the player to end bridging.", 4, 1, 10, 1);
    public final Value<Boolean> stopmotion = new Value<Boolean>("StopMotion", new String[]{"StopMotion", "sm"}, "Don't move when not placed.", false);
    public final Value<Boolean> blockfly = new Value<Boolean>("BlockFly", new String[]{"BlockFly", "bf"}, "Rapid towering.", false);
    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Delay", "d"}, "Delay to place.", 0f, 0f, 1f, 0.1f);

    public ScaffoldModule() {
        super("Scaffold", new String[]{"Scaffold", "AutoBridge"}, "Builds a bridge under you with your held item.", "NONE", -1, ModuleType.WORLD);
    }

    private Timer _timer = new Timer();
    private Timer _towerPauseTimer = new Timer();
    private Timer _towerTimer = new Timer();

    private boolean lastFeet = false;
    private double towerStart = 0.0;

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.POST) || mode.getValue() == MODE.CLASSIC)
            return;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled())
            return;

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock) mc.player.getHeldItemMainhand().getItem();
            if (BlockInteractionUtil.shouldPlace(itemBlock.getBlock())) {
                EnumFacing facing = mc.player.getHorizontalFacing();
                Vec3i position = getNextBlock(facing.getDirectionVec(), mc);
                if (position != null) {
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(new BlockPos(position.getX(), position.getY(), position.getZ()), facing, EnumHand.MAIN_HAND, (float) facing.getDirectionVec().getX() / 2.0F, (float) facing.getDirectionVec().getY() / 2.0F, (float) facing.getDirectionVec().getZ() / 2.0F));
                }
            }
        }
    }

    @Listener
    public void onMotionUpdate(EventUpdateWalkingPlayer event){
        if(mode.getValue() == MODE.EXTRAPOLATE)
            return;

        if (event.isCanceled())
            return;

        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (!_timer.passed(delay.getValue() * 1000))
            return;

        Minecraft mc = Minecraft.getMinecraft();

        // verify we have a block in our hand
        ItemStack stack = mc.player.getHeldItemMainhand();

        int prevSlot = -1;

        if (!BlockInteractionUtil.verifyStack(stack))
        {
            for (int i = 0; i < 9; ++i)
            {
                stack = mc.player.inventory.getStackInSlot(i);

                if (BlockInteractionUtil.verifyStack(stack))
                {
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }
        }

        if (!BlockInteractionUtil.verifyStack(stack))
            return;

        _timer.reset();

        BlockPos toPlaceAt = null;

        if (lastFeet && this.blockfly.getValue() && mc.gameSettings.keyBindJump.isKeyDown())
        {
            final double motion = 0.42d; // jump motion
            if (mc.player.onGround)
            {
                towerStart = mc.player.posY;
                mc.player.motionY = motion;
            }

            if (mc.player.posY > towerStart + motion)
            {
                mc.player.setPosition(mc.player.posX, (int) mc.player.posY, mc.player.posZ);
                mc.player.motionY = motion;
                towerStart = mc.player.posY;
            }
        }
        else
        {
            towerStart = 0.0;
        }

        BlockPos feetBlock = BlockInteractionUtil.GetLocalPlayerPosFloored().down();

        boolean placeAtFeet = BlockInteractionUtil.isValidPlaceBlockState(feetBlock);

        if (placeAtFeet) {
            toPlaceAt = feetBlock;
            lastFeet = true;
        } else // find a supporting position for feet block
        {
            lastFeet = false;
            BlockInteractionUtil.ValidResult result = BlockInteractionUtil.valid(feetBlock);

            // find a supporting block
            if (result != BlockInteractionUtil.ValidResult.Ok && result != BlockInteractionUtil.ValidResult.AlreadyBlockThere)
            {
                BlockPos[] array = { feetBlock.north(), feetBlock.south(), feetBlock.east(), feetBlock.west() };

                BlockPos toSelect = null;
                double lastDistance = 420.0;

                for (BlockPos pos : array)
                {
                    if (!BlockInteractionUtil.isValidPlaceBlockState(pos))
                        continue;

                    double dist = pos.getDistance((int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
                    if (lastDistance > dist)
                    {
                        lastDistance = dist;
                        toSelect = pos;
                    }
                }

                // if we found a position, that's our selection
                if (toSelect != null)
                    toPlaceAt = toSelect;
            }

        }

        if (toPlaceAt != null)
        {
            // PositionRotation
            // CPacketPlayerTryUseItemOnBlock
            // CPacketAnimation

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);

            for (final EnumFacing side : EnumFacing.values())
            {
                final BlockPos neighbor = toPlaceAt.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
                {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f)
                    {
                        float[] rotations = BlockInteractionUtil.getFacingRotations(toPlaceAt.getX(), toPlaceAt.getY(), toPlaceAt.getZ(), side);

                        event.setCanceled(true);
                        BlockInteractionUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }

            if (BlockInteractionUtil.place(toPlaceAt, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.Placed)
            {
                // swinging is already in the place function.
            }
        }
        else
            _towerPauseTimer.reset();

        // set back our previous slot
        if (prevSlot != -1)
        {
            mc.player.inventory.currentItem = prevSlot;
            mc.playerController.updateController();
        }
    }

    private Vec3i getNextBlock(Vec3i direction, final Minecraft mc) {
        for (int i = 0; i <= (int) reach.getValue(); i++) {
            Vec3i position = new Vec3i(mc.player.posX + direction.getX() * i, mc.player.posY - 1, mc.player.posZ + direction.getZ() * i);
            Vec3i before = new Vec3i(mc.player.posX + direction.getX() * (i - 1), mc.player.posY - 1, mc.player.posZ + direction.getZ() * (i - 1));
            if ((mc.world.getBlockState(new BlockPos(position.getX(), position.getY(), position.getZ())).getMaterial() == Material.AIR) && (mc.world.getBlockState(new BlockPos(before.getX(), before.getY(), before.getZ())).getMaterial() != Material.AIR)) {
                return before;
            }
        }
        return null;
    }
}

