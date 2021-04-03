package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class OffscreenModule extends Module {

    private Vector2f[] triPoints = new Vector2f[3];

    public final Value<Integer> size = new Value<Integer>("Size", new String[]{"size", "s"}, "Size of the triangles", 10, 0, 100, 1);
    public final Value<Integer> distance = new Value<Integer>("Distance", new String[]{"dist", "distance"}, "Distance to the triangles", 100, 0, 400, 1);
    public final Value<Color> arrColor = new Value<Color>("ArrowColor", new String[]{"ac", "arrowcolor"}, "The arrows' color", new Color(255,50,50));
    public final Value<Integer> alpha = new Value<Integer>("Alpha", new String[]{"alpha", "a"}, "Alpha of the triangles", 255, 0, 255, 1);
    public final Value<Boolean> rainbow = new Value<Boolean>("Rainbow", new String[]{"rain", "rainbow"}, "Makes the arrows rainbow.", false);
    public final Value<Boolean> flashAlpha = new Value<Boolean>("FlashAlpha", new String[]{"FlashAlpha", "fa"}, "Makes the alpha oscillate", false);
    public final Value<Float> flashAlphaSpeed = new Value<Float>("FlashAlphaSpeed", new String[]{"FlashAlphaSpeed", "fas"}, "Speed of the flashing alpha", 7.f, 1.f, 15.f, 0.5f);


    private float rainSpeed = 0.1f;
    private Timer timer = new Timer();
    private Timer timerA = new Timer();
    private ICamera camera = new Frustum();
    private float hue = 0;
    private float alphaFloat = 255;
    private boolean goingDown = true;

    public OffscreenModule() {
        super("Offscreen", new String[]{"offscreen"}, "Shows entities which are not in the render distance.", "NONE", -1, Module.ModuleType.RENDER);
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = rainSpeed;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    private float getJitterAlpha() {
        final float seconds = ((System.currentTimeMillis() - this.timerA.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = flashAlphaSpeed.getValue() * 100;

        this.timerA.reset();
        return desiredTimePerSecond * seconds;
    }

    @Listener
    public void render2D(EventRender2D event) {

        hue += getJitter();
        if(hue > 1)
            hue -= 1;
        java.awt.Color rainbowColorC = java.awt.Color.getHSBColor(hue, 1, 1);
        int colour = 0;

        final float alphaJitter = getJitterAlpha();

        if(flashAlpha.getValue()){
            if(goingDown)
                alphaFloat -= alphaJitter;
            else
                alphaFloat += alphaJitter;

            if(alphaFloat > 255){
                goingDown = true;
                alphaFloat = 255;
            } else if(alphaFloat < 0) {
                goingDown = false;
                alphaFloat = 0;
            }

            colour = 0x1000000 * (int)alphaFloat;
        }else{
            colour = 0x1000000 * alpha.getValue();
        }

        final int alphaColor = colour;


        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        float screen_center_x = res.getScaledWidth()/2;
        float screen_center_y = res.getScaledHeight()/2;

        final Minecraft mc = Minecraft.getMinecraft();

        for (Entity e : mc.world.loadedEntityList) {
            if(e == null)
                continue;

            if(!(e instanceof EntityPlayer))
                continue;

            if(e == mc.player)
                continue;

            if(e.getEntityId() == 420420420 || e.getEntityId() == 420420421)
                continue;

            int colourToUse = alphaColor;

            if(Harakiri.get().getFriendManager().isFriend(e) != null)
                colourToUse += 0x00B3FF;
            else
                colourToUse += rainbow.getValue() ? + rainbowColorC.getRed() * 0x10000 + rainbowColorC.getGreen() * 0x100 + rainbowColorC.getBlue() :
                        + arrColor.getValue().getRed() * 0x10000 + arrColor.getValue().getGreen() * 0x100 + arrColor.getValue().getBlue();

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            final BlockPos blockPos = e.getPosition();

            final AxisAlignedBB bb = new AxisAlignedBB(
                    blockPos.getX() - mc.getRenderManager().viewerPosX,
                    blockPos.getY() - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                    blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                continue;
            }

            final GLUProjection.Projection projection = GLUProjection.getInstance().project(blockPos.getX() - mc.getRenderManager().viewerPosX, blockPos.getY() - mc.getRenderManager().viewerPosY, blockPos.getZ() - mc.getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            // Draw the arrow

            Vector2f angle;
            angle = VectorAngles(new Vector3f((float)(screen_center_x - projection.getX()), (float)(screen_center_y - projection.getY()), 0));
            //Vec3d angle = new Vec3d(0, 0, 1).rotatePitch(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationPitch)).rotateYaw(-(float) Math.toRadians(Minecraft.getMinecraft().player.rotationYaw));

            angle.y += 180;

            final float angle_yaw_rad = (float)Math.toRadians(angle.y);

			final float new_point_x = screen_center_x +
                    distance.getValue() * (float)Math.cos(angle_yaw_rad);
            final float new_point_y = screen_center_y +
                    distance.getValue() * (float)Math.sin(angle_yaw_rad);


            triPoints[0] = new Vector2f(new_point_x - size.getValue(), new_point_y - size.getValue());
            triPoints[1] = new Vector2f(new_point_x + size.getValue() * 2.5f, new_point_y);
            triPoints[2] = new Vector2f(new_point_x - size.getValue(), new_point_y + size.getValue());

            final Vector2f points_center = divVec2f(addVec2f(addVec2f(triPoints[0], triPoints[1]), triPoints[2]), 3);

            GlStateManager.pushMatrix();
            RenderUtil.begin2D();

            RenderUtil.drawTriangle(points_center.x, points_center.y, size.getValue(), angle.y + 90, colourToUse);

            RenderUtil.end2D();
            GlStateManager.popMatrix();
        }
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    private Vector3f AngleVectors(final Vector3f angles) {
        Vector2f spcp;
        Vector2f sycy;

        spcp = XMScalarSinCos((float)Math.toRadians(angles.x));
        sycy = XMScalarSinCos((float)Math.toRadians(angles.y));

        return new Vector3f(spcp.y*sycy.y,spcp.y*sycy.x,-spcp.x);
    }

    private Vector2f VectorAngles(final Vector3f forward) {
        float	tmp, yaw, pitch;

        if(forward.x == 0 && forward.y == 0) {
            yaw = 0;
            if(forward.z > 0)
                pitch = 270;
            else
                pitch = 90;
        } else {
            yaw = (float)(Math.atan2(forward.y, forward.x) * 180 / 3.1415f);
            if(yaw < 0)
                yaw += 360;

            tmp = (float)Math.sqrt(forward.x * forward.x + forward.y * forward.y);
            pitch = (float)(Math.atan2(-forward.z, tmp) * 180 / 3.1415f);
            if(pitch < 0)
                pitch += 360;
        }

        Vector2f angles = new Vector2f();
        angles.x = pitch;
        angles.y = yaw;
        return angles;
    }

    private Vector2f addVec2f(Vector2f a, Vector2f b) {
        return new Vector2f(a.x + b.x, a.y + b.y);
    }

    private Vector2f subVec2f(Vector2f a, Vector2f b) {
        return new Vector2f(a.x - b.x, a.y - b.y);
    }

    private Vector2f divVec2f(Vector2f src, float fac) {
        return new Vector2f(src.x / fac, src.y / fac);
    }

    private void rotateLocalTriangle(float ang){
        final Vector2f points_center = divVec2f(addVec2f(addVec2f(triPoints[0], triPoints[1]), triPoints[2]), 3);
        for (int i = 0; i < 3; ++i)
        {
            Vector2f p = triPoints[i];
            p = subVec2f(p, points_center);

		    final float temp_x = p.x;
            final float temp_y = p.y;

            final float theta = (float)Math.toRadians(ang);
            final float c = (float)Math.cos(theta);
            final float s = (float)Math.sin(theta);

            p.x = temp_x * c - temp_y * s;
            p.y = temp_x * s + temp_y * c;

            p = addVec2f(p, points_center);

            triPoints[i] = p;
        }
    }

    private Vector2f calculateRelativeAngle(final Vector3f dest, final Vector2f viewAngs) {
        Vector3f delta = new Vector3f();
        delta = Vector3f.sub(dest, new Vector3f((float)Minecraft.getMinecraft().player.getPositionEyes(0).x,(float)Minecraft.getMinecraft().player.getPositionEyes(0).y,(float)Minecraft.getMinecraft().player.getPositionEyes(0).z), delta);
        Vector2f angles = new Vector2f(0,0);
        angles.x = (float)Math.toDegrees(Math.atan2(-delta.z, Math.hypot(delta.x, delta.y))) - viewAngs.x;
        angles.y = (float)Math.toDegrees(Math.atan2(delta.y, delta.x)) - viewAngs.y;
        //angles.normalise();
        return angles;
    }

    private Vector2f XMScalarSinCos(float val) {
        // Returns SIN COS in Vec2f
        Vector2f ret = new Vector2f();

        float quotient = 0.159154943f*val;
        if (val >= 0.0f)
        {
            quotient = (float)((int)(quotient + 0.5f));
        }
        else
        {
            quotient = (float)((int)(quotient - 0.5f));
        }
        float y = val - 6.283185307f*quotient;

        // Map y to [-pi/2,pi/2] with sin(y) = sin(Value).
        float sign;
        if (y > 1.570796327f)
        {
            y = 3.141592654f - y;
            sign = -1.0f;
        }
        else if (y < -1.570796327f)
        {
            y = -3.141592654f - y;
            sign = -1.0f;
        }
        else
        {
            sign = +1.0f;
        }

        float y2 = y * y;

        // 11-degree minimax approximation
        ret.x = ( ( ( ( (-2.3889859e-08f * y2 + 2.7525562e-06f) * y2 - 0.00019840874f ) * y2 + 0.0083333310f ) * y2 - 0.16666667f ) * y2 + 1.0f ) * y;

        // 10-degree minimax approximation
        float p = ( ( ( ( -2.6051615e-07f * y2 + 2.4760495e-05f ) * y2 - 0.0013888378f ) * y2 + 0.041666638f ) * y2 - 0.5f ) * y2 + 1.0f;
        ret.y = sign*p;

        return ret;
    }
}
