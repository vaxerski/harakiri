package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.render.EventRenderEntities;
import me.vaxry.harakiri.framework.event.render.EventRenderEntity;
import me.vaxry.harakiri.framework.extd.ShaderGroupExt;
import me.vaxry.harakiri.framework.layeredit.LayerEnderman;
import me.vaxry.harakiri.framework.layeredit.LayerSpider;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.*;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.manager.APIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.LayerEndermanEyes;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerSpiderEyes;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.*;
import java.util.List;


public final class ESPModule extends Module {

    public boolean isRenderingOutline = false;

    private StorageESPModule storageESPModule = null;

    enum SHADER {
        OUTLINE,
        SIMPLIFIED
    }

    public final Value<SHADER> shaderV = new Value<SHADER>("Style", new String[]{"Style", "s"}, "Select the shader to use. Simplified works with Optifine Shaders.", SHADER.OUTLINE);
    public final Value<Boolean> removeLayers = new Value<Boolean>("RemoveLayers", new String[]{"RemoveLayers", "rl"}, "Do not draw outlines over layers, for example elytra or armor.", true);
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

        this.onEnable();
    }

    @Override
    public void onFullLoad() {
        super.onFullLoad();
        this.storageESPModule = (StorageESPModule) Harakiri.get().getModuleManager().find(StorageESPModule.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Harakiri.get().getEventManager().addEventListener(this);
        return;
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

        if (mc.player == null || (!this.isEnabled() && !storageESPModule.isEnabled()))
            return;

        RenderUtil.begin3D();

        mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

        if(this.items.getValue() && !this.itemsShader.getValue() && this.isEnabled()){

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
        }

        if(Harakiri.get().getUsername().equalsIgnoreCase(""))
            Harakiri.get().getApiManager().killThisThing(); // Anti crack, some sort of

        if(this.shaderV.getValue() == SHADER.SIMPLIFIED && this.isEnabled()){
            for(Entity e : mc.world.getLoadedEntityList()) {
                if (e == mc.player)
                    continue;

                if((e instanceof EntityItemFrame && !this.itemFrames.getValue())
                        || ((e instanceof EntityBoat || e instanceof EntityMinecart) && !this.vehicles.getValue())
                        || (e instanceof EntityEnderCrystal && !this.crystals.getValue())
                        || (e instanceof EntityPlayer && !this.players.getValue())
                        || (e instanceof EntityLivingBase && this.hostileMobsList.contains(e.getClass()) && !this.hostile.getValue() && !(e instanceof EntityPlayer))
                        || (e instanceof EntityLivingBase && !this.hostileMobsList.contains(e.getClass()) && !this.passive.getValue() && !(e instanceof EntityPlayer)))
                    continue;

                cam.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                AxisAlignedBB hitbox = e.getEntityBoundingBox();

                double deltaX = e.posX - e.lastTickPosX;
                double deltaY = e.posY - e.lastTickPosY;
                double deltaZ = e.posZ - e.lastTickPosZ;

                AxisAlignedBB bb = new AxisAlignedBB(
                        hitbox.minX - mc.getRenderManager().viewerPosX + deltaX,
                        hitbox.minY - mc.getRenderManager().viewerPosY + deltaY,
                        hitbox.minZ - mc.getRenderManager().viewerPosZ + deltaZ,
                        hitbox.maxX - mc.getRenderManager().viewerPosX + deltaX,
                        hitbox.maxY - mc.getRenderManager().viewerPosY + deltaY,
                        hitbox.maxZ - mc.getRenderManager().viewerPosZ + deltaZ);

                if (!cam.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    continue;
                }

                int color = 0xFFFFFFFF;

                if (e instanceof EntityItemFrame || e instanceof EntityMinecart || e instanceof EntityBoat) {
                    color = 0xFFFF6600;
                } else if (e instanceof EntityEnderCrystal) {
                    color = 0xFF9900CC;
                } else if (e instanceof EntityPlayer) {
                    if (Harakiri.get().getFriendManager().isFriend(e) != null) {
                        color = 0xFF80FFFF;
                    }else{
                        color = 0xFFFF3333;
                    }
                } else if (e instanceof EntityLivingBase) {
                    if(hostileMobsList.contains(e.getClass())) {
                        color = 0xFFFF3333;
                    } else {
                        color = 0xFF33FF33;
                    }
                }

                RenderUtil.drawBoundingBox(bb, 0.5f, color);
            }
        }

        if(storageESPModule.isEnabled() && storageESPModule.modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED){

            mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

            for(TileEntity te : mc.world.loadedTileEntityList){
                if(!storageESPModule.isTileStorage(te))
                    continue;

                cam.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

                final AxisAlignedBB bb = storageESPModule.boundingBoxForEnt(te);
                final AxisAlignedBB bb2 = new AxisAlignedBB(
                        bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ);

                if (!cam.isBoundingBoxInFrustum(bb2)) {
                    continue;
                }

                RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(storageESPModule.getColorShader(te), 0x55));
                RenderUtil.drawBoundingBox(bb, 1.f, 0x44000000);
            }
        }

        RenderUtil.end3D();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        toLoadShader = true;
    }

    /*@SubscribeEvent
    public void onRenderLivingBasePre(RenderLivingEvent.Specials.Pre<EntityLivingBase> ent){
        Minecraft mc = Minecraft.getMinecraft();

        if(!livingBases.contains(ent.getEntity())){
            livingBases.add(ent.getEntity());
            //replaceLayers(ent.getEntity());
        }

        if(hostileMobsList.contains(ent.getEntity().getClass())) {
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
        }else{
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), green.getName());
        }
    }*/

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

                if(this.players.getValue() && this.shaderV.getValue() != SHADER.SIMPLIFIED) {

                    if (!coloredPlayers.contains((EntityPlayer) event.getEntity()))
                        coloredPlayers.add((EntityPlayer) event.getEntity());

                    if (Harakiri.get().getFriendManager().isFriend(event.getEntity()) != null) {
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
            if(this.isEnabled() || storageESPModule.isEnabled())
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
        if(!this.isEnabled() && !storageESPModule.isEnabled())
            return;

        if(this.isEnabled() && storageESPModule.isEnabled()){
            if(this.shaderV.getValue() == SHADER.SIMPLIFIED && storageESPModule.modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED)
                return;
        }else if(this.isEnabled()){
            if(this.shaderV.getValue() == SHADER.SIMPLIFIED)
                return;
        }else if(storageESPModule.isEnabled()){
            if(storageESPModule.modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED)
                return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        this.entityOutlineFramebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
        GlStateManager.disableBlend();
    }

    private void renderAllEntities(float partialTicks, boolean doColor){
        final Minecraft mc = Minecraft.getMinecraft();

        if(this.shaderV.getValue() == SHADER.SIMPLIFIED)
            return;

        for(Entity e : mc.world.getLoadedEntityList()){
            if(!(e instanceof EntityItem) && !(e instanceof EntityItemFrame) && !(e instanceof EntityMinecart) && !(e instanceof EntityBoat)
                && !(e instanceof EntityLivingBase) && !(e instanceof EntityEnderCrystal))
                continue;

            if((e instanceof EntityItem && !this.itemsShader.getValue()) ||
                    (e instanceof EntityItemFrame && !this.itemFrames.getValue())
                    || ((e instanceof EntityBoat || e instanceof EntityMinecart) && !this.vehicles.getValue())
                    || (e instanceof EntityEnderCrystal && !this.crystals.getValue())
                    || (e instanceof EntityPlayer && !this.players.getValue())
                    || (e instanceof EntityLivingBase && this.hostileMobsList.contains(e.getClass()) && !this.hostile.getValue() && !(e instanceof EntityPlayer))
                    || (e instanceof EntityLivingBase && !this.hostileMobsList.contains(e.getClass()) && !this.passive.getValue() && !(e instanceof EntityPlayer)))
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
                    /*if (Harakiri.get().getFriendManager().isFriend(e) != null)
                        GlStateManager.enableOutlineMode(0xFF00FFFF);
                    else
                        GlStateManager.enableOutlineMode(0xFFFF3300);*/
                } else if (e instanceof EntityLivingBase) {
                    if(hostileMobsList.contains(e.getClass())) {
                        board.addPlayerToTeam(e.getUniqueID().toString(), red.getName());
                    } else {
                        board.addPlayerToTeam(e.getUniqueID().toString(), green.getName());
                    }
                    /*if (this.hostileMobsList.contains(e.getClass()))
                        GlStateManager.enableOutlineMode(0xFFFF3300);
                    else
                        GlStateManager.enableOutlineMode(0xFF00EE00);*/
                }

                //mc.getRenderManager().renderEntityStatic(e, partialTicks, false);

                // Force full brightness // // // // // // // // // // // //
                int i = 15728880;
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
                double d0 = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double)partialTicks;
                double d1 = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double)partialTicks;
                double d2 = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double)partialTicks;
                float f = e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * partialTicks;
                mc.getRenderManager().renderEntity(e, d0 - mc.getRenderManager().renderPosX, d1 - mc.getRenderManager().renderPosY, d2 - mc.getRenderManager().renderPosZ, f, partialTicks, false);
                // // // // // // // // // // // // // // // // // // // //

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
        if(((StorageESPModule) storageESPModule).modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED)
            return;

        for(TileEntity te : Minecraft.getMinecraft().world.loadedTileEntityList){
            if(((StorageESPModule) storageESPModule).getColorShader(te) == 0)
                continue; // Cuz fuck stuff we dont care bout

            if(doColor) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(((StorageESPModule) storageESPModule).getColorShader(te));
            }

            // This doesnt force lighting
            TileEntitySpecialRenderer<TileEntity> tileentityspecialrenderer = TileEntityRendererDispatcher.instance.getRenderer(te);
            if(tileentityspecialrenderer != null) {
                tileentityspecialrenderer.setLightmapDisabled(true);
                tileentityspecialrenderer.render(te, (double) te.getPos().getX() - TileEntityRendererDispatcher.instance.staticPlayerX, (double) te.getPos().getY() - TileEntityRendererDispatcher.instance.staticPlayerY, (double) te.getPos().getZ() - TileEntityRendererDispatcher.instance.staticPlayerZ, partialTicks, -1, 1.0F);
            }
            //TileEntityRendererDispatcher.instance.render(te, partialTicks, -1);

            if(doColor) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }
        }
    }

    private void renderESP(EventRenderEntities event, float partialTicks){
        final Minecraft mc = Minecraft.getMinecraft();
        lastPartialTicks = partialTicks;

        if(this.isEnabled() && storageESPModule.isEnabled()){
            if(this.shaderV.getValue() == SHADER.SIMPLIFIED && storageESPModule.modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED)
                return;
        }else if(this.isEnabled()){
            if(this.shaderV.getValue() == SHADER.SIMPLIFIED)
                return;
        }else if(storageESPModule.isEnabled()){
            if(storageESPModule.modeValue.getValue() == StorageESPModule.MODE.SIMPLIFIED)
                return;
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
                    Harakiri.get().logChat("Shader failed 2: " + t.getMessage());
                    //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            try {
                this.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                this.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");

                this.lastShader = this.shaderV.getValue();
            }catch(Throwable t){
                //Harakiri.get().logChat("Shader failed 2: " + t.getMessage());
                //JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }

            toLoadShader = false;
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

            // So that we get bright stuff.
            float gamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 200000F;

            GlStateManager.depthFunc(519);
            GlStateManager.disableFog();
            this.entityOutlineFramebuffer.bindFramebuffer(false);
            RenderHelper.disableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(true);
            GlStateManager.disableLighting();

            isRenderingOutline = true;
            if(this.isEnabled())
                renderAllEntities(partialTicks, true);
            isRenderingOutline = false;

            if(storageESPModule.isEnabled())
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

            this.entityOutlineFramebuffer.unbindFramebuffer();
            mc.framebuffer.bindFramebuffer(false);

            mc.gameSettings.gammaSetting = gamma;
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
