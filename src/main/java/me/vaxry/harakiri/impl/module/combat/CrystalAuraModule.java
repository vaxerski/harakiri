package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.task.rotation.RotationTask;
import me.vaxry.harakiri.framework.util.*;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.GodModeModule;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CrystalAuraModule extends Module {

    private HudModule hudModule = null;
    private HudEditorModule hudEditorModule = null;
    private GodModeModule godModeModule = null;

    public boolean crystalAuraHit = false;

    public final Value<Boolean> attack = new Value<Boolean>("Attack", new String[]{"AutoAttack"}, "Automatically attack crystals.", true);
    public final Value<Float> attackDelay = new Value<Float>("AttackDelay", new String[]{"AttackDelay", "AttackDel", "Del"}, "The delay to attack in milliseconds.", 50.0f, 0.0f, 500.0f, 1.0f);
    public final Value<Float> attackRadius = new Value<Float>("AttackRadius", new String[]{"ARange", "HitRange", "AttackDistance", "AttackRange", "ARadius"}, "The minimum range to attack crystals.", 5.0f, 0.0f, 7.0f, 0.1f);
    public final Value<Float> attackMaxDistance = new Value<Float>("AttackMaxDistance", new String[]{"AMaxRange", "MaxAttackRange", "AMaxRadius", "AMD", "AMR"}, "The max (block)distance an entity must be to the a crystal to begin attacking.", 14.0f, 1.0f, 20.0f, 1.0f);
    public final Value<Boolean> place = new Value<Boolean>("Place", new String[]{"AutoPlace"}, "Automatically place crystals.", true);
    public final Value<Float> placeDelay = new Value<Float>("PlaceDelay", new String[]{"PlaceDelay", "PlaceDel"}, "The delay to place crystals.", 50.0f, 0.0f, 500.0f, 1.0f);
    public final Value<Float> placeRadius = new Value<Float>("PlaceRadius", new String[]{"Radius", "PR", "PlaceRange", "Range"}, "The radius in blocks around the player to process placing in.", 5.5f, 1.0f, 7.0f, 0.5f);
    public final Value<Float> placeMaxDistance = new Value<Float>("PlaceMaxDistance", new String[]{"BlockDistance", "MaxBlockDistance", "PMBD", "MBD", "PBD", "BD"}, "The (max)distance an entity must be to the new crystal to begin placing.", 14.0f, 1.0f, 20.0f, 1.0f);
    public final Value<Float> placeLocalDistance = new Value<Float>("PlaceLocalDistance", new String[]{"LocalDistance", "PLD", "LD"}, "The (max)distance away the entity must be from the local player to begin placing.", 6.0f, 1.0f, 20.0f, 0.5f);
    public final Value<Float> minDamage = new Value<Float>("MinDamage", new String[]{"MinDamage", "Min", "MinDmg"}, "The minimum explosion damage calculated to place down a crystal.", 1.5f, 0.0f, 20.0f, 0.5f);
    public final Value<Float> maxSelf = new Value<Float>("MaxSelfDMG", new String[]{"MaxSelfDMG", "MaxS", "MaxSelf"}, "Maximum self damage to detonate a crystal.", 4F, 0.0f, 20.0f, 0.5f);
    public final Value<Boolean> ignore = new Value<Boolean>("Ignore", new String[]{"Ig"}, "Ignore self damage checks.", false);
    public final Value<Boolean> render = new Value<Boolean>("Render", new String[]{"R"}, "Draws information about recently placed crystals from your player.", true);
    public final Value<Boolean> renderDamage = new Value<Boolean>("RenderDamage", new String[]{"RD", "RenderDamage", "ShowDamage"}, "Draws calculated explosion damage on recently placed crystals from your player.", true);
    public final Value<Boolean> offHand = new Value<Boolean>("Offhand", new String[]{"Hand", "otherhand", "off"}, "Use crystals in the off-hand instead of holding them with the main-hand.", false);
    public final Value<Boolean> offHandAuto = new Value<Boolean>("OffhandAuto", new String[]{"HandAuto", "otherhandauto", "offa"}, "Automatically put crystals into the offhand when no need for a totem (AutoTotem).", false);

    private final Timer attackTimer = new Timer();
    private final Timer placeTimer = new Timer();

    private final List<PlaceLocation> placeLocations = new CopyOnWriteArrayList<>();

    private final RotationTask placeRotationTask = new RotationTask("CrystalAuraPlaceTask", 6);
    private final RotationTask attackRotationTask = new RotationTask("CrystalAuraAttackTask", 7);

    private BlockPos currentPlacePosition = null;
    private Entity currentAttackEntity = null;

    public CrystalAuraModule() {
        super("CrystalAura", new String[]{"AutoCrystal", "Crystal"}, "Automatically places crystals near enemies and detonates them", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onFullLoad() {
        super.onFullLoad();
        hudModule = (HudModule)Harakiri.get().getModuleManager().find(HudModule.class);
        hudEditorModule = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);
        godModeModule = (GodModeModule) Harakiri.get().getModuleManager().find(GodModeModule.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getRotationManager().finishTask(this.placeRotationTask);
        Harakiri.get().getRotationManager().finishTask(this.attackRotationTask);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null)
            return;

        switch (event.getStage()) {
            case PRE:
                this.currentPlacePosition = null;
                this.currentAttackEntity = null;

                if (mc.player.getHeldItem(this.offHand.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL) {
                    if (this.place.getValue()) {
                        if (this.placeTimer.passed(this.placeDelay.getValue())) {
                            final float radius = this.placeRadius.getValue();

                            float damage = 0;
                            double maxDistanceToLocal = this.placeLocalDistance.getValue();

                            EntityLivingBase targetPlayer = null;

                            for (float x = radius; x >= -radius; x--) {
                                for (float y = radius; y >= -radius; y--) {
                                    for (float z = radius; z >= -radius; z--) {
                                        final BlockPos blockPos = new BlockPos(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);

                                        if (canPlaceCrystal(blockPos)) {
                                            for (Entity entity : mc.world.loadedEntityList) {
                                                if (entity instanceof EntityPlayer) {
                                                    final EntityPlayer player = (EntityPlayer) entity;
                                                    if (player != mc.player && !player.getName().equals(mc.player.getName()) && player.getHealth() > 0 && Harakiri.get().getFriendManager().isFriend(player) == null) {
                                                        final double distToBlock = entity.getDistance(blockPos.getX() + 0.5f, blockPos.getY() + 1, blockPos.getZ() + 0.5f);
                                                        final double distToLocal = entity.getDistance(mc.player.posX, mc.player.posY, mc.player.posZ);
                                                        if (distToBlock <= this.placeMaxDistance.getValue() && distToLocal <= maxDistanceToLocal) {
                                                            targetPlayer = player;
                                                            maxDistanceToLocal = distToLocal;
                                                        }
                                                    }
                                                }
                                            }

                                            if (targetPlayer != null) {
                                                final float currentDamage = calculateExplosionDamage(targetPlayer, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;

                                                float localDamage = calculateExplosionDamage(mc.player, 6.0f, blockPos.getX() + 0.5f, blockPos.getY() + 1.0f, blockPos.getZ() + 0.5f) / 2.0f;

                                                if (this.isLocalImmune()) {
                                                    localDamage = -1;
                                                }

                                                if (currentDamage > damage && currentDamage >= this.minDamage.getValue() && localDamage <= currentDamage && localDamage <= this.maxSelf.getValue()) {
                                                    damage = currentDamage;
                                                    this.currentPlacePosition = blockPos;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (this.currentPlacePosition != null && damage > 0) {
                                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.currentPlacePosition.getX() + 0.5f, this.currentPlacePosition.getY() + 0.5f, this.currentPlacePosition.getZ() + 0.5f));

                                Harakiri.get().getRotationManager().startTask(this.placeRotationTask);
                                if (this.placeRotationTask.isOnline()) {
                                    Harakiri.get().getRotationManager().setPlayerRotations(angle[0], angle[1]);
                                }
                            }

                            this.placeTimer.reset();
                        }
                    }

                    if (this.attack.getValue()) {
                        for (Entity entity : mc.world.loadedEntityList) {
                            if (entity instanceof EntityEnderCrystal) {
                                if (mc.player.getDistance(entity) <= this.attackRadius.getValue()) {
                                    for (Entity ent : mc.world.loadedEntityList) {
                                        if (ent != null && ent != mc.player && (ent.getDistance(entity) <= this.attackMaxDistance.getValue()) && ent != entity && ent instanceof EntityPlayer) {
                                            final EntityPlayer player = (EntityPlayer) ent;
                                            float currentDamage = calculateExplosionDamage(player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;
                                            float localDamage = calculateExplosionDamage(mc.player, 6.0f, (float) entity.posX, (float) entity.posY, (float) entity.posZ) / 2.0f;

                                            if (this.isLocalImmune()) {
                                                localDamage = -1;
                                            }

                                            if (localDamage <= currentDamage && currentDamage >= this.minDamage.getValue() && localDamage <= this.maxSelf.getValue()) {
                                                final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());

                                                Harakiri.get().getRotationManager().startTask(this.attackRotationTask);
                                                if (this.attackRotationTask.isOnline()) {
                                                    Harakiri.get().getRotationManager().setPlayerRotations(angle[0], angle[1]);
                                                    this.currentAttackEntity = entity;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case POST:
                if (this.currentPlacePosition != null) {
                    if (this.placeRotationTask.isOnline()) {
                        EnumHand useHand = null;
                        if (this.offHand.getValue()) {
                            if (mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.END_CRYSTAL) {
                                useHand = EnumHand.OFF_HAND;
                            } else {
                                if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL)
                                    useHand = EnumHand.MAIN_HAND;
                            }
                        } else {
                            useHand = EnumHand.MAIN_HAND;
                        }
                        if (useHand != null) {
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.currentPlacePosition, EnumFacing.UP, useHand, 0, 0, 0));
                            this.placeLocations.add(new PlaceLocation(this.currentPlacePosition.getX(), this.currentPlacePosition.getY(), this.currentPlacePosition.getZ()));
                        }
                    }
                } else {
                    Harakiri.get().getRotationManager().finishTask(this.placeRotationTask);
                }

                if (this.currentAttackEntity != null) {
                    if (this.attackTimer.passed(this.attackDelay.getValue()) && this.attackRotationTask.isOnline()) {
                        EnumHand useHand = null;
                        if (this.offHand.getValue()) {
                            if (mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.END_CRYSTAL) {
                                useHand = EnumHand.OFF_HAND;
                            } else {
                                if (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL)
                                    useHand = EnumHand.MAIN_HAND;
                            }
                        } else {
                            useHand = EnumHand.MAIN_HAND;
                        }
                        if(useHand != null) {
                            mc.player.swingArm(useHand);
                            this.crystalAuraHit = true;
                            mc.playerController.attackEntity(mc.player, this.currentAttackEntity);
                        }
                        this.attackTimer.reset();
                    }
                } else {
                    Harakiri.get().getRotationManager().finishTask(this.attackRotationTask);
                }

                this.crystalAuraHit = false;
                break;
        }
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
                final SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) event.getPacket();
                if (packetSpawnObject.getType() == 51) {
                    for (PlaceLocation placeLocation : this.placeLocations) {
                        if (placeLocation.getDistance((int) packetSpawnObject.getX(), (int) packetSpawnObject.getY() - 1, (int) packetSpawnObject.getZ()) <= 1) {
                            placeLocation.placed = true;
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onRender(EventRender3D event) {
        if (!this.render.getValue())
            return;

        final Minecraft mc = Minecraft.getMinecraft();

        RenderUtil.begin3D();
        for (PlaceLocation placeLocation : this.placeLocations) {
            if (placeLocation.alpha <= 0) {
                this.placeLocations.remove(placeLocation);
                continue;
            }

            placeLocation.update();

            if (placeLocation.placed) {
                final AxisAlignedBB bb = new AxisAlignedBB(
                        placeLocation.getX() - mc.getRenderManager().viewerPosX,
                        placeLocation.getY() - mc.getRenderManager().viewerPosY,
                        placeLocation.getZ() - mc.getRenderManager().viewerPosZ,
                        placeLocation.getX() + 1 - mc.getRenderManager().viewerPosX,
                        placeLocation.getY() + 1 - mc.getRenderManager().viewerPosY,
                        placeLocation.getZ() + 1 - mc.getRenderManager().viewerPosZ);

                final boolean useRainbow = hudModule.rainbow.getValue();

                RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(useRainbow ? Harakiri.get().getHudManager().rainbowColor : hudEditorModule.color.getValue().getRGB() + 0xFF000000, placeLocation.alpha / 2));
                RenderUtil.drawBoundingBox(bb, 1, ColorUtil.changeAlpha(0xFF000000, placeLocation.alpha));

                if (this.renderDamage.getValue()) {
                    GlStateManager.pushMatrix();
                    //RenderUtil.glBillboardDistanceScaled((float) placeLocation.getX() + 0.5f, (float) placeLocation.getY() + 0.5f, (float) placeLocation.getZ() + 0.5f, mc.player, 1);
                    final float damage = placeLocation.damage;
                    if (damage != -1) {
                        final String damageText = (Math.floor(damage) == damage ? (int) damage : String.format("%.1f", damage)) + "";
                        //GlStateManager.disableDepth();

                        Coordinate textCoord = conv3Dto2DSpace((float) placeLocation.getX() + 0.5f, (float) placeLocation.getY() + 0.5f, (float) placeLocation.getZ() + 0.5f);
                        textCoord.x -= Harakiri.get().getTTFFontUtil().getStringWidth(damageText) / 2F;

                        Harakiri.get().getTTFFontUtil().drawStringWithShadow(damageText, (float)textCoord.x, (float)textCoord.y, 0xFFAAAACC);
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
        RenderUtil.end3D();
    }

    private Coordinate conv3Dto2DSpace(double x, double y, double z) {
        final GLUProjection.Projection projection = GLUProjection.getInstance().project(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

        return projection.getType() == GLUProjection.Projection.Type.OUTSIDE || projection.getType() == GLUProjection.Projection.Type.INVERTED ? null : new Coordinate(projection.getX(), projection.getY());
    }

    private boolean isLocalImmune() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.capabilities.isCreativeMode)
            return true;

        if (godModeModule != null && godModeModule.isEnabled())
            return true;

        if (this.ignore.getValue())
            return true;

        return false;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();

        final Block block = mc.world.getBlockState(pos).getBlock();

        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
            final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
            final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

            if (floor == Blocks.AIR && ceil == Blocks.AIR) {
                if (mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty()) {
                    return mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) <= this.placeRadius.getValue();
                }
            }
        }

        return false;
    }

    private float calculateExplosionDamage(EntityLivingBase entity, float size, float x, float y, float z) {
        final Minecraft mc = Minecraft.getMinecraft();
        final float scale = size * 2.0F;
        final Vec3d pos = MathUtil.interpolateEntity(entity, mc.getRenderPartialTicks());
        final double dist = MathUtil.getDistance(pos, x, y, z) / (double) scale;
        //final double dist = entity.getDistance(x, y, z) / (double) scale;
        final Vec3d vec3d = new Vec3d(x, y, z);
        final double density = (double) entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double densityScale = (1.0D - dist) * density;

        float unscaledDamage = (float) ((int) ((densityScale * densityScale + densityScale) / 2.0d * 7.0d * (double) scale + 1.0d));

        unscaledDamage *= 0.5f * mc.world.getDifficulty().getId();

        return scaleExplosionDamage(entity, new Explosion(mc.world, null, x, y, z, size, false, true), unscaledDamage);
    }

    private float scaleExplosionDamage(EntityLivingBase entity, Explosion explosion, float damage) {
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        damage *= (1.0F - MathHelper.clamp(EnchantmentHelper.getEnchantmentModifierDamage(entity.getArmorInventoryList(), DamageSource.causeExplosionDamage(explosion)), 0.0F, 20.0F) / 25.0F);
        return damage;
    }

    private static final class PlaceLocation extends Vec3i {

        private int alpha = 0xAA;
        private boolean placed = false;
        private float damage = -1;

        private PlaceLocation(int xIn, int yIn, int zIn, float damage) {
            super(xIn, yIn, zIn);
            this.damage = damage;
        }

        private PlaceLocation(int xIn, int yIn, int zIn) {
            super(xIn, yIn, zIn);
        }

        private void update() {
            if (this.alpha > 0)
                this.alpha -= 1;
        }
    }

}
