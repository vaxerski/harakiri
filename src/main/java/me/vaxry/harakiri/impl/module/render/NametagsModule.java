package me.vaxry.harakiri.impl.module.render;


import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import jdk.nashorn.internal.ir.Block;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.render.EventRender2D;
import me.vaxry.harakiri.api.event.render.EventRenderName;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.GLUProjection;
import me.vaxry.harakiri.api.util.ItemUtil;
import me.vaxry.harakiri.api.util.RenderUtil;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.locationtech.jts.geom.Coordinate;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.vecmath.Vector3d;
import java.util.*;

public final class NametagsModule extends Module {

    private float NAMETAG_SAFEAREA = 1.f;
    private ICamera camera = new Frustum();;

    public final Value<Float> additionalScale = new Value<Float>("Scale", new String[]{"Scale", "s"}, "Scale the nametag", 1.f, 0.5f, 2.5f, 0.5f);


    public NametagsModule() {
        super("Nametags", new String[]{"Nametags"}, "Adds custom nametags for players.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRender2D(EventRender2D event){

        Minecraft mc = Minecraft.getMinecraft();

        if(!this.isEnabled())
            return;
        if(mc.world == null || mc.player == null)
            return;

        for(Entity ent : mc.world.getLoadedEntityList()){
            if(!(ent instanceof EntityPlayer))
                continue;

            if(ent == mc.player)
                continue;

            BlockPos blockPos = (ent).getPosition();

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

            AxisAlignedBB bb = new AxisAlignedBB(
                    blockPos.getX() - mc.getRenderManager().viewerPosX,
                    blockPos.getY() - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                    blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                    blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);


            if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                blockPos = new BlockPos(ent.getPosition().getX(), ent.getPosition().getY() + 1, ent.getPosition().getZ());
                bb = new AxisAlignedBB(
                        blockPos.getX() - mc.getRenderManager().viewerPosX,
                        blockPos.getY() - mc.getRenderManager().viewerPosY,
                        blockPos.getZ() - mc.getRenderManager().viewerPosZ,
                        blockPos.getX() + 1 - mc.getRenderManager().viewerPosX,
                        blockPos.getY() + 1 - mc.getRenderManager().viewerPosY,
                        blockPos.getZ() + 1 - mc.getRenderManager().viewerPosZ);
                if (!camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                        bb.minY + mc.getRenderManager().viewerPosY,
                        bb.minZ + mc.getRenderManager().viewerPosZ,
                        bb.maxX + mc.getRenderManager().viewerPosX,
                        bb.maxY + mc.getRenderManager().viewerPosY,
                        bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                    continue;
                }
            }

            EntityPlayer e = (EntityPlayer)ent;

            Vec3d nametagvec = new Vec3d(e.getPositionVector().x, e.getPositionVector().y + e.eyeHeight, e.getPositionVector().z);

            Coordinate nametagMiddle = conv3Dto2DSpace(nametagvec.x, nametagvec.y + 0.67334f, nametagvec.z);

            // Nametag string setup
            String nametagstr = "";

            int health = Math.round(e.getHealth());
            health += e.getAbsorptionAmount();

            if(health > 20)
                nametagstr += "\2472\247o" + health;
            else if(health > 13)
                nametagstr += "\2472" + health;
            else if(health > 7)
                nametagstr += "\2476" + health;
            else
                nametagstr += "\247c" + health;

            nametagstr += "\247f ";

            if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null)
                nametagstr += "\2473" + e.getName();
            else
                nametagstr += "\247f" + e.getName();

            nametagstr += "\247f ";

            final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(mc.player.getUniqueID());
            int ping = -1;
            if (Objects.nonNull(playerInfo)) {
                if(playerInfo.getResponseTime() != 0)
                    ping = playerInfo.getResponseTime();
            }

            if(ping == -1)
                nametagstr += "\2478?";
            else if (ping > 200)
                nametagstr += "\247c" + ping + "ms";
            else if(ping > 100)
                nametagstr += "\2476" + ping + "ms";
            else if(ping > 50)
                nametagstr += "\2472" + ping + "ms";
            else
                nametagstr += "\247a" + ping + "ms";

            double distancetoent = get3DDistance(e);

            float nametagX = 0;
            float nametagY = 0;
            float scale = 0;
            float textLength = 0;
            float xoffset = 0;

            if(distancetoent > 5.f) {
                // draw without 3D scaling

                scale = 1.0f - (float)distancetoent / 500.f;
                scale *= additionalScale.getValue();

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);

                nametagX = (float)nametagMiddle.x;
                nametagY = (float)nametagMiddle.y - (float)distancetoent / 10.f;

                if(distancetoent > 80)
                    nametagY = (float)nametagMiddle.y - (float)80 / 10;

                nametagX /= scale;
                nametagY /= scale;

                textLength = mc.fontRenderer.getStringWidth(nametagstr);
                nametagX -= textLength / 2.f;
            }else{
                // draw with 3D scaling

                float strwidth = mc.fontRenderer.getStringWidth(nametagstr) / 43.f; // real units

                float playerYaw = (float)Math.toRadians(mc.player.rotationYaw);

                //float x1 = (float)(Math.cos(playerYaw) * 2) / strwidth;
                //float y1 = (float)(Math.sin(playerYaw) * 2) / strwidth;

                Coordinate nametagPoint = new Coordinate(nametagvec.x, nametagvec.z);

                Coordinate point = new Coordinate((float)nametagvec.x + strwidth / 2.f, (float)nametagvec.z);

                Coordinate x1 = rotate_point(nametagPoint, point, playerYaw);
                Coordinate y1;
                if(playerYaw + 3.14f > 3.14f)
                    y1 = rotate_point(nametagPoint, point, playerYaw - 3.14f);
                else
                    y1 = rotate_point(nametagPoint, point, playerYaw + 3.14f);

                Coordinate right = conv3Dto2DSpace(x1.x, nametagvec.y + 0.67334f,x1.y);
                Coordinate left = conv3Dto2DSpace(y1.x, nametagvec.y + 0.67334f,y1.y);

                float scaledwidth = (float)Math.abs(right.x - left.x);

                scale = scaledwidth / mc.fontRenderer.getStringWidth(nametagstr);
                scale *= additionalScale.getValue();

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);

                textLength = mc.fontRenderer.getStringWidth(nametagstr);

                if(left.x > right.x){
                    right.x /= scale;
                    right.y /= scale;

                    nametagX = (float)right.x + xoffset;
                    nametagY = (float)right.y;
                }else{
                    left.x /= scale;
                    left.y /= scale;

                    nametagX = (float)left.x + xoffset;
                    nametagY = (float)left.y;
                }

                xoffset = 2.9f;

            }

            // Draw basic nametag

            RenderUtil.drawRect(nametagX - NAMETAG_SAFEAREA + xoffset, nametagY, nametagX + textLength + xoffset + NAMETAG_SAFEAREA, nametagY + mc.fontRenderer.FONT_HEIGHT + 2 * NAMETAG_SAFEAREA, 0x551d1d1d);
            mc.fontRenderer.drawStringWithShadow(nametagstr, nametagX + xoffset, nametagY + NAMETAG_SAFEAREA, 0xFFDDDDDD);

            GlStateManager.scale(1/scale, 1/scale, 1/scale);
            GlStateManager.popMatrix();

            
        }
    }

    @Listener
    public void renderName(EventRenderName event) {
        if(!this.isEnabled())
            return;
        if (event.getEntity() instanceof EntityPlayer) {
            event.setCanceled(true);
        }
    }

    Coordinate rotate_point(Coordinate around, Coordinate point, float theta) {
        float p1x = (float)(Math.cos(theta) * (point.x - around.x) - Math.sin(theta) * (point.y - around.y) + around.x);
        float p2x = (float)(Math.sin(theta) * (point.x - around.x) + Math.cos(theta) * (point.y - around.y) + around.y);
        return new Coordinate(p1x, p2x);
    }

    private Coordinate conv3Dto2DSpace(double x, double y, double z) {
        final GLUProjection.Projection projection = GLUProjection.getInstance().project(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

        final Coordinate returns = new Coordinate(projection.getX(), projection.getY());

        return returns;
    }

    private int get3DDistance(EntityPlayer e) {
        return (int)(Math.sqrt(Math.pow((Minecraft.getMinecraft().player.posX - e.posX),2) + Math.pow((Minecraft.getMinecraft().player.posY - e.posY),2) + Math.pow((Minecraft.getMinecraft().player.posZ - e.posZ),2)));
    }

}
