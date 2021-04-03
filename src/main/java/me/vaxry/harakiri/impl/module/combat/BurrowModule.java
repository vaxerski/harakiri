package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventApplyCollision;
import me.vaxry.harakiri.framework.event.player.EventPushOutOfBlocks;
import me.vaxry.harakiri.framework.event.player.EventPushedByWater;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.world.EventAddCollisionBox;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class BurrowModule extends Module {
    private int prevSlot = -1;

    public final Value<Float> rubberbandPerc = new Value<Float>("RubberbandHeight", new String[]{"RubberbandHeight", "RP"}, "Rubberband height.", 20F, -20F, 20F, 1F);
    public final Value<Float> jumpHeight = new Value<Float>("JumpHeight", new String[]{"JumpHeight", "JH"}, "How high to jump before rubberbanding (perc).", 80F, 70F, 100F, 1F);
    //public final Value<Boolean> packet = new Value<Boolean>("Packet", new String[]{"Packet", "Pck"}, "Use packet mode.", false);
    public final Value<Boolean> posVel = new Value<Boolean>("Position", new String[]{"Position", "Pos"}, "Use position instead of velocity.", false);


    public BurrowModule() {
        super("Burrow", new String[]{"Burrow", "Burr"}, "Automatically places an obsidian block at your feet.", "NONE", -1, ModuleType.COMBAT);
    }

    private boolean jumped = false;
    private boolean gotHeight = false;

    private BlockPos toPlace;

    @Override
    public void onEnable() {
        super.onEnable();
        this.jumped = false;
        this.gotHeight = false;
    }

    @Listener
    public void onMotionUpdate(EventUpdateWalkingPlayer event){

        if (event.isCanceled())
            return;

        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        if(!jumped) {
            toPlace = BlockInteractionUtil.GetLocalPlayerPosFloored();

            if (mc.world.getBlockState(toPlace).getBlock() != Blocks.AIR) {
                ceaseAndDesist();
                Harakiri.get().getNotificationManager().addNotification("Burrow", "Burrow failed to place the block (Feet not air)!");
                return;
            }

            ItemStack stack = null;

            for (int i = 0; i < 9; ++i) {

                if (isObsidian(mc.player.inventory.getStackInSlot(i))) {
                    stack = mc.player.inventory.getStackInSlot(i);
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }

            if (stack == null) {
                Harakiri.get().getNotificationManager().addNotification("Burrow", "Burrow couldn't find any obsidian in your hotbar!");
                ceaseAndDesist();
                return;
            }

            /*if(this.packet.getValue()){
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.4F, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.75F, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1F, mc.player.posZ, true));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.15F, mc.player.posZ, true));

                if(BlockInteractionUtil.place(toPlace, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.CantPlace)
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                if (prevSlot != -1) {
                    mc.player.inventory.currentItem = prevSlot;
                    mc.playerController.updateController();
                }

                ceaseAndDesist();
                return;
            }*/

            // Jump
            mc.player.jump();
            this.jumped = true;
        }
        else {
            if (mc.player.posY > toPlace.getY() + (this.jumpHeight.getValue() / 100F)) {
                // We can place

                gotHeight = true;

                final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);

                for (final EnumFacing side : EnumFacing.values()) {
                    final BlockPos neighbor = toPlace.offset(side);
                    final EnumFacing side2 = side.getOpposite();

                    if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false)) {
                        final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                        if (eyesPos.distanceTo(hitVec) <= 5.0f) {
                            float[] rotations = BlockInteractionUtil.getFacingRotations(toPlace.getX(), toPlace.getY(), toPlace.getZ(), side);

                            event.setCanceled(true);
                            BlockInteractionUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                            break;
                        }
                    }
                }

                if(BlockInteractionUtil.place(toPlace, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.CantPlace)
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                // Lag back

                if(this.posVel.getValue())
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + this.rubberbandPerc.getValue(), mc.player.posZ, true));
                else
                    mc.player.setVelocity(0, mc.player.motionY + this.rubberbandPerc.getValue(), 0);

                mc.player.setPosition(mc.player.posX, toPlace.getY(), mc.player.posZ);

                // Restore slot
                if (prevSlot != -1) {
                    mc.player.inventory.currentItem = prevSlot;
                    mc.playerController.updateController();
                }

                ceaseAndDesist();
            }else if(gotHeight){
                ceaseAndDesist();
                Harakiri.get().getNotificationManager().addNotification("Burrow", "Burrow couldn't place the block!");
            }
        }
    }

    @Listener
    public void collideWithBlock(EventAddCollisionBox event) {
        if(this.jumped)
            event.setCanceled(true);
    }

    @Listener
    public void pushOutOfBlocks(EventPushOutOfBlocks event) {
        if(this.jumped)
            event.setCanceled(true);
    }

    @Listener
    public void pushedByWater(EventPushedByWater event) {
        if(this.jumped)
            event.setCanceled(true);
    }

    @Listener
    public void applyCollision(EventApplyCollision event) {
        if(this.jumped)
            event.setCanceled(true);
    }

    private boolean isObsidian(ItemStack stack){
        return stack.getItem() == Item.getItemFromBlock(Blocks.OBSIDIAN);
    }

    private void ceaseAndDesist(){
        this.setEnabled(false);
        this.onDisable();
    }
}
