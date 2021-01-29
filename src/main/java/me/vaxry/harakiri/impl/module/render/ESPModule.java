package me.vaxry.harakiri.impl.module.render;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.render.EventRender2D;
import me.vaxry.harakiri.api.event.render.EventRender3D;
import me.vaxry.harakiri.api.event.render.EventRenderName;
import me.vaxry.harakiri.api.extd.ShaderGroupExt;
import me.vaxry.harakiri.api.friend.Friend;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.util.Timer;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */

public final class ESPModule extends Module {

    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Items", "i"}, "Draw Items", false);
    public final Value<Boolean> hostile = new Value<Boolean>("Hostile", new String[]{"Hostile", "h"}, "Draw Hostile Entities", false);
    public final Value<Boolean> passive = new Value<Boolean>("Passive", new String[]{"Passive", "p"}, "Draw Hostile Entities", false);

    private ResourceLocation shader;
    private boolean toLoadShader = false;
    private ICamera cam = new Frustum();

    private float ITEM_A = 0.35f;
    private HashMap<EntityItem, Float> opacity = new HashMap<>();
    private Timer timer = new Timer();

    // scoreboard stuff //
    private Scoreboard board;
    private ScorePlayerTeam green;
    private ScorePlayerTeam red;

    private Class[] hostileMobs = {EntitySpider.class, EntityCaveSpider.class, EntityEnderman.class,EntityPigZombie.class,
        EntityEvoker.class, EntityVindicator.class, EntityVex.class, EntityEndermite.class, EntityGuardian.class, EntityElderGuardian.class,
        EntityShulker.class, EntityHusk.class, EntityStray.class, EntityBlaze.class, EntityCreeper.class, EntityGhast.class, EntityMagmaCube.class,
        EntitySilverfish.class, EntitySkeleton.class, EntitySlime.class, EntityZombie.class, EntityZombieVillager.class, EntityDragon.class, EntityWitch.class};
    private List<Class> hostileMobsList = new ArrayList<>(Arrays.asList(hostileMobs));

    public ESPModule() {
        super("ESP", new String[]{"ESP"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);

        shader = new ResourceLocation("harakirimod", "shaders/shader.json");
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = 100.f;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    @Listener
    public void OnRender3D(EventRender3D event){
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null)
            return;

        if(this.items.getValue()){
            RenderUtil.begin3D();

            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

            // Render itemz
            for(Entity e : mc.world.getLoadedEntityList()){
                if(!(e instanceof EntityItem))
                    continue;

                cam.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                AxisAlignedBB bb = new AxisAlignedBB(
                        e.posX - ITEM_A/2.f - mc.getRenderManager().viewerPosX,
                        e.posY - mc.getRenderManager().viewerPosY,
                        e.posZ - ITEM_A/2.f - mc.getRenderManager().viewerPosZ,
                        e.posX + ITEM_A/2.f - mc.getRenderManager().viewerPosX,
                        e.posY + ITEM_A - mc.getRenderManager().viewerPosY,
                        e.posZ + ITEM_A/2.f - mc.getRenderManager().viewerPosZ);

                if (!cam.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    continue;
                }

                if(!opacity.containsKey(e)){
                    opacity.put((EntityItem)e, 0.f);
                }

                int color = 0x1000000 * opacity.get(e).intValue() + 0xCCFF33;

                RenderUtil.drawBoundingBox(bb, 0.5f, color);
            }

            float jitter = getJitter();

            for(Map.Entry<EntityItem, Float> entry : opacity.entrySet()){
                opacity.put(entry.getKey(), Math.min(255.f, entry.getValue() + jitter));
            }

            RenderUtil.end3D();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        toLoadShader = true;
    }

    @SubscribeEvent
    public void onRenderLivingBasePre(RenderLivingEvent.Specials.Pre<EntityLivingBase> ent){
        Minecraft mc = Minecraft.getMinecraft();
        if(toLoadShader) {

            // Create the shader
            try {
                mc.renderGlobal.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                mc.renderGlobal.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                mc.renderGlobal.entityOutlineFramebuffer = mc.renderGlobal.entityOutlineShader.getFramebufferRaw("final");

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                this.board = mc.player.getWorldScoreboard();
                this.green = board.createTeam("haraGreen");
                this.red = board.createTeam("haraRed");
                this.green.setPrefix(TextFormatting.GREEN.toString());
                this.red.setPrefix(TextFormatting.RED.toString());

            }catch(Throwable t){
                Harakiri.INSTANCE.logChat("Shader failed: " + t.getMessage());
               // JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }

            toLoadShader = false;
        }

        if(this.isEnabled() &&
                ((hostileMobsList.contains(ent.getEntity().getClass()) && hostile.getValue()) ||
                        (!hostileMobsList.contains(ent.getEntity().getClass()) && passive.getValue())) ) {
            ent.getEntity().setGlowing(true);
        } else
            ent.getEntity().setGlowing(false);


        if(hostileMobsList.contains(ent.getEntity().getClass()))
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
        else
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), green.getName());
    }

    @SubscribeEvent
    public void onRenderLivingBasePost(RenderLivingEvent.Specials.Post<EntityLivingBase> ent){

       // if(ent.getEntity() instanceof EntityPlayer)
       //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), red);
       // else
       //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), green);

    }
}
