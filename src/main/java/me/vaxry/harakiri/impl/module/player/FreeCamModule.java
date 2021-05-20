package me.vaxry.harakiri.impl.module.player;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.gui.EventRenderHelmet;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.player.*;
import me.vaxry.harakiri.framework.event.render.EventRenderOverlay;
import me.vaxry.harakiri.framework.event.world.EventAddCollisionBox;
import me.vaxry.harakiri.framework.event.world.EventLiquidCollisionBB;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.event.world.EventSetOpaqueCube;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.Vec3d;


public final class FreeCamModule extends Module {

    private Entity riding;
    private EntityOtherPlayerMP entity;
    private Vec3d position;
    private float yaw;
    private float pitch;
    private int iThirdperson;

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Speed of freecam flight.", 1.0f, 0.0f, 2.0f, 0.1f);
    //public final Value<Boolean> view = new Value<Boolean>("3D", new String[]{"View"}, "The old Nodus client style free-cam, kind of like an elytra. (Hold forward key & move the mouse to turn)", false);
    public final Value<Boolean> packet = new Value<Boolean>("Packet", new String[]{"Pack"}, "Disables any player position or rotation packets from being sent during freecam if enabled.", true);
    public final Value<Boolean> allowDismount = new Value<Boolean>("AllowDismount", new String[]{"Dismount", "Dis", "AllowDis"}, "Allow dismounting of the riding entity.", true);

    public FreeCamModule() {
        super("FreeCam", new String[]{"FreeCamera"}, "Allows you to noclip.", "NONE", -1, ModuleType.PLAYER);
        this.onDisable();
        this.setEnabled(false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            iThirdperson = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 0;
            this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(mc.player);
            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
                this.entity.startRiding(this.riding);
            } else {
                this.riding = null;
            }
            this.entity.rotationYaw = mc.player.rotationYaw;
            this.entity.rotationYawHead = mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(mc.player.inventory);
            this.entity.setFlag(7, mc.player.isElytraFlying());
            this.entity.rotateElytraX = mc.player.rotateElytraX;
            this.entity.rotateElytraY = mc.player.rotateElytraY;
            this.entity.rotateElytraZ = mc.player.rotateElytraZ;
            mc.world.addEntityToWorld(69420, this.entity);
            this.position = mc.player.getPositionVector();
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (this.riding != null) {
                mc.player.startRiding(this.riding, true);
            }
            if (this.entity != null) {
                mc.world.removeEntity(this.entity);
            }
            if (this.position != null) {
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            }
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
            mc.player.motionX = 0;
            mc.player.motionY = 0;
            mc.player.motionZ = 0;
            mc.gameSettings.thirdPersonView = iThirdperson;
        }
    }

    Attender<EventMove> onMove = new Attender<>(EventMove.class, event -> Minecraft.getMinecraft().player.noClip = true);

    Attender<EventUpdateWalkingPlayer> onUpdateWalkingPlayer = new Attender<>(EventUpdateWalkingPlayer.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.player.setVelocity(0, 0, 0);
            mc.player.renderArmPitch = 5000;
            mc.player.jumpMovementFactor = this.speed.getValue();

            final double[] dir = MathUtil.directionSpeed(this.speed.getValue());

            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            } else {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            mc.player.setSprinting(false);

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motionY += this.speed.getValue();
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motionY -= this.speed.getValue();
            }
        }
    });

    Attender<EventSendPacket> onPacketSend = new Attender<>(EventSendPacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (Minecraft.getMinecraft().world != null) {
                if (!this.allowDismount.getValue()) {
                    if (event.getPacket() instanceof CPacketInput) {
                        event.setCanceled(true);
                    }
                    if (event.getPacket() instanceof CPacketEntityAction) {
                        CPacketEntityAction packetEntityAction = (CPacketEntityAction) event.getPacket();
                        if (packetEntityAction.getAction().equals(CPacketEntityAction.Action.START_SNEAKING)) {
                            event.setCanceled(true);
                        }
                    }
                }

                if (this.packet.getValue()) {
                    if (event.getPacket() instanceof CPacketPlayer) {
                        event.setCanceled(true);
                    }
                } else {
                    if (!(event.getPacket() instanceof CPacketUseEntity) && !(event.getPacket() instanceof CPacketPlayerTryUseItem) && !(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) && !(event.getPacket() instanceof CPacketPlayer) && !(event.getPacket() instanceof CPacketVehicleMove) && !(event.getPacket() instanceof CPacketChatMessage) && !(event.getPacket() instanceof CPacketKeepAlive)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    });

    Attender<EventLoadWorld> onLoadWorld = new Attender<>(EventLoadWorld.class, event -> {
        this.setEnabled(false);
        this.onDisable();
    });

    Attender<EventReceivePacket> onPacketReceive = new Attender<>(EventReceivePacket.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSetPassengers) {
                final SPacketSetPassengers packet = (SPacketSetPassengers) event.getPacket();
                final Entity riding = Minecraft.getMinecraft().world.getEntityByID(packet.getEntityId());

                if (riding != null && riding == this.riding) {
                    this.riding = null;
                }
            }
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                if (this.packet.getValue()) {
                    if (this.entity != null) {
                        this.entity.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
                    }
                    this.position = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Minecraft.getMinecraft().player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                    event.setCanceled(true);
                } else {
                    event.setCanceled(true);
                }
            }
        }
    });

    Attender<EventAddCollisionBox> onCollideWithBlock = new Attender<>(EventAddCollisionBox.class, event -> {
        if (event.getEntity() == Minecraft.getMinecraft().player) {
            event.setCanceled(true);
        }
    });

    Attender<EventLiquidCollisionBB> onLiquidCollisionBB = new Attender<>(EventLiquidCollisionBB.class, event -> {
        event.setBoundingBox(Block.NULL_AABB);
        event.setCanceled(true);
    });

    Attender<EventSetOpaqueCube> onSetOpaqueCube = new Attender<>(EventSetOpaqueCube.class, event -> {
        event.setCanceled(true);
    });

    Attender<EventRenderOverlay> onRenderOverlay = new Attender<>(EventRenderOverlay.class, event -> {
        event.setCanceled(true);
    });

    Attender<EventRenderHelmet> onRenderHelmet = new Attender<>(EventRenderHelmet.class, event -> {
        event.setCanceled(true);
    });

    Attender<EventPushOutOfBlocks> onPushOutOfBlocks = new Attender<>(EventPushOutOfBlocks.class, event -> {
        event.setCanceled(true);
    });

    Attender<EventPushedByWater> onPushedByWater = new Attender<>(EventPushedByWater.class, event -> {
        event.setCanceled(true);
    });

    Attender<EventApplyCollision> onApplyCollision = new Attender<>(EventApplyCollision.class, event -> {
        event.setCanceled(true);
    });
}
