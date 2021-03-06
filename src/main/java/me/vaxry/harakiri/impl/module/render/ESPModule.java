package me.vaxry.harakiri.impl.module.render;

import ibxm.Player;
import io.netty.util.internal.ReflectionUtil;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.render.EventRenderEntities;
import me.vaxry.harakiri.framework.event.render.EventRenderEntity;
import me.vaxry.harakiri.framework.extd.ShaderGroupExt;
import me.vaxry.harakiri.framework.layeredit.LayerEnderman;
import me.vaxry.harakiri.framework.layeredit.LayerSpider;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.util.*;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerEndermanEyes;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerSpiderEyes;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import scala.collection.parallel.ParIterableLike;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */

public final class ESPModule extends Module {

    enum SHADER {
        OUTLINE,
        GLOW
    }

    public final Value<SHADER> shaderV = new Value<SHADER>("Style", new String[]{"Style", "s"}, "Select the shader to use.", SHADER.OUTLINE);
    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Items", "i"}, "Draw Items", false);
    public final Value<Boolean> itemsShader = new Value<Boolean>("ItemsShader", new String[]{"ItemsShader", "is"}, "Draw Items with a shader (not a box)", false);
    public final Value<Boolean> hostile = new Value<Boolean>("Hostile", new String[]{"Hostile", "h"}, "Draw Hostile Entities", false);
    public final Value<Boolean> passive = new Value<Boolean>("Passive", new String[]{"Passive", "p"}, "Draw Hostile Entities", false);
    public final Value<Boolean> crystals = new Value<Boolean>("Crystals", new String[]{"Crystals", "c"}, "Draw Crystals", false);
    public final Value<Boolean> players = new Value<Boolean>("Players", new String[]{"Players", "pl"}, "Draw Players", false);
    public final Value<Boolean> vehicles = new Value<Boolean>("Vehicles", new String[]{"Vehicles", "v"}, "Draw vehicles", false);
    public final Value<Boolean> itemFrames = new Value<Boolean>("ItemFrames", new String[]{"ItemFrames", "if"}, "Draw item frames", false);

    // Shader stuff
    private Framebuffer entityOutlineFramebuffer;
    private ShaderGroup entityOutlineShader;


    private ResourceLocation shader;
    private ResourceLocation shaderGlow;
    private boolean toLoadShader = true;
    private ICamera cam = new Frustum();
    private SHADER lastShader = SHADER.OUTLINE;

    private float ITEM_A = 0.35f;
    private HashMap<EntityItem, Float> opacity = new HashMap<>();
    private Timer timer = new Timer();

    private ArrayList<EntityLivingBase> livingBases = new ArrayList<>();
    private static List<LayerRenderer<EntityLivingBase>> renderLivingBaseLayerRenderersField;

    // scoreboard stuff //
    private Scoreboard board;
    public ScorePlayerTeam green;
    public ScorePlayerTeam red;
    public ScorePlayerTeam purple;
    public ScorePlayerTeam lblue;
    public ScorePlayerTeam yellow;
    public ScorePlayerTeam pink;
    public ScorePlayerTeam gray;

    private ArrayList<EntityPlayer> coloredPlayers = new ArrayList<>();

    private Class[] hostileMobs = {EntityWither.class, EntityWitherSkeleton.class, EntitySpider.class, EntityCaveSpider.class, EntityEnderman.class,EntityPigZombie.class,
        EntityEvoker.class, EntityVindicator.class, EntityVex.class, EntityEndermite.class, EntityGuardian.class, EntityElderGuardian.class,
        EntityShulker.class, EntityHusk.class, EntityStray.class, EntityBlaze.class, EntityCreeper.class, EntityGhast.class, EntityMagmaCube.class,
        EntitySilverfish.class, EntitySkeleton.class, EntitySlime.class, EntityZombie.class, EntityZombieVillager.class, EntityDragon.class, EntityWitch.class};
    private List<Class> hostileMobsList = new ArrayList<>(Arrays.asList(hostileMobs));

    public ESPModule() {
        super("ESP", new String[]{"ESP"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);

        Minecraft mc = Minecraft.getMinecraft();

        shader = new ResourceLocation("shaders/post/esp.json");
        shaderGlow = new ResourceLocation("shaders/post/espglow.json");
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

        if(this.items.getValue() && !this.itemsShader.getValue()){
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

        // process storageesp
        StorageESPModule storageESPModule = (StorageESPModule)Harakiri.INSTANCE.getModuleManager().find(StorageESPModule.class);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        toLoadShader = true;
    }

    @SubscribeEvent
    public void onRenderLivingBasePre(RenderLivingEvent.Specials.Pre<EntityLivingBase> ent){
        Minecraft mc = Minecraft.getMinecraft();

        if(!livingBases.contains(ent.getEntity())){
            livingBases.add(ent.getEntity());
            replaceLayers(ent.getEntity());
        }

        if(hostileMobsList.contains(ent.getEntity().getClass())) {
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
        }else{
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), green.getName());
        }
    }

    @Listener
    public void onRenderEntity(EventRenderEntity event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.getStage() == EventStageable.EventStage.PRE && this.isEnabled()) {

            if (event.getEntity() instanceof EntityEnderCrystal && this.crystals.getValue()) {
                board.addPlayerToTeam(event.getEntity().getUniqueID().toString(), pink.getName());
            }

            if (event.getEntity() instanceof EntityItem && this.itemsShader.getValue()) {
                board.addPlayerToTeam(event.getEntity().getUniqueID().toString(), green.getName());
            }

            if (event.getEntity() instanceof EntityPlayerMP || event.getEntity() instanceof EntityPlayer || event.getEntity() instanceof EntityOtherPlayerMP || event.getEntity() instanceof EntityPlayerSP) {
                // player

                if(this.players.getValue()) {

                    if (!coloredPlayers.contains((EntityPlayer) event.getEntity()))
                        coloredPlayers.add((EntityPlayer) event.getEntity());

                    if (Harakiri.INSTANCE.getFriendManager().isFriend(event.getEntity()) != null) {
                        //friend
                        board.addPlayerToTeam(event.getEntity().getName(), lblue.getName());
                    } else {
                        // Not friend.
                        board.addPlayerToTeam(event.getEntity().getName(), red.getName());
                    }
                }
            }
        }
    }

    @Listener
    public void onRenderEntities(EventRenderEntities event){
        if(event.getStage() == EventStageable.EventStage.POST){
            for(EntityPlayer e : coloredPlayers) {
                board.removePlayerFromTeams(e.getName());
                board.removeEntity(e);
            }

            coloredPlayers.clear();
        } else if(event.getStage() == EventStageable.EventStage.PRE){
            renderESP(event, event.getPartialTicks());
        } else if(event.getStage() == EventStageable.EventStage.MID){

            // enable alpha if glow

        } else if(event.getStage() == EventStageable.EventStage.RENDER1){

        }
    }

    @SubscribeEvent
    public void onRenderLivingBasePost(RenderLivingEvent.Specials.Post<EntityLivingBase> ent){

        // if(ent.getEntity() instanceof EntityPlayer)
        //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), red);
        // else
        //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), green);

    }

    private float lastPartialTicks = 0;

    public void renderFramebuffer(){
        if(!this.isEnabled())
            return;
        final Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        this.entityOutlineFramebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
        GlStateManager.disableBlend();
    }

    private void renderAllEntities(float partialTicks, boolean doColor){
        final Minecraft mc = Minecraft.getMinecraft();

        for(Entity e : mc.world.getLoadedEntityList()){
            if(!(e instanceof EntityItem) && !(e instanceof EntityItemFrame) && !(e instanceof EntityMinecart) && !(e instanceof EntityBoat)
                && !(e instanceof EntityLivingBase) && !(e instanceof EntityEnderCrystal))
                continue;

            if((e instanceof EntityItem && !this.itemsShader.getValue()) ||
                    (e instanceof EntityItemFrame && !this.itemFrames.getValue())
                    || ((e instanceof EntityBoat || e instanceof EntityMinecart) && !this.vehicles.getValue())
                    || (e instanceof EntityEnderCrystal && !this.crystals.getValue())
                    || (e instanceof EntityPlayer && !this.players.getValue())
                    || (e instanceof EntityLivingBase && this.hostileMobsList.contains(e.getClass()) && !this.hostile.getValue())
                    || (e instanceof EntityLivingBase && !this.hostileMobsList.contains(e.getClass()) && !this.passive.getValue()))
                continue;

            if(e == mc.getRenderViewEntity())
                continue;

            if(doColor) {
                GlStateManager.enableColorMaterial();

                if (e instanceof EntityItem) {
                    //GlStateManager.enableOutlineMode(0xFFCCFF33);
                } else if (e instanceof EntityItemFrame || e instanceof EntityMinecart || e instanceof EntityBoat) {
                    mc.getRenderManager().setRenderOutlines(false);
                    GlStateManager.enableOutlineMode(0xFFFF6600);
                } else if (e instanceof EntityEnderCrystal) {
                    //GlStateManager.enableOutlineMode(0xFF9900CC);
                } else if (e instanceof EntityPlayer) {
                    /*if (Harakiri.INSTANCE.getFriendManager().isFriend(e) != null)
                        GlStateManager.enableOutlineMode(0xFF00FFFF);
                    else
                        GlStateManager.enableOutlineMode(0xFFFF3300);*/
                } else if (e instanceof EntityLivingBase) {
                    /*if (this.hostileMobsList.contains(e.getClass()))
                        GlStateManager.enableOutlineMode(0xFFFF3300);
                    else
                        GlStateManager.enableOutlineMode(0xFF00EE00);*/
                }

                mc.getRenderManager().renderEntityStatic(e, partialTicks, false);

                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();

                if (e instanceof EntityItemFrame || e instanceof EntityMinecart || e instanceof EntityBoat) {
                    mc.getRenderManager().setRenderOutlines(true);
                }
            }else{
                mc.getRenderManager().renderEntityStatic(e, partialTicks, false);
            }
        }
    }

    private void renderAllTileEntities(float partialTicks, boolean doColor){
        if(((StorageESPModule) Harakiri.INSTANCE.getModuleManager().find(StorageESPModule.class)).modeValue.getValue() == StorageESPModule.MODE.CPU)
            return;
        
        for(TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList){
            if(doColor) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(((StorageESPModule) Harakiri.INSTANCE.getModuleManager().find(StorageESPModule.class)).getColorShader(te));
            }

            TileEntityRendererDispatcher.instance.render(te, partialTicks, -1);

            if(doColor) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }
        }
    }

    private void renderESP(EventRenderEntities event, float partialTicks){
        final Minecraft mc = Minecraft.getMinecraft();
        lastPartialTicks = partialTicks;

        if(this.shaderV.getValue() == SHADER.GLOW){
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
        }

        // Setup
        if (toLoadShader || this.lastShader != this.shaderV.getValue()) {
            if(toLoadShader){
                try {
                    this.board = mc.player.getWorldScoreboard();
                    this.green = board.createTeam("haraGreen");
                    this.red = board.createTeam("haraRed");
                    this.purple = board.createTeam("haraPurple");
                    this.lblue = board.createTeam("haraLBlue");
                    this.yellow = board.createTeam("haraYellow");
                    this.gray = board.createTeam("haraGray");
                    this.pink = board.createTeam("haraPink");

                    this.green.setPrefix(TextFormatting.GREEN.toString());
                    this.red.setPrefix(TextFormatting.RED.toString());
                    this.purple.setPrefix(TextFormatting.LIGHT_PURPLE.toString());
                    this.lblue.setPrefix(TextFormatting.AQUA.toString());
                    this.yellow.setPrefix(TextFormatting.YELLOW.toString());
                    this.gray.setPrefix(TextFormatting.GRAY.toString());
                    this.pink.setPrefix(TextFormatting.LIGHT_PURPLE.toString());
                }catch(Throwable t){
                    Harakiri.INSTANCE.logChat("Shader failed 2: " + t.getMessage());
                    //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            try {
                if (this.shaderV.getValue() == SHADER.OUTLINE)
                    this.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                else
                    this.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shaderGlow);
                this.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");

                this.lastShader = this.shaderV.getValue();
                toLoadShader = false;
            }catch(Throwable t){
                Harakiri.INSTANCE.logChat("Shader failed 2: " + t.getMessage());
                //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Prep
        double d3 = event.getRenderviewentity().lastTickPosX + (event.getRenderviewentity().posX - event.getRenderviewentity().lastTickPosX) * (double)partialTicks;
        double d4 = event.getRenderviewentity().lastTickPosY + (event.getRenderviewentity().posY - event.getRenderviewentity().lastTickPosY) * (double)partialTicks;
        double d5 = event.getRenderviewentity().lastTickPosZ + (event.getRenderviewentity().posZ - event.getRenderviewentity().lastTickPosZ) * (double)partialTicks;
        TileEntityRendererDispatcher.staticPlayerX = d3;
        TileEntityRendererDispatcher.staticPlayerY = d4;
        TileEntityRendererDispatcher.staticPlayerZ = d5;
        mc.getRenderManager().setRenderPosition(d3, d4, d5);
        mc.entityRenderer.enableLightmap();
        TileEntityRendererDispatcher.instance.prepare(mc.world, mc.getTextureManager(), mc.fontRenderer, mc.getRenderViewEntity(), mc.objectMouseOver, partialTicks);
        mc.getRenderManager().cacheActiveRenderInfo(mc.world, mc.fontRenderer, mc.getRenderViewEntity(), mc.pointedEntity, mc.gameSettings, partialTicks);

        // Stolen from minecraft jejejeje

        // Render pass is ass so i have to do it another way
        if(ForgeHooksClient.getWorldRenderPass() == 0) {
            this.entityOutlineFramebuffer.framebufferClear();

            GlStateManager.depthFunc(519);
            GlStateManager.disableFog();
            this.entityOutlineFramebuffer.bindFramebuffer(false);
            RenderHelper.disableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(true);

            renderAllEntities(partialTicks, true);

            if(Harakiri.INSTANCE.getModuleManager().find(StorageESPModule.class).isEnabled())
                renderAllTileEntities(partialTicks, true);

            mc.getRenderManager().setRenderOutlines(false);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.depthMask(false);
            this.entityOutlineShader.render(partialTicks);
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.depthFunc(515);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();

            mc.getFramebuffer().bindFramebuffer(false);
        }
    }

    private static void replaceLayers(EntityLivingBase livingBase)
    {
        Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(livingBase);
        renderLivingBaseLayerRenderersField = ReflectionHelper.getPrivateValue(RenderLivingBase.class, (RenderLivingBase)render, "field_177097_h", "layerRenderers");
        try
        {
            List<LayerRenderer<EntityLivingBase>> list = renderLivingBaseLayerRenderersField;
            for (LayerRenderer layer : list.toArray(new LayerRenderer[list.size()]))
            {
                if (layer instanceof LayerSpiderEyes)
                {
                    list.remove(layer);
                    list.add(new LayerSpider((RenderSpider) render));
                }
                else if (layer instanceof LayerEndermanEyes)
                {
                    list.remove(layer);
                    list.add(new LayerEnderman((RenderEnderman) render));
                }
            }
        }
        catch (Throwable t)
        {
            //oops
        }
    }

    public ScorePlayerTeam getTeamFromStr(String s){
        if(s.equalsIgnoreCase(red.toString()))
            return red;
        else if(s.equalsIgnoreCase(green.toString()))
            return green;
        else if(s.equalsIgnoreCase(purple.toString()))
            return purple;
        else if(s.equalsIgnoreCase(lblue.toString()))
            return lblue;
        return null;
    }
}
