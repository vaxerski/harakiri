package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import me.vaxry.harakiri.framework.event.world.EventSpawnEntity;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import me.vaxry.harakiri.Harakiri;

public class RireworksFemovedComponent extends DraggableHudComponent {

    private int fireworks = 0;
    private int lposx = 0;
    private int lposy = 0;
    private int lposz = 0;
    private EntityFireworkRocket lastEntity;
    private float[] bounds;
    private boolean resetBounds = true;

    private final Timer lastRocketTime = new Timer();

    public RireworksFemovedComponent() {
        super("RemovedFireworks");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);

        Harakiri.get().getEventManager().addEventListener(this);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        fireworks = 0;
    }

    @Listener
    public void onSpawnEntity(EventSpawnEntity event) {
        if (event.getEntity() instanceof EntityFireworkRocket) {
            fireworks += 1;
            lposx = (int)event.getEntity().posX;
            lposy = (int)event.getEntity().posY;
            lposz = (int)event.getEntity().posZ;
            lastEntity = (EntityFireworkRocket)event.getEntity();

            lastRocketTime.reset();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final String delay = fireworks > 100 ? "\247cFireworks: " + String.valueOf(fireworks) : "\247fFireworks: " + String.valueOf(fireworks);

            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(delay));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(delay, this.getX(), this.getY(), -1);



        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(Fireworks)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(Fireworks)", this.getX(), this.getY(), 0xFFAAAAAA);

            fireworks = 0;
            lastRocketTime.reset();
        }
    }

    private final ICamera camera = new Frustum();

    private float[] convertBounds(Entity e, float partialTicks, int width, int height) {
        float x = -1;
        float y = -1;
        float w = width + 1;
        float h = height + 1;

        Vec3d pos = new Vec3d(lposx, lposy, lposz);

        AxisAlignedBB bb = e.getEntityBoundingBox();

        if (e instanceof EntityEnderCrystal) {
            bb = new AxisAlignedBB(bb.minX + 0.3f, bb.minY + 0.2f, bb.minZ + 0.3f, bb.maxX - 0.3f, bb.maxY, bb.maxZ - 0.3f);
        }

        if (e instanceof EntityItem) {
            bb = new AxisAlignedBB(bb.minX, bb.minY + 0.7f, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        }

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return null;
        }

        final Vec3d[] corners = {
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, 0, bb.maxZ - bb.minZ - e.width / 2),

                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.minZ - bb.maxZ + e.width / 2),
                new Vec3d(bb.minX - bb.maxX + e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2),
                new Vec3d(bb.maxX - bb.minX - e.width / 2, bb.maxY - bb.minY, bb.maxZ - bb.minZ - e.width / 2)
        };

        for (Vec3d vec : corners) {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(pos.x + vec.x - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.y + vec.y - Minecraft.getMinecraft().getRenderManager().viewerPosY, pos.z + vec.z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            x = Math.max(x, (float) projection.getX());
            y = Math.max(y, (float) projection.getY());

            w = Math.min(w, (float) projection.getX());
            h = Math.min(h, (float) projection.getY());
        }

        if (x != -1 && y != -1 && w != width + 1 && h != height + 1) {
            return new float[]{x, y, w, h};
        }

        return null;
    }

    private int winX = 0;
    private int winY = 0;

    @Listener
    public void render2D(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if(mc.player == null || mc.world == null)
            return;

        if(lastEntity == null)
            return;

        bounds = this.convertBounds(lastEntity, event.getPartialTicks(), event.getScaledResolution().getScaledWidth(), event.getScaledResolution().getScaledHeight());

        winX = event.getScaledResolution().getScaledWidth();
        winY = event.getScaledResolution().getScaledHeight();

        final float seconds = ((System.currentTimeMillis() - this.lastRocketTime.getTime()) / 1000.0f) % 60.0f;

        if(fireworks != 0){
            if(seconds < 10)
                Harakiri.get().getTTFFontUtil().drawStringWithShadow("Last Firework", bounds[0] + (bounds[2] - bounds[0]) / 2, bounds[1] + (bounds[3] - bounds[1]) - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 1, 0xFFFF0000);
        }
    }

    @Listener
    public void render3D(EventRender3D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if(mc.player == null || mc.world == null)
            return;

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        if(winX != 0 && winY != 0)
            bounds = this.convertBounds(lastEntity, event.getPartialTicks(), winX, winY);

        final float seconds = ((System.currentTimeMillis() - this.lastRocketTime.getTime()) / 1000.0f) % 60.0f;

        RenderUtil.begin3D();

        if(fireworks != 0) {
            if (seconds < 10) {
                final AxisAlignedBB bb = new AxisAlignedBB(
                        lastEntity.posX - mc.getRenderManager().viewerPosX,
                        lastEntity.posY - mc.getRenderManager().viewerPosY,
                        lastEntity.posZ - mc.getRenderManager().viewerPosZ,
                        lastEntity.posX + 0.3f - mc.getRenderManager().viewerPosX,
                        lastEntity.posY + 0.3f - mc.getRenderManager().viewerPosY,
                        lastEntity.posZ + 0.3f - mc.getRenderManager().viewerPosZ);

                if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {

                    RenderUtil.drawFilledBox(bb, ColorUtil.changeAlpha(0xFFFF1111, 100));
                    RenderUtil.drawBoundingBox(bb, 0.5f, 0xFFFF4040);
                }
            }
        }

        RenderUtil.end3D();
    }
}
