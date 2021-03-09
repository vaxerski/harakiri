package me.vaxry.harakiri.framework.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class Camera2 {
    private Vec3d pos;

    private float yaw;
    private float yawHead;

    private float pitch;

    private boolean recording;

    private boolean valid;

    private boolean rendering;

    private boolean firstUpdate;

    private Framebuffer frameBuffer;

    private boolean isOptimized = false;

    private boolean camEntityCreated = false;

    private int WIDTH_RESOLUTION = 800;
    private int HEIGHT_RESOLUTION = 600;

    private EntityPlayer viewcam = null;
    private EntityPlayer eCopy = null;

    public Camera2(int x, int y) {
        this.pos = new Vec3d(0, 0, 0);
        this.yaw = 0;
        this.pitch = 0;
        this.WIDTH_RESOLUTION = x;
        this.HEIGHT_RESOLUTION = y;
        this.frameBuffer = new Framebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION, true);
        this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION);
    }

    public void setOptimized(boolean b){
        if(b != this.isOptimized){
            if(b){
                this.frameBuffer = new Framebuffer(WIDTH_RESOLUTION/4, HEIGHT_RESOLUTION/4, true);
                this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION/4, HEIGHT_RESOLUTION/4);
            }else{
                this.frameBuffer = new Framebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION, true);
                this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION);
            }
        }
        this.isOptimized = b;
    }

    //
    // WHAT THE FUCK IS GOING ON HERE??????????????????
    // if this even works, dont touch it, idk how
    // - Vax
    //

    public void updateFbo() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (!this.firstUpdate) {
            mc.renderGlobal.loadRenderers();
            this.firstUpdate = true;
        }
        if (camEntityCreated) {

            addEntityToScene();

            boolean hideGUI = mc.gameSettings.hideGUI;
            int thirdPersonView = mc.gameSettings.thirdPersonView;
            boolean viewBobbing = mc.gameSettings.viewBobbing;
            int displayWidth = mc.displayWidth;
            int displayHeight = mc.displayHeight;

            int frameLimit = mc.gameSettings.limitFramerate;
            float fovSetting = mc.gameSettings.fovSetting;

            boolean sprinting = mc.player.isSprinting();
            boolean invis = mc.player.isInvisible();

            mc.gameSettings.hideGUI = true;
            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.viewBobbing = false;

            if(this.isOptimized){
                mc.displayWidth = WIDTH_RESOLUTION / 4;
                mc.displayHeight = HEIGHT_RESOLUTION / 4;
            }else{
                mc.displayWidth = WIDTH_RESOLUTION;
                mc.displayHeight = HEIGHT_RESOLUTION;
            }

            //if(this.isOptimized)
            //    mc.gameSettings.limitFramerate = 5;
            //else
            //    mc.gameSettings.limitFramerate = 10;

            mc.player.setSprinting(false);

            mc.player.setInvisible(false);

            updateCamEntityStats();

            this.setRecording(true);
            frameBuffer.framebufferClear();
            frameBuffer.bindFramebuffer(true);

            //net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickStart(mc.timer.renderPartialTicks);
            //mc.entityRenderer.updateCameraAndRender(mc.isGamePaused ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks, System.nanoTime());
            //mc.toastGui.drawToast(new ScaledResolution(mc));

            mc.entityRenderer.renderWorld(0, System.nanoTime());
            //net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickEnd(mc.timer.renderPartialTicks);
            mc.entityRenderer.setupOverlayRendering();

            frameBuffer.unbindFramebuffer();
            this.setRecording(false);

            mc.gameSettings.hideGUI = hideGUI;
            mc.gameSettings.thirdPersonView = thirdPersonView;
            mc.gameSettings.viewBobbing = viewBobbing;
            mc.displayWidth = displayWidth;
            mc.displayHeight = displayHeight;
            mc.gameSettings.limitFramerate = frameLimit;
            mc.gameSettings.fovSetting = fovSetting;

            mc.player.setSprinting(sprinting);
            mc.player.setInvisible(invis);

            removeEntityFromScene();

            this.setValid(true);
            this.setRendering(false);
        }
    }

    public void render(float x, float y, float w, float h) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            GlStateManager.pushMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.enableColorMaterial();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            frameBuffer.bindFramebufferTexture();

            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(x, h, 0).tex(0, 0).endVertex();
            bufferbuilder.pos(w, h, 0).tex(1, 0).endVertex();
            bufferbuilder.pos(w, y, 0).tex(1, 1).endVertex();
            bufferbuilder.pos(x, y, 0).tex(0, 1).endVertex();
            tessellator.draw();

            frameBuffer.unbindFramebufferTexture();

            GlStateManager.popMatrix();
        }
    }

    private void updateCamEntityStats() {

        if(!camEntityCreated)
            return;

        viewcam.noClip = true;
        viewcam.setInvisible(true);
        viewcam.copyLocationAndAnglesFrom(eCopy);

        viewcam.setLocationAndAngles(this.pos.x, this.pos.y - Minecraft.getMinecraft().player.eyeHeight, this.pos.z, this.yaw, this.pitch);



        //
        // Prevent angle jitter.
        // I have NO IDEA which were causing the jitter so i copied them all.
        //

        viewcam.cameraYaw = eCopy.cameraYaw;
        viewcam.prevCameraYaw = eCopy.prevCameraYaw;
        viewcam.prevRotationYaw = eCopy.prevRotationYaw;
        viewcam.renderYawOffset = eCopy.renderYawOffset;
        viewcam.rotationYaw = eCopy.rotationYaw;
        viewcam.rotationYawHead = eCopy.rotationYawHead;
        viewcam.prevRotationYawHead = eCopy.prevRotationYawHead;

        viewcam.prevCameraPitch = eCopy.prevCameraPitch;
        viewcam.cameraPitch = eCopy.cameraPitch;
        viewcam.prevRotationPitch = eCopy.prevRotationPitch;
        viewcam.rotationPitch = eCopy.rotationPitch;

    }

    public void createCamEntity(EntityPlayer e) {
        //GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        //viewcam = new FakePlayer(DimensionManager.getWorld(Minecraft.getMinecraft().player.dimension), gameProfile);

        viewcam = new EntityOtherPlayerMP(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player.getGameProfile());

        eCopy = e;

        viewcam.noClip = true;
        viewcam.setInvisible(true);
        viewcam.copyLocationAndAnglesFrom(e);
        viewcam.setLocationAndAngles(this.pos.x, this.pos.y - Minecraft.getMinecraft().player.eyeHeight, this.pos.z, this.yaw, this.pitch);

        camEntityCreated = true;
    }

    public void updateCamEntity(EntityPlayer e) {
        eCopy = e;
    }

    public void destroyCamEntity() {

         //viewcam = null;

        camEntityCreated = false;
    }

    private void addEntityToScene() {
        if(!viewcam.isAddedToWorld())
            Minecraft.getMinecraft().world.addEntityToWorld(420420422, viewcam);
        Minecraft.getMinecraft().setRenderViewEntity(viewcam);
    }

    private void removeEntityFromScene() {
        Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player); // forces the game to return to the original RVE
        if(viewcam.isAddedToWorld())
            Minecraft.getMinecraft().world.removeEntityFromWorld(420420422);
    }

    public void resize() {
        this.frameBuffer.createFramebuffer(WIDTH_RESOLUTION, HEIGHT_RESOLUTION);

        if (!isRecording() && isRendering()) {
            if(this.camEntityCreated)
                this.updateFbo();
        }
    }

    public Vec3d getPos() {
        return pos;
    }

    public void setPos(Vec3d pos) {
        this.pos = pos;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getYawHead() {
        return yawHead;
    }

    public void setYawHead(float yaw) {
        this.yawHead = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isRendering() {
        return rendering;
    }

    public void setRendering(boolean rendering) {
        this.rendering = rendering;
    }
}
