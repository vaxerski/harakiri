package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakeLagModule extends Module {

    private EntityOtherPlayerMP entity;
    private Queue<Packet> packets = new ConcurrentLinkedQueue();
    private final Timer timer = new Timer();

    private boolean isLagging = false;
    private final Timer lagtimer = new Timer();

    private boolean isLow = false;

    private final Timer jointimer = new Timer();
    private boolean isJoin = false;

    private boolean isElytra = false;

    private Packet lastPacket = null;

    // Values
    public final Value<Boolean> stopwhenlag = new Value<Boolean>("StopWhenLag", new String[]{"SWL", "stoplag", "S"}, "Stops when the server lags.", false);
    public final Value<Integer> ms = new Value<Integer>("Milliseconds", new String[]{"ms"}, "Amount of ms to hold the packets for.", 0, 0, 10000, 100);
    public final Value<Boolean> drawFakePlayer = new Value<Boolean>("DrawFakePlayer", new String[]{"DFP", "draw"}, "Draws a fake player at last packet.", false);
    public final Value<Boolean> stopwhenlow = new Value<Boolean>("StopWhenLow", new String[]{"SWLow", "stopwhenlow"}, "Stops the lag when on low health.", false);
    public final Value<Boolean> stoponjoin = new Value<Boolean>("StopOnJoin", new String[]{"SOJ", "stoponjoin"}, "Stops the lag when joining a world for 2s.", false);
    public final Value<Boolean> stoponelytra = new Value<Boolean>("StopOnElytra", new String[]{"SOE", "stoponelytra"}, "Stops the lag when trying to take off with elytra.", false);


    public FakeLagModule() {
        super("FakeLag", new String[]{"FL (True)"}, "Limits outgoing packets", "NONE", -1, Module.ModuleType.PLAYER);
        this.timer.reset();
        this.lagtimer.reset();
        this.jointimer.reset();
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event){
        this.jointimer.reset();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // Reset the timer for accurate lagpackets
        this.timer.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Delete the FakePlayer if exists
        if (this.entity != null) {
            Minecraft.getMinecraft().world.removeEntity(this.entity);
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {

        // Avoid sendPacket() Listener loop a few lines later.
        if(lastPacket == event.getPacket())
            return;

        if(Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null || this.ms.getValue() == 0) {
            this.timer.reset();
            if (this.entity != null) {
                Minecraft.getMinecraft().world.removeEntity(this.entity);
            }
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        final Item chestItem = ((EntityLivingBase)mc.player).getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();
        if(this.stoponelytra.getValue() && chestItem == Items.ELYTRA && Keyboard.isKeyDown(Keyboard.KEY_SPACE))
            isElytra = true;
        else isElytra = false;

        if(isElytra) {
            if (this.entity != null) {
                Minecraft.getMinecraft().world.removeEntity(this.entity);
            }
            return;
        }

        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
        if(Minecraft.getMinecraft().player.getHealth() < 8 && stopwhenlow.getValue())
            isLow = true;
        else isLow = false;

        if(((System.currentTimeMillis() - this.jointimer.getTime()) / 1000.0f) % 60.0f < 2.0f && this.stoponjoin.getValue())
            isJoin = true;
        else isJoin = false;

        if(seconds > (float)(this.ms.getValue())/1000.0f || isLagging || isLow || isJoin){

            //Send packets

            if (this.entity != null) {
                Minecraft.getMinecraft().world.removeEntity(this.entity);
            }
            if (this.packets.size() > 0) {
                for (Packet packet : this.packets) {
                    lastPacket = packet;
                    Minecraft.getMinecraft().player.connection.sendPacket(packet);
                }
                this.packets.clear();
            }

            if (mc.world != null && drawFakePlayer.getValue() && mc.gameSettings.thirdPersonView != 0) {
                this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
                this.entity.copyLocationAndAnglesFrom(mc.player);
                this.entity.rotationYaw = mc.player.rotationYaw;
                this.entity.rotationYawHead = mc.player.rotationYawHead;
                this.entity.inventory.copyInventory(mc.player.inventory);
                this.entity.isAirBorne = mc.player.isAirBorne;
                this.entity.setSneaking(mc.player.isSneaking());
                this.entity.rotateElytraX = mc.player.rotateElytraX;
                this.entity.rotateElytraY = mc.player.rotateElytraY;
                this.entity.rotateElytraZ = mc.player.rotateElytraZ;
                this.entity.setFlag(7, mc.player.isElytraFlying());
                mc.world.addEntityToWorld(420420420, this.entity);
            }

            this.timer.reset();
        } else {
            // Queue packets for lag

            final Packet packet = event.getPacket();

            if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().isSingleplayer()) {
                return;
            }

            if (packet instanceof CPacketChatMessage || packet instanceof CPacketConfirmTeleport || packet instanceof CPacketKeepAlive || packet instanceof CPacketTabComplete || packet instanceof CPacketClientStatus) {
                return;
            }

            this.packets.add(packet);
            event.setCanceled(true);
        }

    }

    // FOR LAG DISABLE
    // isLagging will *NOT* be true if stopWhenLag is off
    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() != null) {
                this.lagtimer.reset();
            }
        }

        final float seconds = ((System.currentTimeMillis() - this.lagtimer.getTime()) / 1000.0f) % 60.0f;
        if(this.stopwhenlag.getValue() && seconds > 1.0f) isLagging = true;
        else isLagging = false;
    }
}
