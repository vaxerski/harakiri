package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventUpdateInput;
import me.vaxry.harakiri.framework.event.world.EventCollideSoulSand;
import me.vaxry.harakiri.framework.event.world.EventLandOnSlime;
import me.vaxry.harakiri.framework.event.world.EventWalkOnSlime;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoSlowDownModule extends Module {

    public final Value<Boolean> soulsand = new Value<Boolean>("SoulSand", new String[]{"Soul", "SS"}, "No slowdown from soul sand.", true);
    public final Value<Boolean> slime = new Value<Boolean>("Slime", new String[]{"Slime", "SlimeBlock", "SlimeBlocks", "slim"}, "No slowdown from slime blocks.", true);
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"it"}, "No slowdown from using items (eating etc).", true);
    public final Value<Boolean> cobweb = new Value<Boolean>("CobWeb", new String[]{"Webs", "Cob"}, "No slowdown from moving in a cobweb.", true);
    public final Value<Boolean> ice = new Value<Boolean>("Ice", new String[]{"ic"}, "No slowdown from walking on ice.", true);

    public NoSlowDownModule() {
        super("NoSlow", new String[]{"AntiSlow", "NoSlowdown", "AntiSlowdown"}, "Allows you to move faster in certain situations.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Blocks.ICE.setDefaultSlipperiness(0.98f);
        Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);
        Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
    }

    @Listener
    public void collideSoulSand(EventCollideSoulSand event) {
        if (this.soulsand.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onWalkOnSlime(EventWalkOnSlime event) {
        if (this.slime.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onLandOnSlime(EventLandOnSlime event) {
        if (this.slime.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.player.isHandActive()) {
                if (mc.player.getHeldItem(mc.player.getActiveHand()).getItem() instanceof ItemShield) {
                    if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0 && mc.player.getItemInUseMaxCount() >= 8) {
                        mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                    }
                }
            }

            if (this.cobweb.getValue()) {
                mc.player.isInWeb = false;
                if (mc.player.getRidingEntity() != null) {
                    mc.player.getRidingEntity().isInWeb = false;
                }
            }
            if (this.ice.getValue()) {
                if (mc.player.getRidingEntity() != null) {
                    Blocks.ICE.setDefaultSlipperiness(0.98f);
                    Blocks.FROSTED_ICE.setDefaultSlipperiness(0.98f);
                    Blocks.PACKED_ICE.setDefaultSlipperiness(0.98f);
                } else {
                    Blocks.ICE.setDefaultSlipperiness(0.45f);
                    Blocks.FROSTED_ICE.setDefaultSlipperiness(0.45f);
                    Blocks.PACKED_ICE.setDefaultSlipperiness(0.45f);
                }
            }
        }
    }

    @Listener
    public void updateInput(EventUpdateInput event) {
        if (this.items.getValue()) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player.isHandActive() && !mc.player.isRiding()) {
                mc.player.movementInput.moveStrafe /= 0.2f;
                mc.player.movementInput.moveForward /= 0.2f;
            }
        }
    }

}
