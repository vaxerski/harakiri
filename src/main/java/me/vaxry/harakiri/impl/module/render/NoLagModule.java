package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.render.*;
import me.vaxry.harakiri.framework.event.world.EventLightUpdate;
import me.vaxry.harakiri.framework.event.world.EventSpawnParticle;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.tileentity.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.HashMap;
import java.util.Map;

public final class NoLagModule extends Module {

    public final Value<Boolean> light = new Value<Boolean>("Light", new String[]{"Lit", "l"}, "Disables lighting updates.", true);
    public final Value<Boolean> signs = new Value<Boolean>("Signs", new String[]{"Sign", "si"}, "Disables the rendering of sign text.", false);
    //public final Value<Boolean> sounds = new Value<Boolean>("Sounds", new String[]{"Sound", "s"}, "Disable entity swap-item/equip sound.", true);
    public final Value<Boolean> sky = new Value<Boolean>("Sky", new String[]{"Skies", "ski"}, "Disables the rendering of the sky.", false);
    //public final Value<Boolean> pistons = new Value<Boolean>("Pistons", new String[]{"Piston", "p"}, "Choose to enable the piston lag fix. Disables pistons from rendering.", false);
    public final Value<Boolean> names = new Value<Boolean>("Names", new String[]{"Name", "n"}, "Disables the rendering of vanilla nametags (not hara ones).", false);
    public final Value<Boolean> slimes = new Value<Boolean>("Slimes", new String[]{"Slime", "sl"}, "Disables slimes from spawning.", false);
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Item", "i"}, "Disables the rendering of items.", false);
    public final Value<Boolean> particles = new Value<Boolean>("Particles", new String[]{"Part", "par"}, "Disables the spawning of particles.", false);
    public final Value<Boolean> withers = new Value<Boolean>("Withers", new String[]{"Wither", "w"}, "Disables the rendering of withers.", false);
    public final Value<Boolean> witherSkulls = new Value<Boolean>("WitherSkulls", new String[]{"WitherSkull", "skulls", "skull", "ws"}, "Disables the rendering of flying wither skulls.", false);
    public final Value<Boolean> crystals = new Value<Boolean>("Crystals", new String[]{"Wither", "w"}, "Disables the rendering of crystals.", false);
    public final Value<Boolean> tnt = new Value<Boolean>("TNT", new String[]{"Wither", "w"}, "Disables the rendering of TNT.", false);
    public final Value<Boolean> tiles = new Value<Boolean>("TileEntities", new String[]{"TileEntities", "t"}, "Remove Tile entities.", false);
    public final Value<Float> tileDist = new Value<Float>("TileDistance", new String[]{"TileDistance", "td"}, "Distance to remove the tile entities.", 50.f, 0.f, 100.f, 1.f);
    public final Value<Boolean> firework = new Value<Boolean>("Fireworks", new String[]{"Fir", "f"}, "Disables the rendering of fireworks.", false);
    public final Value<Boolean> removeChunkBan = new Value<Boolean>("NoChunkBan", new String[]{"NoChunk", "nw"}, "Remove certain chunkbans.", false);
    public final Value<Boolean> hardRemove = new Value<Boolean>("HardRemove", new String[]{"HardRemove", "hr"}, "Hard removes certain entities (e.g. items)", false);


    private final HashMap<TileEntity, String> signTexts = new HashMap<>();

    public NoLagModule() {
        super("NoLag", new String[]{"AntiLag", "NoRender"}, "Fixes malicious lag exploits and bugs.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.slimes.getValue()) {
                if (event.getPacket() instanceof SPacketSpawnMob) {
                    final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();
                    if (packet.getEntityType() == 55) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @Listener
    public void updateLighting(EventLightUpdate event) {
        if (this.light.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void renderBlockModel(EventRenderBlockModel event) {
        /*if (this.pistons.getValue()) {
            final Block block = event.getBlockState().getBlock();
            if (block instanceof BlockPistonMoving || block instanceof BlockPistonExtension) {
                event.setRenderable(false);
                event.setCanceled(true);
            }
        }*/
        if (this.removeChunkBan.getValue()) {
            final Block block = event.getBlockState().getBlock();
            if (block instanceof BlockBeacon || block instanceof BlockEnchantmentTable) {
                event.setRenderable(false);
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void renderTe(EventRenderTileEntity event){
        if(this.tiles.getValue()){
            double distance = Math.sqrt(event.te.getDistanceSq(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ));

            if(distance > this.tileDist.getValue()){
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void renderWorld(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (this.signs.getValue() || this.removeChunkBan.getValue()) {
            for (TileEntity te : mc.world.loadedTileEntityList) {
                if (te instanceof TileEntitySign && this.signs.getValue()) {
                    final TileEntitySign sign = (TileEntitySign) te;
                    if(!sign.signText[0].toString().equalsIgnoreCase("") || !sign.signText[1].toString().equalsIgnoreCase("") ||
                            !sign.signText[2].toString().equalsIgnoreCase("") || !sign.signText[3].toString().equalsIgnoreCase("")) {
                        signTexts.put(sign, sign.signText[0].getFormattedText() + "<br>" + sign.signText[1].getFormattedText() + "<br>" + sign.signText[2].getFormattedText() + "<br>" + sign.signText[3].getFormattedText());
                        sign.signText = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
                    }
                }
                else if (te instanceof TileEntityBeacon && this.removeChunkBan.getValue()) {
                    te.invalidate();
                }
                else if (te instanceof TileEntityEnchantmentTable && this.removeChunkBan.getValue()) {
                    te.invalidate();
                }
                else if (te instanceof TileEntitySkull && this.removeChunkBan.getValue()) {
                    te.invalidate();
                }
            }
        }

        if(!this.signs.getValue() && !signTexts.isEmpty()){
            // Restore signs
            for(Map.Entry<TileEntity, String> entry : signTexts.entrySet()){
                if(entry.getKey() == null)
                    continue;

                String[] str = entry.getValue().split("<br>");
                ((TileEntitySign)entry.getKey()).signText = new ITextComponent[]{new TextComponentString(str[0]),new TextComponentString(str[1]),new TextComponentString(str[2]),new TextComponentString(str[3])};
            }

            signTexts.clear();
        }
    }

    @Listener
    public void onSpawnParticle(EventSpawnParticle event) {
        if (this.particles.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketSoundEffect) {
                /*if (this.sounds.getValue()) {
                    final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                    if (packet.getCategory() == SoundCategory.PLAYERS && packet.getSound() == SoundEvents.ITEM_ARMOR_EQUIP_GENERIC) {
                        event.setCanceled(true);
                    }
                }*/
            }
        }
    }

    @Listener
    public void onRenderEntity(EventRenderEntity event) {
        if (event.getEntity() != null) {
            if (this.items.getValue()) {
                if (event.getEntity() instanceof EntityItem)
                    event.setCanceled(true);
            }

            if (this.withers.getValue()) {
                if (event.getEntity() instanceof EntityWither)
                    event.setCanceled(true);
            }

            if (this.witherSkulls.getValue()) {
                if (event.getEntity() instanceof EntityWitherSkull)
                    event.setCanceled(true);
            }

            if (this.crystals.getValue()) {
                if (event.getEntity() instanceof EntityEnderCrystal)
                    event.setCanceled(true);
            }

            if (this.tnt.getValue()) {
                if (event.getEntity() instanceof EntityTNTPrimed)
                    event.setCanceled(true);
            }

            if(this.firework.getValue()) {
                if (event.getEntity() instanceof EntityFireworkRocket)
                    event.setCanceled(true);
            }
        }
    }

    @Listener
    public void onRenderSky(EventRenderSky event) {
        if (this.sky.getValue()) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void onRenderName(EventRenderName event) {
        if (this.names.getValue()) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Restore signs
        for(Map.Entry<TileEntity, String> entry : signTexts.entrySet()){
            if(entry.getKey() == null)
                continue;

            String[] str = entry.getValue().split("<br>");
            ((TileEntitySign)entry.getKey()).signText = new ITextComponent[]{new TextComponentString(str[0]),new TextComponentString(str[1]),new TextComponentString(str[2]),new TextComponentString(str[3])};
        }

        signTexts.clear();
    }
}
