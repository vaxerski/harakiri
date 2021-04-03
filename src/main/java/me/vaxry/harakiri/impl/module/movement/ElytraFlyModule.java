package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class ElytraFlyModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Mode to use for elytra flight.", Mode.VANILLA);

    private enum Mode {
        VANILLA, PACKET, CONTROL
    }

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd", "amount", "s"}, "Speed multiplier for elytra flight, higher values equals more speed.", 1.0f, 0.0f, 10.0f, 0.01f);

    public final Value<Boolean> autoStart = new Value<Boolean>("AutoStart", new String[]{"AutoStart", "Auto-Start", "start", "autojump", "as"}, "Hold down the jump key to have an easy automated lift off.", true);
    public final Value<Float> autoStartDelay = new Value<Float>("StartDelay", new String[]{"AutoStartDelay", "startdelay", "autojumpdelay", "asd"}, "Delay(ms) between auto-start attempts.", 20.0f, 0.0f, 200.0f, 5.0f);
    //public final Value<Boolean> autoEquip = new Value<Boolean>("AutoEquip", new String[]{"AutoEquipt", "AutoElytra", "Equip", "Equipt", "ae"}, "Automatically equips a durable elytra before or during flight. (inventory only, not hotbar!)", false);
    //public final Value<Float> autoEquipDelay = new Value<Float>("EquipDelay", new String[]{"AutoEquipDelay", "AutoEquiptDelay", "equipdelay", "aed"}, "Delay(ms) between elytra equip swap attempts.", 200.0f, 0.0f, 1000.0f, 10.0f);
    //public final Value<Boolean> stayAirborne = new Value<Boolean>("StayAirborne", new String[]{"Airborne", "StayInAir", "Stay-Airborne", "air", "sa"}, "Attempts to always keep the player airborne (only use when AutoEquip is enabled).", false);
    public final Value<Boolean> autoDock = new Value<Boolean>("AutoDock", new String[]{"Autodock", "ad", "dock"}, "Stops your movement when 0.3 blocks off the ground.", false);
    //public final Value<Boolean> stayAirborneDisable = new Value<Boolean>("StayAirborneDisable", new String[]{"AutoDisableStayAirborne", "StayAirborneAutoDisable", "Stay-Airborne-Disable", "DisableStayInAir", "adsa", "dsa", "sad"}, "Automatically disables StayAirborne when on the ground.", true);
    //public final Value<Float> stayAirborneDelay = new Value<Float>("StayAirborneDelay", new String[]{"StayAirborneWait", "Stay-Airborne-Delay", "sadelay"}, "Delay (in ms) between stayairborne attempts.", 100.0f, 0.0f, 400.0f, 5.0f);
    public final Value<Boolean> disableInLiquid = new Value<Boolean>("DisableInLiquid", new String[]{"DisableInWater", "DisableInLava", "disableliquid", "liquidoff", "noliquid", "dil"}, "Disables elytra flight when the player is in contact with water.", false);
    public final Value<Boolean> infiniteDurability = new Value<Boolean>("InfiniteDurability", new String[]{"InfiniteDura", "dura", "inf", "infdura"}, "Only works on shitty servers.", false);
    public final Value<Boolean> noKick = new Value<Boolean>("NoKick", new String[]{"AntiKick", "Kick", "nk"}, "Bypass the server kicking you for flying while packet elytra-flight.", true);

    private final Timer startDelayTimer = new Timer();
    private final Timer equipDelayTimer = new Timer();
    private final Timer stayAirborneTimer = new Timer();

    public ElytraFlyModule() {
        super("ElytraFly", new String[]{"Elytra", "ElytraPlus", "Elytra+"}, "Allows you to fly with elytras", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.capabilities.isFlying = false;
        }
    }

    /*@Override
    public String getMetaData() {
        if (this.autoEquip.getValue())
            return "" + this.getElytraCount();

        return super.getMetaData();
    }*/

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();

        //if (!this.autoEquip.getValue() && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
        //    return;

        switch (event.getStage()) {
            case PRE:
                final ItemStack stackOnChestSlot = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

                // handle disabling stay airborne if it is on
               // if (this.stayAirborneDisable.getValue() && this.stayAirborne.getValue()) {
               //     if (mc.player.onGround) {
               //         this.stayAirborne.setValue(false);
                //        Harakiri.get().logChat("\247rToggled\2477 ElytraFly " + this.stayAirborne.getName() + " \247coff\247r, as you've touched the ground.");
                //    }
                //}

               /* if (this.autoEquip.getValue()) {
                    // ensure player has an elytra on before running any code
                    if (stackOnChestSlot.isEmpty() && stackOnChestSlot.getItem() != Items.ELYTRA) {
                        if (InventoryUtil.hasItem(Items.ELYTRA)) {
                            if (this.getElytraSlot() != -1 && this.equipDelayTimer.passed(this.autoEquipDelay.getValue())) {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, this.getElytraSlot(), 0, ClickType.QUICK_MOVE, mc.player);
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                this.equipDelayTimer.reset();
                            }
                        }
                    }

                    // check for broken elytra when auto equip is enabled
                    if (!stackOnChestSlot.isEmpty() && stackOnChestSlot.getItem() == Items.ELYTRA) {
                        if (!ItemElytra.isUsable(stackOnChestSlot)) {
                            if (this.getElytraCount() > 0 && this.getElytraSlot() != -1 && this.equipDelayTimer.passed(this.autoEquipDelay.getValue())) {
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.QUICK_MOVE, mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, this.getElytraSlot(), 0, ClickType.PICKUP, mc.player);
                                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                this.equipDelayTimer.reset();
                            }
                        }

                        if (this.stayAirborne.getValue() && !mc.player.isElytraFlying() && mc.player.motionY < 0) { // player motion is falling
                            if (this.stayAirborneTimer.passed(this.stayAirborneDelay.getValue())) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                this.stayAirborneTimer.reset();
                            }
                        }
                    }
                }*/

                // liquid check
                if (this.disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
                    if (mc.player.isElytraFlying()) {
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    }
                    return;
                }

                // automatic jump start
                if (this.autoStart.getValue()) {
                    if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying()) { // jump is held, player is not elytra flying
                        if (mc.player.motionY < 0) { // player motion is falling
                            if (this.startDelayTimer.passed(this.autoStartDelay.getValue())) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                                this.startDelayTimer.reset();
                            }
                        }
                    }
                }

                // the player's rotation yaw
                final double rotationYaw = Math.toRadians(mc.player.rotationYaw);

                // ensure the player is in the elytra flying state
                if (mc.player.isElytraFlying()) {
                    if (this.mode.getValue() == Mode.PACKET) {
                        this.freezePlayer(mc.player);
                        this.runNoKick(mc.player);

                        final double[] directionSpeedPacket = MathUtil.directionSpeed(this.speed.getValue());

                        if (mc.player.movementInput.jump) {
                            mc.player.motionY = this.speed.getValue();
                        }

                        if (mc.player.movementInput.sneak) {
                            mc.player.motionY = -this.speed.getValue();
                        }

                        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                            mc.player.motionX = directionSpeedPacket[0];
                            mc.player.motionZ = directionSpeedPacket[1];
                        }

                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                    } else if (this.mode.getValue() == Mode.VANILLA) {
                        final float speedScaled = this.speed.getValue() * 0.05f; // 5/100 of original value
                        final double[] directionSpeedVanilla = MathUtil.directionSpeed(speedScaled);
                        if (mc.player.movementInput.jump) {
                            mc.player.motionY = this.speed.getValue();
                        }

                        if (mc.player.movementInput.sneak) {
                            mc.player.motionY = -this.speed.getValue();
                        }
                        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                            mc.player.motionX += directionSpeedVanilla[0];
                            mc.player.motionZ += directionSpeedVanilla[1];
                        }
                    }
                }

                if (this.infiniteDurability.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
            case POST:
                if (this.infiniteDurability.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                }
                break;
        }
    }

    @Listener
    public void move(EventMove event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.isElytraFlying()) {
            if (this.mode.getValue() == Mode.CONTROL) {
                mc.player.motionY = 0; // Prevent the player from slowly falling down

                final double[] directionSpeedControl = MathUtil.directionSpeed(this.speed.getValue());
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
                if (mc.player.movementInput.jump) {
                    mc.player.motionY = this.speed.getValue() / 2;
                } else if (mc.player.movementInput.sneak) {
                    mc.player.motionY = -this.speed.getValue() / 2;
                }
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    mc.player.motionX = directionSpeedControl[0];
                    mc.player.motionZ = directionSpeedControl[1];
                }

                // Dock
                if(this.autoDock.getValue() && mc.player.movementInput.sneak && !Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled()) {
                    BlockPos underMe = GetLocalPlayerPosFloored().down();
                    Block under = mc.world.getBlockState(underMe).getBlock();
                    BlockPos underMe2 = GetLocalPlayerPosFloored().down().down();
                    Block under2 = mc.world.getBlockState(underMe2).getBlock();
                    if(under == Blocks.AIR && (under2 != Blocks.AIR && under2 != Blocks.WATER && under2 != Blocks.FLOWING_WATER && under2 != Blocks.FLOWING_LAVA && under2 != Blocks.LAVA)){
                        if(mc.player.motionY < 0)
                            mc.player.motionY = -0.1f;
                    }
                    if (under != Blocks.AIR && under != Blocks.WATER && under != Blocks.LAVA && under != Blocks.FLOWING_WATER && under != Blocks.FLOWING_LAVA) {
                        if (mc.player.motionY < 0) {
                            mc.player.motionY = -0.03;
                            mc.player.motionX = directionSpeedControl[0] / 10.f;
                            mc.player.motionZ = directionSpeedControl[1] / 10.f;
                            mc.player.rotationPitch = 0;
                            if (mc.player.posY <= underMe.getY() + 1.34f) {
                                mc.player.motionY = 0;
                                mc.player.setSneaking(false);
                                mc.player.movementInput.sneak = false;
                            }
                        }
                    }
                }

                event.setX(mc.player.motionX);
                event.setY(mc.player.motionY);
                event.setZ(mc.player.motionZ);
            }
        }


        // Fix movement if avoid
        if(Harakiri.get().getModuleManager().find(AvoidModule.class).isEnabled()){
            AvoidModule avm = (AvoidModule)Harakiri.get().getModuleManager().find(AvoidModule.class);
            avm.fixMovevemt(event, mc.player);
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("See that bird? *rips wings off*")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("You've been flying for a while.")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("ElytraFly is disabled.")) {
                    event.setCanceled(true);
                }

                if (packet.getChatComponent().getUnformattedText().equalsIgnoreCase("Your wings are safe under the Newfag Assisted Flight Temporal Agreement.")) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private void freezePlayer(EntityPlayer player) {
        player.motionX = 0;
        player.motionY = 0;
        player.motionZ = 0;
    }

    private void runNoKick(EntityPlayer player) {
        if (this.noKick.getValue() && !player.isElytraFlying()) {
            if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.04f;
            }
        }
    }

    private int getElytraSlot() {
        int bestElytraDurability = 1000000;
        int bestElytraSlot = -1;
        for (int slot = 44; slot > 8; slot--) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                if (!ItemElytra.isUsable(stack))
                    continue;

                if (stack.getItemDamage() < bestElytraDurability) {
                    bestElytraDurability = stack.getItemDamage();
                    bestElytraSlot = slot;
                }
            }
        }
        return bestElytraSlot;
    }

    private int getElytraCount() {
        int elytras = 0;

        if (Minecraft.getMinecraft().player == null)
            return elytras;

        for (int slot = 44; slot > 8; slot--) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(slot);
            if (!ItemElytra.isUsable(stack))
                continue;

            if (!stack.isEmpty() && stack.getItem() == Items.ELYTRA) {
                elytras += stack.getCount();
            }
        }

        return elytras;
    }

    private double getSpeed() {
        return this.speed.getValue() * 1.0f * (Math.cos(3.1415926535f) * (-0.5) + 0.5);
    }

    private void setSpeed(double yaw) {
        final double speedo = getSpeed();
        final Minecraft mc = Minecraft.getMinecraft();

        mc.player.setVelocity(Math.sin(-yaw) * speedo, mc.player.motionY, Math.cos(yaw) * speedo);
    }

    private double getYaw() {
        final Minecraft mc = Minecraft.getMinecraft();

        double strafe = 90 * (int)mc.player.movementInput.moveStrafe;

        if((int)mc.player.movementInput.moveForward != 0.0f){
            strafe *= (int)mc.player.movementInput.moveForward * 0.5f;
        }

        double yaw = mc.player.rotationYaw;
        yaw -= strafe;
        if((int)mc.player.movementInput.moveForward < 0.0f)
            yaw -= 180;


        return Math.toRadians(yaw);
    }

    public static BlockPos GetLocalPlayerPosFloored()
    {
        return new BlockPos(Math.floor(Minecraft.getMinecraft().player.posX), Math.floor(Minecraft.getMinecraft().player.posY), Math.floor(Minecraft.getMinecraft().player.posZ));
    }
}
