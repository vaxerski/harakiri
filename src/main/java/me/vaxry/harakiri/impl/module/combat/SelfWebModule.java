package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class SelfWebModule extends Module {
    private int prevSlot = -1;

    public SelfWebModule() {
        super("SelfWeb", new String[]{"SelfWeb", "SW"}, "Automatically places a cobweb at your feet (must have cobweb in hotbar)", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onMotionUpdate(EventUpdateWalkingPlayer event){

        if (event.isCanceled())
            return;

        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        BlockPos toPlace = BlockInteractionUtil.GetLocalPlayerPosFloored();

        final Minecraft mc = Minecraft.getMinecraft();

        if(mc.world.getBlockState(toPlace).getBlock() != Blocks.AIR) {
            ceaseAndDesist();
            Harakiri.get().getNotificationManager().addNotification("SelfWeb", "SelfWeb failed to place the block (Feet not air)!");
            return;
        }

        ItemStack stack = null;

        for (int i = 0; i < 9; ++i) {

            if (isCobweb(mc.player.inventory.getStackInSlot(i)))
            {
                stack = mc.player.inventory.getStackInSlot(i);
                prevSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = i;
                mc.playerController.updateController();
                break;
            }
        }

        if(stack == null){
            Harakiri.get().getNotificationManager().addNotification("SelfWeb", "SelfWeb couldn't find any webs in your hotbar!");
            ceaseAndDesist();
            return;
        }

        if(BlockInteractionUtil.isValidWebPlaceBlockState(toPlace)){
            // We can place

            final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);

            for (final EnumFacing side : EnumFacing.values())
            {
                final BlockPos neighbor = toPlace.offset(side);
                final EnumFacing side2 = side.getOpposite();

                if (mc.world.getBlockState(neighbor).getBlock().canCollideCheck(mc.world.getBlockState(neighbor), false))
                {
                    final Vec3d hitVec = new Vec3d((Vec3i) neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
                    if (eyesPos.distanceTo(hitVec) <= 5.0f)
                    {
                        float[] rotations = BlockInteractionUtil.getFacingRotations(toPlace.getX(), toPlace.getY(), toPlace.getZ(), side);

                        event.setCanceled(true);
                        BlockInteractionUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
                        break;
                    }
                }
            }

            BlockInteractionUtil.place(toPlace, 5.0f, false, false, true);
        }else{
            Harakiri.get().getNotificationManager().addNotification("SelfWeb", "SelfWeb failed to place the block!");
            ceaseAndDesist();
            return;
        }

        if (prevSlot != -1)
        {
            mc.player.inventory.currentItem = prevSlot;
            mc.playerController.updateController();
        }

        ceaseAndDesist();
    }

    private boolean isCobweb(ItemStack stack){
        return stack.getItem() == Item.getItemFromBlock(Blocks.WEB);
    }

    private void ceaseAndDesist(){
        this.setEnabled(false);
        this.onDisable();
    }
}
