package me.vaxry.harakiri.impl.module.world;

import java.time.chrono.MinguoEra;

import javax.swing.JOptionPane;

import com.mojang.realmsclient.gui.ChatFormatting;

import org.apache.commons.io.filefilter.TrueFileFilter;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable.EventStage;
import me.vaxry.harakiri.framework.event.minecraft.EventRunTick;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.player.EventDestroyBlock;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventRightClickBlock;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.event.world.EventSetBlockState;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import me.vaxry.harakiri.framework.util.BlockUtil;
import me.vaxry.harakiri.framework.util.EntityUtil;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.gui.hud.component.PacketTimeComponent;
import me.vaxry.harakiri.impl.module.combat.AutoDisconnectModule;
import me.vaxry.harakiri.impl.module.combat.NoCrystalModule;
import me.vaxry.harakiri.impl.module.hidden.ReconnectModule;
import me.vaxry.harakiri.impl.module.misc.NoGlitchBlocks;
import me.vaxry.harakiri.impl.module.movement.AutoWalkModule;
import me.vaxry.harakiri.impl.module.player.AutoEatModule;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import me.vaxry.harakiri.impl.module.player.HighwayAutoUnpackModule;
import me.vaxry.harakiri.impl.module.player.RotationLock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NukerModule extends Module {

    float r = 0xFE;
    float g = 0;
    float b = 0;
    int stage = 1;

    final float rainSpeed = 5;

    // for hwinfo
    public String nukerStatus = "wrong mode";
    public Timer  nukerStartTimer = new Timer();
    public Vec3d  startVec = new Vec3d(0,0,0);

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The nuker mode to use.", Mode.SELECTION);

    public enum Mode {
        SELECTION, ALL, CREATIVE
    }

    public enum TunnelDir {
        PLUSX, MINUSX, PLUSZ, MINUSZ
    }

    public final Value<Float> distance = new Value<Float>("Distance", new String[]{"Dist", "D"}, "Maximum distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> fixed = new Value<Boolean>("FixedDist", new String[]{"Fixed", "fdist", "F"}, "Use vertical and horizontal distances in blocks instead of distances relative to the camera.", false);
    public final Value<Float> vDistance = new Value<Float>("Vertical", new String[]{"Vertical", "vdist", "VD"}, "Maximum vertical distance in blocks the nuker will reach.", 4.5f, 0.0f, 5.0f, 0.1f);
    public final Value<Float> hDistance = new Value<Float>("Horizontal", new String[]{"Horizontal", "hist", "HD"}, "Maximum horizontal distance in blocks the nuker will reach.", 3f, 0.0f, 5.0f, 0.1f);
    public final Value<Boolean> flatten = new Value<Boolean>("Flatten", new String[]{"Flatten", "flat", "flt"}, "Flatten the region", false);
    public final Value<Boolean> drawMining = new Value<Boolean>("DrawMining", new String[]{"drawmining", "dm", "drawm"}, "Draw mined blocks' outlines", false);
    public final Value<Boolean> highwayMode = new Value<Boolean>("HighwayMode", new String[]{"highwaymode", "hm", "highway"}, "Dig a 2x1 tunnel.", false);
    public final Value<Boolean> stopOnMilestone = new Value<Boolean>("StopOnMilestone", new String[]{"stopOnMilestone", "som", "stop"}, "Stops at 1M intervals.", false);
    public final Value<Boolean> scaffoldCompat = new Value<Boolean>("ScaffoldCompat", new String[]{"scaffoldcompat", "sc", "scaffold"}, "Stop when scaffold needed.", false);
    public final Value<Boolean> fillCompat = new Value<Boolean>("FillCompat", new String[]{"fillcompat", "fc", "fill"}, "Stop when filling needed.", false);
    public final Value<Boolean> eatCompat = new Value<Boolean>("AutoEatCompat", new String[]{"autoeatcompat", "aec", "eat"}, "Stop when autoeat needed.", false);

    public TunnelDir TunnelDirection = TunnelDir.PLUSX;

    private final RotationTask rotationTask = new RotationTask("NukerTask", 2);

    private float lastTimerValue = 1;
    private boolean timerset = false;

    private Block selected = null;
    private BlockPos currentPos = null;

    private Timer lastStuckTimer = new Timer();

    private Timer lastFreezeCooldownTimer = new Timer();

    private UnpackStep currentUnpackStep = UnpackStep.PLACESHULKER;

    private BlockPos startingPos = new BlockPos(0,121,0);
    private boolean isBaritoneRunning = false;
    private Timer illegalPosTimer = new Timer();
    private Timer baritoneTimer = new Timer();
    private boolean baritoneSwitchBool = false;
    private Timer baritoneReachedTimer = new Timer();

    private Timer stuckTimer = new Timer();
    private Vec3d lastStuckPos = new Vec3d(0,0,0);
    private boolean needsToDisconnect = false;
    private Timer joinWaitTimer = new Timer();
    private boolean justJoined = false;

    private Timer fallingTimer = new Timer();

    private boolean shownMsgBox = false;

    private Timer lastShulkerAbandoned = new Timer();

    public NukerModule() {
        super("Nuker", new String[]{"Nuke"}, "Automatically mines blocks within reach.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.selected = null;

        if (Minecraft.getMinecraft().player == null)
            return;
        
        final String mcfacing = Minecraft.getMinecraft().player.getHorizontalFacing().getName();
        if (mcfacing == "south") {
            TunnelDirection = TunnelDir.PLUSZ;
        } else if (mcfacing == "north") {
            TunnelDirection = TunnelDir.MINUSZ;
        } else if (mcfacing == "east") {
            TunnelDirection = TunnelDir.PLUSX;
        } else {
            TunnelDirection = TunnelDir.MINUSX;
        }

        this.nukerStartTimer.reset();
        this.startVec = Minecraft.getMinecraft().player.getPositionVector();

        this.lastTimerValue = ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.getValue();
        this.timerset = false;

        this.unpackingInProgress = false;
        this.currentUnpackStep = UnpackStep.PLACESHULKER;
        this.waiting = false;
        this.waittimer.reset();
        this.pickedUpPickSlot = -1;
        this.counterForPickup = 0;
        this.counterForPickedUpAlready = 0;

        this.lastStuckTimer.reset();
        this.shulkerInvSlot = -1;

        this.startingPos = new BlockPos(Minecraft.getMinecraft().player.getPositionVector());
        this.illegalPosTimer.reset();
        this.baritoneSwitchBool = false;
        this.isBaritoneRunning = false;

        this.joinWaitTimer.reset();

        this.shownMsgBox = false;

        // turn on autodisconnect if tunnel
        if (this.highwayMode.getValue()) {
            AutoDisconnectModule adm = (AutoDisconnectModule)Harakiri.get().getModuleManager().find(AutoDisconnectModule.class);
            if (!adm.isEnabled())
                adm.toggle();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getRotationManager().finishTask(this.rotationTask);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (this.highwayMode.getValue()) {
            // and turn on autoreconnect if enabling
            ReconnectModule rm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
            rm.auto.setValue(true);
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        this.joinWaitTimer.reset();
        this.justJoined = true;
    }

    @Listener
    public void onUpdate(EventRender2D event) {
       // if (event.getStage() == EventStage.POST) {

            final Minecraft mc = Minecraft.getMinecraft();

            if (Minecraft.getMinecraft().player.dimension != -1)
                return;

                // reconnect regularly every 15 mins
            if (this.joinWaitTimer.passed(30 * 60 * 1000) && !unpackingInProgress) {

                ReconnectModule rm = (ReconnectModule)Harakiri.get().getModuleManager().find(ReconnectModule.class);

                // literally yoinked from the mc source
                boolean flag = mc.isIntegratedServerRunning();
                boolean flag1 = mc.isConnectedToRealms();
                mc.world.sendQuittingDisconnectingPacket();
                mc.loadWorld((WorldClient) null);

                rm.reconnect();

                this.joinWaitTimer.reset();
            }
            
       // }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || Harakiri.get().getModuleManager().find(FreeCamModule.class).isEnabled())
            return;

        // check for server lag
        final PacketTimeComponent ptc = (PacketTimeComponent)Harakiri.get().getHudManager().findComponent(PacketTimeComponent.class);
        final float seconds = ((System.currentTimeMillis() - ptc.timer.getTime()) / 1000.0f) % 60.0f;

        if (seconds > 1.f) {
            if (Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
            if (Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
            if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
            if (Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
            return; // protect from rare crashes i think
        }

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

                    if (this.nukerStatus == "Illegal position, trying baritone")
                        this.isBaritoneRunning = true;

                    if (this.stopOnMilestone.getValue()) {
                        int nextMilestone = ((int)(Math.max(this.startingPos.getX(), this.startingPos.getZ())/ 1000000.f) + 1) * 1000000;
                        if (Math.max(mc.player.posX, mc.player.posZ) > nextMilestone) {
                            this.nukerStatus = "Reached milestone: stop.";
                            if (Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
                            return;
                        }
                    }
                    
                    if (this.highwayMode.getValue()) {

                        if (Minecraft.getMinecraft().player.dimension != -1){
                            this.nukerStatus = "wrong dimension";
                            return;
                        }

                        if (!this.joinWaitTimer.passed(2000)) {
                            if (this.justJoined) {
                                this.justJoined = false;
                                mc.player.setPosition(mc.player.posX, mc.player.posY + 0.3f, mc.player.posZ);
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.3f, mc.player.posZ, true));
                            }
                            this.nukerStatus = "join wait grace period (2s)";
                            return;
                        }

                        if (Math.abs(Minecraft.getMinecraft().player.posX) >= 29999000 || Math.abs(Minecraft.getMinecraft().player.posZ) >= 29999000) {
                            this.nukerStatus = "REACHED THE END! 29.999M! stop.";
                            if (Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
                            return;
                        }

                        int lengthX = 1;
                        int lengthZ = 1;
                        boolean plusZ = false;
                        boolean plusX = false;

                        switch (TunnelDirection) {
                            case PLUSX:
                                plusX = true;
                            case MINUSX:
                                lengthX = this.hDistance.getValue().intValue();
                            break;
                            case PLUSZ:
                                plusZ = true;
                            case MINUSZ:
                                lengthZ = this.hDistance.getValue().intValue();
                            break;
                        }

                        float yaw;
                        switch (TunnelDirection) {
                            case PLUSX:
                                yaw = -90;
                                break;
                            case MINUSX:
                                yaw = 90;
                                break;
                            case MINUSZ:
                                yaw = 180;
                                break;
                            case PLUSZ:
                                yaw = 0;
                                break;
                            default:
                                yaw = 0;
                                break;
                        }

                        ((RotationLock)Harakiri.get().getModuleManager().find(RotationLock.class)).yaw = yaw;

                        if (!Harakiri.get().getModuleManager().find(AutoDisconnectModule.class).isEnabled())
                            Harakiri.get().getModuleManager().find(AutoDisconnectModule.class).toggle();

                        if (isBaritoneRunning) {
                            if (Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                            if (Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
                            if (Harakiri.get().getModuleManager().find(FillLavaModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(FillLavaModule.class).toggle();
                        } else {
                            if (!Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                            if (!Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                            if (!Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
                            if (!Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
                            if (!Harakiri.get().getModuleManager().find(FillLavaModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(FillLavaModule.class).toggle();

                                
                            if (!Harakiri.get().getModuleManager().find(HighwayAutoUnpackModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(HighwayAutoUnpackModule.class).toggle();
                        }

                        this.disconnectScenarios();

                        if (distance(mc.player.getPositionVector(), this.lastStuckPos) > 2) {
                            this.stuckTimer.reset();
                            this.lastStuckPos = mc.player.getPositionVector();
                        }

                        if (this.stuckTimer.passed(30000)) {

                            // dig 
                            for (long y = Math.round(mc.player.posY - 1) + 2; y > Math.round(mc.player.posY - 1); y -= 1.0D) {
                                for (long x = (long)(mc.player.posX + 1 - (plusX ? 0 : lengthX)); x < (long)(mc.player.posX + (plusX ? lengthX : 0) + 1); x += 1.0D) {
                                    for (long z = (long)(mc.player.posZ + 1 - (plusZ ? 0 : lengthZ)); z < (long)(mc.player.posZ + (plusZ ? lengthZ : 0) + 1); z += 1.0D) {
                                        final BlockPos blockPos = new BlockPos(x, y, z);
                                        breakCreativeBlock(blockPos, false);

                                        this.nukerStatus = "Gaming: " + Integer.toString((int)x) + ", " + Integer.toString((int)y) + ", " + Integer.toString((int)z);
                                    }
                                }
                            }

                            breakFixAhead();
                        }

                        // we are probably glitched in the floor
                        if (!unpackingInProgress && mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getMaterial() != Material.AIR && mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getMaterial() != Material.LAVA) {
                            needsToDisconnect = true;
                            return;
                        }

                        if ((this.scaffoldCompat.getValue() || this.fillCompat.getValue() || this.eatCompat.getValue()) && this.needsToStopToBlock(plusX, plusZ, lengthX == 1 ? 1 : lengthX - 2, lengthZ == 1 ? 1 : lengthZ - 2 )) {
                            // check if we dont have blocks that need to be filled
                            if (!isBaritoneRunning){
                                lastFreezeCooldownTimer.reset();
                                return;
                            }
                            
                        }

                        if (!lastFreezeCooldownTimer.passed(200)) {
                            this.nukerStatus = "Grace period cooldown";
                            return; // 200ms after a pause of a pause more. Filling especially.
                        }

                        // set our hand to a pickaxe
                        ItemStack stack = mc.player.getHeldItemMainhand();

                        if (!(stack.getItem() instanceof ItemPickaxe)) {
                            for (int i = 0; i < 9; ++i) {
                                stack = mc.player.inventory.getStackInSlot(i);

                                if (stack.getItem() instanceof ItemPickaxe) {
                                    if (unpackingInProgress && Harakiri.get().getModuleManager().find(HighwayAutoUnpackModule.class).isEnabled())
                                        break; // dont switch if we are unpacking.
                                    mc.player.inventory.currentItem = i;
                                    mc.playerController.updateController();
                                    break;
                                }
                            }
                        }

                        if ((!(stack.getItem() instanceof ItemPickaxe) || unpackingInProgress) && Harakiri.get().getModuleManager().find(HighwayAutoUnpackModule.class).isEnabled()){
                            if (!isBaritoneRunning && mc.player.getPosition().getY() == 121) {
                                this.nukerStatus = "No Pickaxes";
                                unpackNewPickaxe();
                                return;
                            }
                        } else if (!(stack.getItem() instanceof ItemPickaxe)) {
                            if (!isBaritoneRunning) {
                                this.nukerStatus = "No Pickaxes";
                                return;
                            }
                        }

                        if (isBaritoneRunning) {

                            this.nukerStatus = "waiting for baritone to finish...";

                            final AutoEatModule aem = ((AutoEatModule) Harakiri.get().getModuleManager().find(AutoEatModule.class));
                            if (mc.player.getFoodStats().getFoodLevel() < aem.hunger.getValue() || mc.player.getHealth() < aem.health.getValue()) {
                                this.nukerStatus = "Baritone stopped by autoeat";

                                if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                                if (Harakiri.get().getModuleManager().find(TimerModule.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(TimerModule.class).toggle();
                                if (Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                                if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                                if (Harakiri.get().getModuleManager().find(RotationLock.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(RotationLock.class).toggle();
                                if (Harakiri.get().getModuleManager().find(FillLavaModule.class).isEnabled())
                                    Harakiri.get().getModuleManager().find(FillLavaModule.class).toggle();

                                mc.player.sendChatMessage("#stop");

                                return;
                            }

                            // spam it every 5s.
                            BlockPos goalPos = new BlockPos(0, 121, 0);

                            switch (TunnelDirection) {
                                case PLUSX:
                                    goalPos.add(mc.player.getPosition().getX() - 20, 0, this.startingPos.getZ());
                                    break;
                                case MINUSX:
                                    goalPos.add(mc.player.getPosition().getX() + 20, 0, this.startingPos.getZ());
                                    break;
                                case PLUSZ:
                                    goalPos.add(this.startingPos.getX(), 0, mc.player.getPosition().getZ() - 20);
                                    break;
                                case MINUSZ:
                                    goalPos.add(this.startingPos.getX(), 0, mc.player.getPosition().getZ() + 20);
                                    break;
                            }

                            if (baritoneTimer.passed(5000)) {
                                baritoneTimer.reset();

                                if (baritoneSwitchBool)
                                    mc.player.sendChatMessage("#goto " + Integer.toString(goalPos.getX()) + " " + Integer.toString(goalPos.getY()) + " " + Integer.toString(goalPos.getZ()));
                                else {
                                    mc.player.sendChatMessage("#stop");
                                    breakFixAhead();
                                }

                                baritoneSwitchBool = !baritoneSwitchBool;

                                Harakiri.get().logChat("toggling baritone");
                            }

                            // check if we are back on the hw
                            // we are y=121
                            switch (TunnelDirection) {
                                case PLUSX:
                                case MINUSX:
                                    if (new BlockPos(mc.player.getPositionVector()).getZ() != this.startingPos.getZ()) {
                                        return;
                                    }
                                    break;
                                case PLUSZ:
                                case MINUSZ:
                                    if (new BlockPos(mc.player.getPositionVector()).getX() != this.startingPos.getX()) {
                                        return;
                                    }
                                    break;
                            }

                            if (mc.player.getPosition().getY() != 121 || mc.world.getBlockState(new BlockPos(mc.player.getPositionVector())).getMaterial() == Material.LAVA || mc.world.getBlockState(new BlockPos(mc.player.getPositionVector()).up()).getMaterial() == Material.LAVA){
                                this.baritoneReachedTimer.reset();
                                return;
                            } else {
                                if (!this.baritoneReachedTimer.passed(3000)) {
                                    this.nukerStatus = "baritone grace period of 3000ms";
                                    return;
                                }
                            }

                            // PASS!
                            isBaritoneRunning = false;
                            mc.player.sendChatMessage("#stop");

                            // set the yaw
                            mc.player.rotationYaw = yaw;

                            Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                            Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                            Harakiri.get().getModuleManager().find(RotationLock.class).toggle();

                            ((ScaffoldModule) Harakiri.get().getModuleManager().find(ScaffoldModule.class)).scaffoldStatus = "idle";
                            ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(lastTimerValue);
                            timerset = false;

                            return;
                        }

                        boolean invalidXZ = false;
                        switch (TunnelDirection) {
                            case PLUSX:
                            case MINUSX:
                                if (new BlockPos(mc.player.getPositionVector()).getZ() != this.startingPos.getZ()) {
                                    invalidXZ = true;
                                }
                                break;
                            case PLUSZ:
                            case MINUSZ:
                                if (new BlockPos(mc.player.getPositionVector()).getX() != this.startingPos.getX()) {
                                    invalidXZ = true;
                                }
                                break;
                        }

                        if (invalidXZ || mc.player.getPositionVector().y != 121) {
                            this.nukerStatus = "Illegal position, trying baritone";

                            if (!isBaritoneRunning && illegalPosTimer.passed(500)) {
                                isBaritoneRunning = true;
                                Harakiri.get().logChat("Illegal position! Baritone, help!");

                                if (!timerset) {
                                    lastTimerValue = ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.getValue();
                                    ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(1.f);
                                    timerset = true;
                                }

                                Harakiri.get().getModuleManager().find(ScaffoldModule.class).toggle();
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                                Harakiri.get().getModuleManager().find(RotationLock.class).toggle();

                                mc.player.sendChatMessage("#allowBreak true");
                                mc.player.sendChatMessage("#allowPlace true");

                                Harakiri.get().getRotationManager().setPlayerYaw(-mc.player.rotationYaw);
                                
                                baritoneSwitchBool = false;
                            }

                            return;
                        } else {
                            illegalPosTimer.reset();
                        }

                        if (isBaritoneRunning) {
                            this.nukerStatus = ChatFormatting.RED + "Paused by baritone recovery";
                            return;
                        }

                        if (mc.player.motionY < -0.2f && mc.player.posY == 121) {
                            Harakiri.get().logChat("Bugged 121 falling");
                            if (this.fallingTimer.passed(2000)) {
                                this.needsToDisconnect = true;
                                this.fallingTimer.reset();
                                return;
                            }
                        } else {
                            this.fallingTimer.reset();
                        }

                        lastStuckTimer.reset();

                        // check if we didnt miss any shulkers
                        checkForShulkersOnGround();

                        for (long y = Math.round(mc.player.posY - 1) + 2; y > Math.round(mc.player.posY - 1); y -= 1.0D) {
                            for (long x = (long)(mc.player.posX + 1 - (plusX ? 0 : lengthX)); x < (long)(mc.player.posX + (plusX ? lengthX : 0) + 1); x += 1.0D) {
                                for (long z = (long)(mc.player.posZ + 1 - (plusZ ? 0 : lengthZ)); z < (long)(mc.player.posZ + (plusZ ? lengthZ : 0) + 1); z += 1.0D) {
                                    final BlockPos blockPos = new BlockPos(x, y, z);
                                    breakCreativeBlock(blockPos, false);

                                    this.nukerStatus = "Gaming: " + Integer.toString((int)x) + ", " + Integer.toString((int)y) + ", " + Integer.toString((int)z);
                                }
                            }
                        }

                        // The nuker sometimes gets stuck on one block in front of the player, rounding error?
                        breakFixAhead();
                        

                    } else {
                        for (double y = Math.round(mc.player.posY - 1) + this.vDistance.getValue(); y > Math.round(mc.player.posY - 1); y -= 1.0D) {
                            for (double x = mc.player.posX - this.hDistance.getValue(); x < mc.player.posX + this.hDistance.getValue(); x += 1.0D) {
                                for (double z = mc.player.posZ - this.hDistance.getValue(); z < mc.player.posZ + this.hDistance.getValue(); z += 1.0D) {
                                    final BlockPos blockPos = new BlockPos(x, y, z);
                                    
                                    breakCreativeBlock(blockPos, false);
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

    private double distance(Vec3d a, Vec3d b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
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

    private boolean needsToStopToBlock(boolean plusX, boolean plusZ, int lengthX, int lengthZ) {
        final Minecraft mc = Minecraft.getMinecraft();

        ((ScaffoldModule) Harakiri.get().getModuleManager().find(ScaffoldModule.class)).scaffoldStatus = "idle";

            //                                  5s of a timeout
        if (this.scaffoldCompat.getValue() && !lastStuckTimer.passed(5000)) {
            for (double x = mc.player.posX + 1 - (plusX ? 0 : lengthX); x < mc.player.posX + (plusX ? lengthX : 0) + 1; x += 1.0D) {
                for (double z = mc.player.posZ + 1 - (plusZ ? 0 : lengthZ); z < mc.player.posZ + (plusZ ? lengthZ : 0) + 1; z += 1.0D) {

                    final BlockPos blockPos = new BlockPos(x, mc.player.getPositionVector().y - 1, z);
                    final Block block = BlockUtil.getBlock(blockPos);

                    if (block == Blocks.AIR) {
                        this.nukerStatus = "Stopped by scaffold (" + Integer.toString((int)x) + ", " + Integer.toString((int)mc.player.getPositionVector().y - 1) + ", " + Integer.toString((int)z) + ").";
                        ((ScaffoldModule) Harakiri.get().getModuleManager().find(ScaffoldModule.class)).scaffoldStatus = ChatFormatting.GREEN + "placing";
                        
                        if (!timerset) {
                            lastTimerValue = ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.getValue();
                            ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(1.f);
                            timerset = true;

                            if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                                Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                        }
                        
                        return true;
                    }
                }
            }
        }

            //              fillcompat sometimes gets stuck so if we've been waiting for 2 seconds ignore this check
        if (this.fillCompat.getValue() && !lastStuckTimer.passed(2000)) {
            if (((FillLavaModule)Harakiri.get().getModuleManager().find(FillLavaModule.class)).isWorking) {
                this.nukerStatus = "Stopped by fill lava";

                if (!timerset) {
                    lastTimerValue = ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.getValue();
                    ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(1.f);
                    timerset = true;

                    if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                        Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();
                }

                return true;
            }
        }

        ((ScaffoldModule) Harakiri.get().getModuleManager().find(ScaffoldModule.class)).scaffoldStatus = "idle";
        ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(lastTimerValue);
        timerset = false;

        if (!Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled() && !isBaritoneRunning)
            Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();

        // no timeout for eating.
        if (this.eatCompat.getValue()) {
            final AutoEatModule aem = ((AutoEatModule)Harakiri.get().getModuleManager().find(AutoEatModule.class));
            if (mc.player.getFoodStats().getFoodLevel() < aem.hunger.getValue() || mc.player.getHealth() < aem.health.getValue()) {
                this.nukerStatus = "Stopped by autoeat";

                if (Harakiri.get().getModuleManager().find(AutoWalkModule.class).isEnabled())
                    Harakiri.get().getModuleManager().find(AutoWalkModule.class).toggle();

                return true;
            }
        }
        

        return false;
    }

    public void breakPacketBlock(BlockPos blockPos) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (!mc.world.getBlockState(blockPos).isFullBlock())
            return;

        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
        mc.playerController.onPlayerDestroyBlock(blockPos);
    }

    public void breakCreativeBlock(BlockPos blockPos, boolean force) {
        final Minecraft mc = Minecraft.getMinecraft();
        final Block block = BlockUtil.getBlock(blockPos);
        if (block == Blocks.AIR || (!mc.world.getBlockState(blockPos).isFullBlock() && !force) || block == Blocks.BEDROCK)
            return;

        if ((this.flatten.getValue() && blockPos.getY() < mc.player.posY) || force)
            return;

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
            // if (distanceSqHitVec >= distanceSqPosVec)
            // continue;

            // face block
            final float[] rotations = EntityUtil.getRotations(hitVec.x, hitVec.y, hitVec.z);
            Harakiri.get().getRotationManager().setPlayerRotations(rotations[0], rotations[1]);

            // damage block
            if (mc.playerController.onPlayerDamageBlock(blockPos, side)) {
                mc.player.swingArm(EnumHand.MAIN_HAND);

            }
        }
    }

    private void breakFixAhead() {
        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos blockPosFix;
        BlockPos blockPosFix2;
        switch (TunnelDirection) {
            case PLUSX:
                blockPosFix = new BlockPos(mc.player.getPositionVector().x + 1, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z);
                blockPosFix2 = new BlockPos(mc.player.getPositionVector().x + 1, mc.player.getPositionVector().y + 1,
                        mc.player.getPositionVector().z);

                breakPacketBlock(blockPosFix);
                breakPacketBlock(blockPosFix2);
                break;
            case MINUSX:
                blockPosFix = new BlockPos(mc.player.getPositionVector().x - 1, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z);
                blockPosFix2 = new BlockPos(mc.player.getPositionVector().x - 1, mc.player.getPositionVector().y + 1,
                        mc.player.getPositionVector().z);

                breakPacketBlock(blockPosFix);
                breakPacketBlock(blockPosFix2);
                break;
            case PLUSZ:
                blockPosFix = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z + 1);
                blockPosFix2 = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y + 1,
                        mc.player.getPositionVector().z + 1);

                breakPacketBlock(blockPosFix);
                breakPacketBlock(blockPosFix2);
                break;
            case MINUSZ:
                blockPosFix = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z - 1);
                blockPosFix2 = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y + 1,
                        mc.player.getPositionVector().z - 1);

                breakPacketBlock(blockPosFix);
                breakPacketBlock(blockPosFix2);
                break;
        }
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


    private enum UnpackStep {
        PLACESHULKER,
        OPENSHULKER,
        TAKEPICKAXE,
        CHECKIFEMPTY,
        BREAKSHULKER,
        PICKUPSHULKER,
        CONTINUERUN
    }

    private BlockPos shulkerPosition = new BlockPos(0,0,0);
    private boolean shulkerEmpty = false;
    private int shulkerInvSlot = -1;
    private boolean unpackingInProgress = false;
    private boolean waiting = false;
    private Timer waittimer = new Timer();
    private final RotationTask rotationForPickup = new RotationTask("pickaxexd", 2);
    private int pickedUpPickSlot = -1;
    private Timer timeoutThings = new Timer();
    private int counterForPickup = 0;
    private int counterForPickedUpAlready = 0;
    private int shulkersInHotbar = 0;
    private Timer pickupTimeoutTimer = new Timer();
    private int offsetSearchingShulker = 1;
    private Timer openingShulkerTimer = new Timer();
    private Timer breakShulkerTimer = new Timer();

    public void unpackNewPickaxe() {
        // oh boy. here we go.

        if (!unpackingInProgress)
            waittimer.reset();

        unpackingInProgress = true;
        if (!timerset) {
            lastTimerValue = ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.getValue();
            ((TimerModule) Harakiri.get().getModuleManager().find(TimerModule.class)).speed.setValue(1.f);
            timerset = true;
        }

        Harakiri.get().getModuleManager().find(AutoWalkModule.class).setEnabled(false);
        Harakiri.get().getModuleManager().find(AutoWalkModule.class).onDisable();
        Harakiri.get().getModuleManager().find(RotationLock.class).setEnabled(false);
        Harakiri.get().getModuleManager().find(RotationLock.class).onDisable();

        switch(currentUnpackStep) {
            case PLACESHULKER:
                this.nukerStatus = "UNPACKING: Placing a shulker";
                if (timeoutThings.passed(500)) {
                    if (!waiting && placeShulker()) {
                        waiting = true;
                    }
                    timeoutThings.reset();
                }

                if (waiting) {
                    this.nukerStatus = "UNPACKING: Placing a shulker " + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(100)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.OPENSHULKER;
                        waiting = false;
                        openingShulkerTimer.reset();
                    }
                }
            break;
            case OPENSHULKER:
                this.nukerStatus = "UNPACKING: Opening the shulker";
                if (!waiting && openShulker())
                    waiting = true;

                if (waiting) {
                    this.nukerStatus = "UNPACKING: Opening the shulker" + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(500)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.TAKEPICKAXE;
                        waiting = false;
                    }
                }
            break;
            case TAKEPICKAXE:
                this.nukerStatus = "UNPACKING: Taking a pickaxe";
                if (timeoutThings.passed(500)) {
                    if (!waiting && takePickaxe()) {
                        waiting = true;
                    }
                    timeoutThings.reset();
                }
                
                if (waiting) {
                    this.nukerStatus = "UNPACKING: Taking a pickaxe" + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(100)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.CHECKIFEMPTY;
                        waiting = false;
                    }
                }
            break;
            case CHECKIFEMPTY:
                this.nukerStatus = "UNPACKING: Checking if empty";
                if (!waiting)
                    checkIfEmpty();
                waiting = true;
                if (waiting) {
                    this.nukerStatus = "UNPACKING: Checking if empty" + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(100)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.BREAKSHULKER;
                        waiting = false;
                        breakShulkerTimer.reset();
                    }
                }
            break;
            case BREAKSHULKER:
                this.nukerStatus = "UNPACKING: Breaking shulker";
                if (!waiting && breakShulker())
                    waiting = true;
                
                if (waiting) {
                    this.nukerStatus = "UNPACKING: Breaking shulker" + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(100)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.PICKUPSHULKER;
                        this.pickupTimeoutTimer.reset();
                        waiting = false;
                    }
                }
            break;
            case PICKUPSHULKER:
                this.nukerStatus = "UNPACKING: Picking up the shulker";
                if (timeoutThings.passed(50)) {
                    if (!waiting && pickUpShulker())
                        waiting = true;

                    timeoutThings.reset();
                }

                if (waiting) {
                    this.nukerStatus = "UNPACKING: Picking up the shulker" + ChatFormatting.GRAY + "(waiting)";
                    if (waittimer.passed(100)) {
                        waittimer.reset();
                        currentUnpackStep = UnpackStep.CONTINUERUN;
                        waiting = false;
                    }
                }
            break;
            case CONTINUERUN:
                this.nukerStatus = "UNPACKING: Continuing the run";
                continueRun();
                currentUnpackStep = UnpackStep.PLACESHULKER;
                unpackingInProgress = false;
                Harakiri.get().getModuleManager().find(AutoWalkModule.class).setEnabled(true);
                Harakiri.get().getModuleManager().find(AutoWalkModule.class).onEnable();
                Harakiri.get().getModuleManager().find(RotationLock.class).setEnabled(true);
                Harakiri.get().getModuleManager().find(RotationLock.class).onEnable();
                waiting = false;
                pickedUpPickSlot = -1;
                counterForPickup = 0;
                counterForPickedUpAlready = 0;
                shulkerEmpty = false;
                shulkerInvSlot = -1;
                waittimer.reset();
            break;
        }

        
    }

    private void continueRun() {
        final Minecraft mc = Minecraft.getMinecraft();

        // set the pitch and yaw
        float yaw = 0;
        switch (TunnelDirection) {
            case PLUSX:
                yaw = -90;
                break;
            case MINUSX:
                yaw = 90;
                break;
            case MINUSZ:
                yaw = 180;
                break;
            case PLUSZ:
                yaw = 0;
                break;
        }

        // now set the rotation
        mc.player.setPositionAndRotation(mc.player.getPositionVector().x, mc.player.getPositionVector().y, mc.player.getPositionVector().z, yaw, 0);
    }

    private boolean pickUpShulker() {
        if (shulkerEmpty) {
            lastShulkerAbandoned.reset();
            return true;
        }

        if (pickupTimeoutTimer.passed(30000)) {
            // if 30s passed, we are stuck. too bad, i guess.
            return true;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        Block blockatplace = BlockUtil.getBlock(shulkerPosition);
        if ((blockatplace instanceof BlockShulkerBox)) {
            mc.playerController.onPlayerDamageBlock(shulkerPosition, mc.player.getHorizontalFacing());
            mc.player.swingArm(EnumHand.MAIN_HAND);
            return false;
        }

        counterForPickup++;

        if (mc.currentScreen instanceof GuiContainer)
            mc.player.closeScreen();

        mc.player.inventory.currentItem = shulkerInvSlot;
        mc.playerController.updateController();

        // count shulkers
        int countedShulkers = 0;
        ItemStack stack = mc.player.getHeldItemMainhand();

        for (int i = 0; i < 9 + 27; ++i) {
            stack = mc.player.inventory.getStackInSlot(i);

            if (stack.getItem() instanceof ItemShulkerBox && ((BlockShulkerBox) ((ItemShulkerBox) stack.getItem()).getBlock()).getColor() != EnumDyeColor.YELLOW) {
                countedShulkers++;
            }
        }
        
        if (countedShulkers > shulkersInHotbar && counterForPickedUpAlready < 30) {
            counterForPickedUpAlready += 1;
            Harakiri.get().logChat("Picked up wait: " + Integer.toString(counterForPickedUpAlready));
            return false;
        } else if (countedShulkers <= shulkersInHotbar) { counterForPickedUpAlready = 0; }

        if (counterForPickedUpAlready >= 30) {
            Harakiri.get().logChat("Picked up wait passed!");
            return true;
        } else if (counterForPickedUpAlready > 0){
            return false;
        }

        // find and update the shulker box coord
        Vec3d itemCoord = new Vec3d(0,0,0);
        for (Entity e : mc.world.getLoadedEntityList()) {
            if (!(e instanceof EntityItem))
                continue;

            EntityItem eitem = (EntityItem)e;

            if (eitem.getItem().getItem() instanceof ItemShulkerBox) {
                itemCoord = eitem.getPositionVector();
                break;
            }
        }

        if (itemCoord.x == 0 && itemCoord.y == 0 && itemCoord.z == 0 && counterForPickedUpAlready == 0) {
            Harakiri.get().logChat("No shulker on the ground? Falling back to place pos with random");
            if (TunnelDirection == TunnelDir.PLUSX || TunnelDirection == TunnelDir.MINUSX)
                itemCoord = new Vec3d(shulkerPosition.getX() + offsetSearchingShulker, shulkerPosition.getY(), shulkerPosition.getZ());
            else
                itemCoord = new Vec3d(shulkerPosition.getX(), shulkerPosition.getY(), shulkerPosition.getZ() + offsetSearchingShulker);
        }
        
        // we need to pick it up.

        Harakiri.get().logChat("Shulker found at X: " + Integer.toString((int)itemCoord.x) + " Y: " + Integer.toString((int)itemCoord.y) + " Z: " + Integer.toString((int)itemCoord.z));
        
        // "walk" =)
        float deltaX = 0;
        float deltaZ = 0;

        switch (TunnelDirection) {
            case PLUSX:
            case MINUSX:
                if (itemCoord.x - mc.player.posX > 0.5f)
                    deltaX = 0.05f;
                else if (itemCoord.x - mc.player.posX < -0.5f)
                    deltaX = -0.05f;
            break;
            case PLUSZ:
            case MINUSZ:
                if (itemCoord.z - mc.player.posZ > 0.5f)
                    deltaZ = 0.05f;
                else if (itemCoord.z - mc.player.posZ < -0.5f)
                    deltaZ = -0.05f;
            break;
        }

        mc.player.setPosition(mc.player.posX + deltaX, mc.player.posY, mc.player.posZ + deltaZ);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + deltaX, mc.player.posY, mc.player.posZ + deltaZ, true));

        // check if slot isnt obstructed by another item
        // only if delta 0
        if (!(mc.player.inventory.getStackInSlot(shulkerInvSlot).getItem() instanceof ItemShulkerBox) && mc.player.inventory.getStackInSlot(shulkerInvSlot).getItem() != null ) {
            if (counterForPickup == 10) {
                //                                                                                                                     set the pitch to 80 so we look down vvv
                mc.player.setPositionAndRotation(mc.player.getPositionVector().x, mc.player.getPositionVector().y, mc.player.getPositionVector().z, mc.player.rotationYaw, 80);
                mc.player.dropItem(true);
                offsetSearchingShulker = -offsetSearchingShulker;
            }
        }

        if (mc.player.inventory.getStackInSlot(shulkerInvSlot).isEmpty())
            counterForPickup = 0;

        if (counterForPickup >= 10) {
            counterForPickup = 0;
        }

        return false;
    }

    private boolean breakShulker() {

        final Minecraft mc = Minecraft.getMinecraft();

        if (breakShulkerTimer.passed(30000)) {
            this.needsToDisconnect = true;
        }

        if (mc.currentScreen instanceof GuiContainer)
            mc.player.closeScreen();

        // check if pick found
        boolean pickFound = false;
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!(stack.getItem() instanceof ItemPickaxe)) {
            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);

                if (stack.getItem() instanceof ItemPickaxe) {
                    pickFound = true;
                    break;
                }
            }
        }

        if (!pickFound) {
            this.currentUnpackStep = UnpackStep.OPENSHULKER;
            Harakiri.get().logChat("Trying to break but no pickaxe! (BUG THIS)");
            return false;
        }

        shulkersInHotbar = 0;
        
        for (int i = 0; i < 9 + 27; ++i) {
            stack = mc.player.inventory.getStackInSlot(i);

            if (stack.getItem() instanceof ItemShulkerBox && ((BlockShulkerBox) ((ItemShulkerBox) stack.getItem()).getBlock()).getColor() != EnumDyeColor.YELLOW) {
                shulkersInHotbar++;
            }
        }

        Block blockatplace = BlockUtil.getBlock(shulkerPosition);
        if (!(blockatplace instanceof BlockShulkerBox)) 
            return true;

        mc.player.inventory.currentItem = 2; // pickaxe slot
        mc.playerController.updateController();

        mc.playerController.onPlayerDamageBlock(shulkerPosition, mc.player.getHorizontalFacing());
        mc.player.swingArm(EnumHand.MAIN_HAND);
        return false;
    }

    private void checkIfEmpty() {
        final Minecraft mc = Minecraft.getMinecraft();

        GuiShulkerBox containergui = (GuiShulkerBox) Minecraft.getMinecraft().currentScreen;

        if (containergui == null) {
            Harakiri.get().logChat("Container gui null????");
            return;
        }

        // find a chest slot with a pickaxe
        int slotWithPick = -1;
        for (int slot = 0; slot < 27; slot++) {
            ItemStack itemStack = containergui.inventorySlots.getSlot(slot).getStack();
            if (itemStack.isEmpty())
                continue;

            if (itemStack.getItem() instanceof ItemPickaxe) {
                slotWithPick = slot;
                break;
            }
        }

        if (slotWithPick == -1) {
            shulkerEmpty = true;
            Harakiri.get().logChat("Shulker empty, abandoning.");
        } else {
            Harakiri.get().logChat("Pickaxe left in slot " + Integer.toString(slotWithPick));
        }
            

        // if empty put in a new shulker in its spot
        if (shulkerEmpty) {
            int shulkerSlot = -1;

            for (int i = 0; i < 27 + 27 + 9; i++) {
                ItemStack stack = containergui.inventorySlots.getSlot(i).getStack();
                if (stack.getItem() instanceof ItemShulkerBox && i != shulkerInvSlot && ((BlockShulkerBox)((ItemShulkerBox)stack.getItem()).getBlock()).getColor() == EnumDyeColor.LIGHT_BLUE) {
                    shulkerSlot = i;
                    break;
                }
            }

            Harakiri.get().logChat("New shulker (to replace) at " + Integer.toString(shulkerSlot));

            if (shulkerSlot != -1) {
                // move the shulker to our new slot for it
                mc.playerController.windowClick(containergui.inventorySlots.windowId, shulkerSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(containergui.inventorySlots.windowId, shulkerInvSlot + 27 + 27, 0, ClickType.PICKUP, mc.player);
            }
        }

        mc.player.closeScreen();
    }

    private boolean takePickaxe() {
        final Minecraft mc = Minecraft.getMinecraft();

        pickedUpPickSlot = -1;

        if (Minecraft.getMinecraft().currentScreen == null) {
            Harakiri.get().logChat("Container gui null? (Lag?) Opening again.");
            this.currentUnpackStep = UnpackStep.OPENSHULKER;
            return false;
        }

        GuiShulkerBox containergui = (GuiShulkerBox) Minecraft.getMinecraft().currentScreen;

        if (pickedUpPickSlot == -1) {
            // find a chest slot with a pickaxe
            int slotWithPick = -1;
            for (int slot = 0; slot < 27; slot++) {
                ItemStack itemStack = containergui.inventorySlots.getSlot(slot).getStack();
                if (itemStack.isEmpty())
                    continue;

                if (itemStack.getItem() instanceof ItemPickaxe) {
                    slotWithPick = slot;
                    break;
                }
            }

            if (slotWithPick != -1)
                Harakiri.get().logChat("Pick slot in shulker: " + Integer.toString(slotWithPick));

            if (slotWithPick == -1) {
              //  Harakiri.get().logChat("No pick???");
                return false;
            }

            mc.playerController.windowClick(containergui.inventorySlots.windowId, slotWithPick, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(containergui.inventorySlots.windowId, 29 + 27, 0, ClickType.PICKUP, mc.player);

            pickedUpPickSlot = slotWithPick;

            return true;
        }

        return false;
    }

    private boolean openShulker() {
        final Minecraft mc = Minecraft.getMinecraft();
        final EnumFacing facing = mc.player.getHorizontalFacing();
        if (mc.currentScreen instanceof GuiContainer) 
            return true;

        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(shulkerPosition, facing, EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

        if (openingShulkerTimer.passed(12000)) {
            // if 8s passed, place it again. Probably didnt register.

            mc.player.inventory.currentItem = shulkerInvSlot;
            mc.playerController.updateController();

            if (BlockInteractionUtil.place(shulkerPosition, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.Placed) {
                Harakiri.get().logChat("Placing shulker again because can't open");

                openingShulkerTimer.reset();
            }
        }

        return false;
    }

    private EnumFacing calcSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos sideOffset = pos.offset(side);
            IBlockState offsetState = Minecraft.getMinecraft().world.getBlockState(sideOffset);
            if (!offsetState.getBlock().canCollideCheck(offsetState, false))
                continue;
            if (!offsetState.getMaterial().isReplaceable())
                return side;
        }
        return null;
    }

    private boolean placeShulker() {
        // place the shulker 2 blocks behind.
        final Minecraft mc = Minecraft.getMinecraft();
        final EnumFacing facing = mc.player.getHorizontalFacing();

        // get the place pos
        BlockPos toPlace = mc.player.getPosition();
        switch (TunnelDirection) {
            case PLUSX:
                toPlace = new BlockPos(mc.player.getPositionVector().x - 2, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z);
                break;
            case MINUSX:
                toPlace = new BlockPos(mc.player.getPositionVector().x + 2, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z);
                break;
            case PLUSZ:
                toPlace = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z - 2);
                break;
            case MINUSZ:
                toPlace = new BlockPos(mc.player.getPositionVector().x, mc.player.getPositionVector().y,
                        mc.player.getPositionVector().z + 2);
                break;
        }

        shulkerPosition = toPlace;

        // check if placed
        Block blockatplace = BlockUtil.getBlock(toPlace);
        if (blockatplace instanceof BlockShulkerBox)
            return true;

        // get the first shulker into our hand
        if (shulkerInvSlot == -1) {
            ItemStack stack = mc.player.getHeldItemMainhand();

            for (int i = 0; i < 9; ++i) {
                stack = mc.player.inventory.getStackInSlot(i);

                if (stack.getItem() instanceof ItemShulkerBox && ((BlockShulkerBox)((ItemShulkerBox)stack.getItem()).getBlock()).getColor() != EnumDyeColor.YELLOW) {
                    Harakiri.get().logChat("Shulker found with a color " + ((BlockShulkerBox)((ItemShulkerBox)stack.getItem()).getBlock()).getColor().name());
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    shulkerInvSlot = i;
                    break;
                }
            }

            if (!(stack.getItem() instanceof ItemShulkerBox)) {
                this.nukerStatus = "No Shulkers!";
                return false;
            }
        }

        // place it
        if (BlockInteractionUtil.place(toPlace, 5.0f, false, false, true) == BlockInteractionUtil.PlaceResult.Placed) {
            Harakiri.get().logChat("Placing!");
        }

        return false;
    }

    public int getFoodCount() {
        int food = 0;

        if (Minecraft.getMinecraft().player == null)
            return food;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && this.isFoodItem(stack.getItem())) {
                food += stack.getCount();
            }
        }

        return food;
    }

    private boolean isFoodItem(Item item) {
        if (!(item instanceof ItemFood))
            return false; // is not of ItemFood class

        if (item == Items.CHORUS_FRUIT || item == Items.ROTTEN_FLESH || item == Items.POISONOUS_POTATO
                || item == Items.SPIDER_EYE)
            return false;

        return true;
    }

    private void disconnectScenarios() {
        final Minecraft mc = Minecraft.getMinecraft();

        int totems = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }

        int food = getFoodCount();

        if (totems == 0 || food == 0) {
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));

            if (!shownMsgBox) {
                new Thread(() -> {
                    JOptionPane.showConfirmDialog(null, "Harakiri: Disconnect on no food/totems");
                    shownMsgBox = false;
                }).start();
                shownMsgBox = true;
            }

            ReconnectModule rcm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
            if (rcm.isEnabled())
                rcm.auto.setValue(false);
        }

        final ItemStack headItem = ((EntityLivingBase) mc.player).getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        final ItemStack chestItem = ((EntityLivingBase) mc.player).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        final ItemStack legItem = ((EntityLivingBase) mc.player).getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        final ItemStack feetItem = ((EntityLivingBase) mc.player).getItemStackFromSlot(EntityEquipmentSlot.FEET);

        int lowItems = 0;
        if (headItem.getItemDamage() > headItem.getMaxDamage() * 0.8f && headItem.isItemStackDamageable()) {
            lowItems += 1;
        }
        if (chestItem.getItemDamage() > chestItem.getMaxDamage() * 0.8f && headItem.isItemStackDamageable()
                && chestItem.getItem() != Items.ELYTRA) {
            lowItems += 1;
        }
        if (legItem.getItemDamage() > legItem.getMaxDamage() * 0.8f && headItem.isItemStackDamageable()) {
            lowItems += 1;
        }
        if (feetItem.getItemDamage() > feetItem.getMaxDamage() * 0.8f && headItem.isItemStackDamageable()) {
            lowItems += 1;
        }

        if (lowItems > 0) {
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketHeldItemChange(420));

            if (!shownMsgBox) {
                shownMsgBox = true;
                new Thread(() -> {
                    JOptionPane.showConfirmDialog(null, "Harakiri: Disconnect on low armor");
                    shownMsgBox = false;
                }).start();
            }

            ReconnectModule rcm = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
            if (rcm.isEnabled())
                rcm.auto.setValue(false);
        }
    }

    private void checkForShulkersOnGround() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (!(((HighwayAutoUnpackModule)(Harakiri.get().getModuleManager().find(HighwayAutoUnpackModule.class))).pickupLost.getValue()))
            return;

        Vec3d itemCoord = new Vec3d(0, 0, 0);
        for (Entity e : mc.world.getLoadedEntityList()) {
            if (!(e instanceof EntityItem))
                continue;

            EntityItem eitem = (EntityItem) e;

            if (eitem.getItem().getItem() instanceof ItemShulkerBox) {
                itemCoord = eitem.getPositionVector();
                break;
            }
        }

        if (itemCoord.y == 0 && itemCoord.x == 0 && itemCoord.z == 0)
            return; // no shulker

        if (!this.lastShulkerAbandoned.passed(25000))
            return; // empty shulker


        // uh oh! we dropped a parcel!
        this.unpackingInProgress = true;
        this.currentUnpackStep = UnpackStep.PICKUPSHULKER;
        this.pickupTimeoutTimer.reset();
        this.shulkerEmpty = false;

        ItemStack stack;
        for (int i = 8; i > 0; --i) {
            stack = mc.player.inventory.getStackInSlot(i);

            if (!(stack.getItem() instanceof ItemShulkerBox) && !(stack.getItem() instanceof ItemAppleGold)) {
                this.shulkerInvSlot = i;
                break;
            }
        }

        int count = 0;
        for (int i = 0; i < 9 + 27; ++i) {
            stack = mc.player.inventory.getStackInSlot(i);

            if (stack.getItem() instanceof ItemShulkerBox && ((BlockShulkerBox) ((ItemShulkerBox) stack.getItem()).getBlock()).getColor() != EnumDyeColor.YELLOW) {
                count++;
            }
        }

        this.shulkersInHotbar = count;

        this.waittimer.reset();
        this.waiting = false;
    }
}
