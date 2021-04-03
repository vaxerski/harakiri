package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.world.TimerModule;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.vecmath.Vector2f;

public final class StepModule extends Module {

    //public final Value<Integer> height = new Value<Integer>("Height", new String[]{"Height", "H"}, "The step block-height.", 2, 1, 4, 1);
    public final Value<Integer> ticks = new Value<Integer>("Ticks", new String[]{"Ticks", "T"}, "Tick delay.", 2, 0, 10, 0);
    public final Value<Integer> wiait = new Value<Integer>("Wait", new String[]{"Wait", "W"}, "Wait delay.", 2, 1, 10, 0);
    public final Value<Float> timer = new Value<Float>("TimerSpeed", new String[]{"TimerSpeed", "Timer"}, "Timer to use when stepping.", 1.f, 0.1f, 2.f, 0.1f);
    public final Value<Boolean> sureStep = new Value<Boolean>("SureStep", new String[]{"SureStep", "S"}, "SureStep.", false);
    public final Value<Float> sureStepAm = new Value<Float>("SureStepPerc", new String[]{"SureStepPerc", "SSP"}, "SureStep amount.", 0.25f, 0.f, 2.f, 0.1f);
    public final Value<Boolean> stopMotion = new Value<Boolean>("StopMotion", new String[]{"StopMotion", "SM"}, "StopMotion.", false);
    //public final Value<Integer> resetTimer = new Value<Integer>("ResetTimer", new String[]{"ResetTimer", "RT"}, "How long you have to be on ground to step again. (in ms)", 100, 0, 2000, 100);


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
            step();
        }
    }

    private void step(){
        final Minecraft mc = Minecraft.getMinecraft();

        final TimerModule timer = (TimerModule)Harakiri.get().getModuleManager().find(TimerModule.class);
        if(!timer.isEnabled())
            Minecraft.getMinecraft().timer.tickLength = 50.0f;

        if(ticksLast - 100 > mc.player.ticksExisted){
            ticksLast = 0;
            // Probably died or sumn reset.
        }

        if(ticksLast + this.ticks.getValue() > mc.player.ticksExisted){
            return;
        }

        if(!(mc.player.collidedHorizontally && mc.player.onGround) && !isStepping){
            return;
        }

        if(!mc.player.collidedHorizontally)
            return;

        if(!mc.player.onGround)
            return;

        if(waitS < this.wiait.getValue()){
            waitS++;
            return;
        }

        AxisAlignedBB bb = mc.player.getEntityBoundingBox();

        final float angleLocal = getAngFromLocal();

        final int height = checkIfAngleLegitStep(angleLocal);
        if(height == 0)
            return;

        stepX = Math.abs(mc.player.motionX) > Math.abs(mc.player.motionZ);

        isStepping = true;

        switch (height) {
            case 1:
                this.selectedPositions = this.oneblockPositions;
                break;
            case 2:
                this.selectedPositions = this.twoblockPositions;
                break;
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

    private float getAngFromLocal(){
        final Minecraft mc = Minecraft.getMinecraft();
        float result = mc.player.rotationYaw;

        final boolean isMovingForward = mc.gameSettings.keyBindForward.pressed;
        final boolean isMovingBack = mc.gameSettings.keyBindBack.pressed;

        if(mc.gameSettings.keyBindBack.pressed)
            result += 180;

        final float multiplySide = isMovingForward || isMovingBack ? 0.5f : 1.f;

        final float offsetAng = isMovingBack ? -(90 * multiplySide) : 90 * multiplySide;
        if(mc.gameSettings.keyBindRight.pressed)
            result += offsetAng;

        if(mc.gameSettings.keyBindLeft.pressed)
            result -= offsetAng;

        // clamp result
        if(result > 180F){
            result = -180 + (result - 180);
        }else if(result < -180F){
            result = 180 + (result + 180);
        }

        return result;
    }

    private int checkNorth(BlockPos localPos){
        final Minecraft mc = Minecraft.getMinecraft();
        if(!isBlockValidPass(mc.world.getBlockState(localPos.north()).getBlock())){
            // hmm, solid for one.
            if(!isBlockValidPass(mc.world.getBlockState(localPos.north().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.north().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.north().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }else{
                // Empty for two. may be possible oneblock
                if(isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.north().up().up()).getBlock())){
                    // Possible oneblock!
                    return 1;
                }
            }
        }else{
            // check for 2
            if(!isBlockValidPass(mc.world.getBlockState(localPos.north().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.north().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.north().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }
        }

        return 0;
    }

    private int checkEast(BlockPos localPos){
        final Minecraft mc = Minecraft.getMinecraft();
        if(!isBlockValidPass(mc.world.getBlockState(localPos.east()).getBlock())){
            // hmm, solid for one.
            if(!isBlockValidPass(mc.world.getBlockState(localPos.east().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.east().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.east().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }else{
                // Empty for two. may be possible oneblock
                if(isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.east().up().up()).getBlock())){
                    // Possible oneblock!
                    return 1;
                }
            }
        }else{
            // check for 2
            if(!isBlockValidPass(mc.world.getBlockState(localPos.east().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.east().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.east().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }
        }

        return 0;
    }

    private int checkWest(BlockPos localPos){
        final Minecraft mc = Minecraft.getMinecraft();
        if(!isBlockValidPass(mc.world.getBlockState(localPos.west()).getBlock())){
            // hmm, solid for one.
            if(!isBlockValidPass(mc.world.getBlockState(localPos.west().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.west().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.west().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }else{
                // Empty for two. may be possible oneblock
                if(isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.west().up().up()).getBlock())){
                    // Possible oneblock!
                    return 1;
                }
            }
        }else{
            // check for 2
            if(!isBlockValidPass(mc.world.getBlockState(localPos.west().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.west().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.west().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }
        }

        return 0;
    }

    private int checkSouth(BlockPos localPos){
        final Minecraft mc = Minecraft.getMinecraft();
        if(!isBlockValidPass(mc.world.getBlockState(localPos.south()).getBlock())){
            // hmm, solid for one.
            if(!isBlockValidPass(mc.world.getBlockState(localPos.south().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.south().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.south().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }else{
                // Empty for two. may be possible oneblock
                if(isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.south().up().up()).getBlock())){
                    // Possible oneblock!
                    return 1;
                }
            }
        }else{
            // check for 2
            if(!isBlockValidPass(mc.world.getBlockState(localPos.south().up()).getBlock())){
                //Solid for two
                if(isBlockValidPass(mc.world.getBlockState(localPos.south().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.south().up().up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up()).getBlock())
                        && isBlockValidPass(mc.world.getBlockState(localPos.up().up().up()).getBlock())){
                    // Possible twoblock!
                    return 2;
                }
            }
        }

        return 0;
    }

    private int checkIfAngleLegitStep(float angle){
        final Minecraft mc = Minecraft.getMinecraft();

        // Four quarters
        final BlockPos localPos = GetLocalPlayerPosFloored();

        final int north = checkNorth(localPos);
        final int east = checkEast(localPos);
        final int west = checkWest(localPos);
        final int south = checkSouth(localPos);

        // Eligibleness
        final boolean eligiblePlusZ = (mc.player.posZ - 0.700F) % 1 == 0;
        final boolean eligibleMinusZ = (mc.player.posZ - 0.300F) % 1 == 0;
        final boolean eligiblePlusX = (mc.player.posX - 0.700F) % 1 == 0;
        final boolean eligibleMinusX = (mc.player.posX - 0.300F) % 1 == 0;

        Vector2f moveVec = new Vector2f(0,0);

        int ret = 0;
        // First, -180 : -90 (+X-Z)
        if(angle >= -180 && angle <= -90){
            if((north != 0 && eligibleMinusZ)) {
                moveVec.y -= 0.1f;
                ret = north;
            }
            if(east != 0 && eligiblePlusX) {
                moveVec.x += 0.1f;
                ret = Math.max(north, east);
            }
        }
        // +X+Z
        else if(angle >= -90 && angle <= 0){
            if((south != 0 && eligiblePlusZ)) {
                moveVec.y += 0.1f;
                ret = south;
            }
            if(east != 0 && eligiblePlusX) {
                moveVec.x += 0.1f;
                ret = Math.max(south, east);
            }
        }
        // -X+Z
        else if(angle >= 0 && angle <= 90){
            if((south != 0 && eligiblePlusZ)) {
                moveVec.y += 0.1f;
                ret = south;
            }
            if(west != 0 && eligibleMinusX) {
                moveVec.x -= 0.1f;
                ret = Math.max(south, west);
            }
        }
        // -X-Z
        else if(angle >= 90 && angle <= 180){
            if((north != 0 && eligibleMinusZ)) {
                moveVec.y -= 0.1f;
                ret = north;
            }
            if(west != 0 && eligibleMinusX) {
                moveVec.x -= 0.1f;
                ret = Math.max(north, west);
            }
        }

        // Check hitbox
        AxisAlignedBB playerbb = mc.player.getEntityBoundingBox();
        final AxisAlignedBB movedbb1 = new AxisAlignedBB(playerbb.minX + moveVec.x,
                playerbb.minY + 1,
                playerbb.minZ + moveVec.y,
                playerbb.maxX + moveVec.x,
                playerbb.maxY + 1,
                playerbb.maxZ + moveVec.y);

        final AxisAlignedBB movedbb2 = new AxisAlignedBB(playerbb.minX + moveVec.x,
                playerbb.minY + 2,
                playerbb.minZ + moveVec.y,
                playerbb.maxX + moveVec.x,
                playerbb.maxY + 2,
                playerbb.maxZ + moveVec.y);

        for (int x = localPos.getX() - 2; x < localPos.getX() + 2; x++) {
            for (int z = localPos.getZ() - 2; z < localPos.getZ() + 2; z++) {
                for(int y = localPos.getY(); y < localPos.getY() + 4; y++) {
                    BlockPos blockPos = new BlockPos(x, mc.player.getPosition().getY() + 1.0D, z);
                    Block block = mc.world.getBlockState(blockPos).getBlock();
                    final AxisAlignedBB blockbb = new AxisAlignedBB(
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            blockPos.getX() + 1,
                            blockPos.getY() + 1,
                            blockPos.getZ() + 1);
                    if (!isBlockValidPass(block)) {
                        //if(!mc.world.getBlockState(blockPos).isFullBlock())
                        //continue;
                        if (blockbb.intersects(movedbb1) && ret == 1) {
                            if(!blockbb.intersects(movedbb2))
                                ret = 2;
                            else
                                return 0;
                        }else if(blockbb.intersects(movedbb1) && blockbb.intersects(movedbb2))
                            return 0;
                    }
                }
            }
        }

        return ret;
    }

    public boolean isBlockValidPass(Block block){
        return block instanceof BlockAir || block instanceof BlockSnow || block instanceof net.minecraft.block.BlockTallGrass || block instanceof BlockBush || block instanceof BlockFlower || block instanceof BlockFlowerPot || block instanceof net.minecraft.block.BlockFlower || block instanceof BlockTorch || block instanceof BlockSign;
    }

    public BlockPos GetLocalPlayerPosFloored()
    {
        return new BlockPos(Math.floor(Minecraft.getMinecraft().player.posX), Math.floor(Minecraft.getMinecraft().player.posY), Math.floor(Minecraft.getMinecraft().player.posZ));
    }
}

