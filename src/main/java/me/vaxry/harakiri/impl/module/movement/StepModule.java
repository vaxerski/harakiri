package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import me.vaxry.harakiri.impl.module.world.TimerModule;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author: noil
 * Date: 7/14/2019
 * Time: 2:30 PM
 */
public final class StepModule extends Module {

    //public final Value<Integer> height = new Value<Integer>("Height", new String[]{"Height", "H"}, "The step block-height.", 2, 1, 4, 1);
    public final Value<Integer> ticks = new Value<Integer>("Ticks", new String[]{"Ticks", "T"}, "Tick delay.", 2, 0, 10, 0);
    public final Value<Integer> wiait = new Value<Integer>("Wait", new String[]{"Wait", "W"}, "Wait delay.", 2, 1, 10, 0);
    public final Value<Float> timer = new Value<Float>("TimerSpeed", new String[]{"TimerSpeed", "Timer"}, "Timer to use when stepping.", 1.f, 0.1f, 2.f, 0.1f);
    public final Value<Boolean> sureStep = new Value<Boolean>("SureStep", new String[]{"SureStep", "S"}, "SureStep.", false);
    public final Value<Float> sureStepAm = new Value<Float>("SureStepPerc", new String[]{"SureStepPerc", "SSP"}, "SureStep amount.", 0.25f, 0.f, 2.f, 0.1f);
    public final Value<Boolean> stopMotion = new Value<Boolean>("StopMotion", new String[]{"StopMotion", "SM"}, "StopMotion.", false);


    //public final Value<Boolean> spoof = new Value<Boolean>("Spoof", new String[]{"Spoof", "S"}, "Whether to spoof.", false);

    private final double[] oneblockPositions = new double[] { 0.42D, 0.75D };
    private final double[] twoblockPositions = new double[] { 0.4D, 0.75D, 0.5D, 0.41D, 0.83D, 1.16D, 1.41D, 1.57D, 1.58D, 1.42D };
    private final double[] futurePositions = new double[] { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D };
    final double[] twoFiveOffset = new double[] { 0.425D, 0.821D, 0.699D, 0.599D, 1.022D, 1.372D, 1.652D, 1.869D, 2.019D, 1.907D };
    private final double[] threeBlockPositions = new double[] { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D, 1.78D, 1.63D, 1.51D, 1.9D, 2.21D, 2.45D, 2.43D };
    private final double[] fourBlockPositions = new double[] { 0.42D, 0.78D, 0.63D, 0.51D, 0.9D, 1.21D, 1.45D, 1.43D, 1.78D, 1.63D, 1.51D, 1.9D, 2.21D, 2.45D, 2.43D, 2.78D, 2.63D, 2.51D, 2.9D, 3.21D, 3.45D, 3.43D };
    private double[] selectedPositions = new double[0];

    private int ticksLast = 0;
    private int waitS = 0;
    private boolean isStepping = false;
    private boolean stepX = false;

    public StepModule() {
        super("Step", new String[]{"stp"}, "Allows you to step up blocks you shouldn't.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            final TimerModule timer = (TimerModule)Harakiri.INSTANCE.getModuleManager().find(TimerModule.class);
            if(!timer.isEnabled())
                Minecraft.getMinecraft().timer.tickLength = 50.0f;

            if(ticksLast - 100 > mc.player.ticksExisted){
                ticksLast = 0;
                // Probably died or sumn reset.
            }

            if(ticksLast + this.ticks.getValue() > mc.player.ticksExisted){
                return;
            }


            int height = 1;
            if(!(mc.player.collidedHorizontally && mc.player.onGround) && !isStepping){
                return;
            }

            stepX = Math.abs(mc.player.motionX) > Math.abs(mc.player.motionZ);

            isStepping = true;

            if(waitS < this.wiait.getValue()){
                waitS++;
                return;
            }

            AxisAlignedBB bb = mc.player.getEntityBoundingBox();
            final AxisAlignedBB extendedbb = new AxisAlignedBB(bb.minX - 0.1f,
                    bb.minY + 0.1f,
                    bb.minZ - 0.1f,
                    bb.maxX + 0.1f,
                    bb.maxY - 0.1f,
                    bb.maxZ + 0.1f);

            // Check if the collision is 1 or 2
            for (int x = MathHelper.floor(extendedbb.minX); x < MathHelper.floor(extendedbb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(extendedbb.minZ); z < MathHelper.floor(extendedbb.maxZ + 1.0D); z++) {
                    BlockPos blockPos = new BlockPos(x, mc.player.getPosition().getY() + 1.0D, z);
                    Block block = mc.world.getBlockState(blockPos).getBlock();
                    final AxisAlignedBB blockbb = new AxisAlignedBB(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            blockPos.getX() + 1,
                            blockPos.getY() + 1,
                            blockPos.getZ() + 1);
                    if (!(block instanceof net.minecraft.block.BlockAir)) {
                        if(!mc.world.getBlockState(blockPos).isFullBlock())
                            continue;
                        if(!mc.world.getBlockState(blockPos).isOpaqueCube())
                            continue;
                        if(blockbb.intersects(extendedbb)) {
                            height = 2;
                            break;
                        }
                    }
                }
            }

            // We have confirmed height, now check if its legal.

            boolean legal = true;

            final AxisAlignedBB afterStep = new AxisAlignedBB(bb.minX - 0.1f,
                    bb.minY + height,
                    bb.minZ - 0.1f,
                    bb.maxX + 0.1f,
                    bb.maxY + height,
                    bb.maxZ + 0.1f);

            for (int x = MathHelper.floor(afterStep.minX); x < MathHelper.floor(afterStep.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(afterStep.minZ); z < MathHelper.floor(afterStep.maxZ + 1.0D); z++) {
                    BlockPos blockPos = new BlockPos(x, mc.player.getPosition().getY() + 3.0D, z);
                    Block block = mc.world.getBlockState(blockPos).getBlock();
                    final AxisAlignedBB blockbb = new AxisAlignedBB(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            blockPos.getX() + 1,
                            blockPos.getY() + 1,
                            blockPos.getZ() + 1);
                    if (!(block instanceof net.minecraft.block.BlockAir)) {
                        if(block instanceof net.minecraft.block.BlockGrass || block instanceof net.minecraft.block.BlockFlower)
                            continue;

                        if(blockbb.intersects(afterStep)) {
                            legal = false;
                            break;
                        }
                    }
                }
            }

            if(!legal) {
                Harakiri.INSTANCE.logChat("not legal");
                isStepping = false;
                return;
            }


            switch (height) {
                case 1:
                    this.selectedPositions = this.oneblockPositions;
                    break;
                case 2:
                    this.selectedPositions = this.twoblockPositions;
                    break;
            }


            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                    Block block = mc.world.getBlockState(new BlockPos(x, bb.maxY + 1.0D, z)).getBlock();
                    if (!(block instanceof net.minecraft.block.BlockAir)) {
                        Harakiri.INSTANCE.logChat("not air");
                        isStepping = false;
                        return;
                    }
                }
            }

            if (mc.player.onGround && !mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.isInsideOfMaterial(Material.LAVA) && mc.player.collidedVertically && mc.player.fallDistance == 0.0F && !mc.gameSettings.keyBindJump.pressed && mc.player.collidedHorizontally && !mc.player.isOnLadder() /*&& (this.packets > this.selectedPositions.length - 2 || (((Boolean)this.spoof.getValue()).booleanValue() && this.packets > ((Integer)this.ticks.getValue()).intValue()))*/) {
                Minecraft.getMinecraft().timer.tickLength = 50.0f / this.timer.getValue();
                for (double position : this.selectedPositions) {
                    mc.player.connection.sendPacket((Packet) new CPacketPlayer.Position(mc.player.posX, mc.player.posY + position, mc.player.posZ, true));
                }

                if(sureStep.getValue()){
                    double x = mc.player.posX + mc.player.motionX * this.sureStepAm.getValue();
                    double z = mc.player.posZ + mc.player.motionZ * this.sureStepAm.getValue();

                    mc.player.setPosition(x, mc.player.posY + this.selectedPositions[this.selectedPositions.length - 1], z);
                }else{
                    mc.player.setPosition(mc.player.posX, mc.player.posY + this.selectedPositions[this.selectedPositions.length - 1], mc.player.posZ);
                }
            }

            ticksLast = mc.player.ticksExisted;
            waitS = 0;
            isStepping = false;
        }
    }

    @Listener
    public void move(EventMove event){
        if(!isStepping){
            return;
        }

        if(this.stopMotion.getValue()){
            // we colliding already
            if(stepX)
                event.setX(0);
            else
                event.setZ(0);
        }
    }
}

