package me.vaxry.harakiri.impl.module.render;


import akka.japi.Pair;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.event.render.EventRenderName;
import me.vaxry.harakiri.framework.event.world.EventRemoveEntity;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.vecmath.Vector3d;
import java.util.*;

public final class NametagsModule extends Module {

    private float NAMETAG_SAFEAREA = 1.f;
    private ICamera camera = new Frustum();

    private HashMap<EntityPlayer, Float> playersList = new HashMap<>();

    public final Value<Float> additionalScale = new Value<Float>("Scale", new String[]{"Scale", "s"}, "Scale the nametag", 1.f, 0.5f, 2.5f, 0.5f);
    public final Value<Float> armorscale = new Value<Float>("ArmorScale", new String[]{"Armorscale", "as"}, "Scale the armor part", 1.f, 0.5f, 2.5f, 0.5f);
    public final Value<Integer> fadein = new Value<Integer>("FadeInSpeed", new String[]{"Fadein", "f"}, "Background fade in speed. Doesnt work for items cuz I'm lazy and minecraft.", 5, 1, 25, 1);
    public final Value<Boolean> simplified = new Value<Boolean>("Simplified", new String[]{"Simplified", "sm"}, "Simplifies the nametag (shader-safe)", false);


    public NametagsModule() {
        super("Nametags", new String[]{"Nametags"}, "Adds custom nametags for players.", "NONE", -1, ModuleType.RENDER);
        Minecraft mc = Minecraft.getMinecraft();
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
            
            if (simplified.getValue())
                drawNametag(((EntityPlayer) ent).getName(), 0xFFFFFFFF, ent, event, true);
            else
                drawNametag((EntityPlayer)ent, event);
        }
    }

    @Listener
    public void onEntityRemove(EventRemoveEntity event) {
        if(event.getEntity() instanceof EntityPlayer){
            playersList.remove(event.getEntity());
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
        double p1x = (Math.cos(theta) * (point.x - around.x) - Math.sin(theta) * (point.y - around.y) + around.x);
        double p2x = (Math.sin(theta) * (point.x - around.x) + Math.cos(theta) * (point.y - around.y) + around.y);
        return new Coordinate(p1x, p2x);
    }

    private Coordinate conv3Dto2DSpace(double x, double y, double z) {
        final GLUProjection.Projection projection = GLUProjection.getInstance().project(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

        return projection.getType() == GLUProjection.Projection.Type.OUTSIDE || projection.getType() == GLUProjection.Projection.Type.INVERTED ? null : new Coordinate(projection.getX(), projection.getY());
    }

    private Coordinate conv3Dto2DSpaceForceOutside(double x, double y, double z) {
        final GLUProjection.Projection projection = GLUProjection.getInstance().project(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

        return projection.getType() == GLUProjection.Projection.Type.INVERTED ? null : new Coordinate(projection.getX(), projection.getY());
    }

    private int get3DDistance(Entity e) {
        return (int)(Math.sqrt(Math.pow((Minecraft.getMinecraft().player.posX - e.posX),2) + Math.pow((Minecraft.getMinecraft().player.posY - e.posY),2) + Math.pow((Minecraft.getMinecraft().player.posZ - e.posZ),2)));
    }

    private boolean isOutOfScreen(Coordinate x) {
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        if((x.x < 0 || x.x > res.getScaledWidth()) && (x.y < 0 || x.y > res.getScaledHeight()))
            return true;
        return false;
    }

    private Vector3d calculateRelativeAngle(final Vec3d source, final Vec3d destination, final Vector3d viewAngles) {
        Vector3d delta = new Vector3d();
        delta.x = destination.x - source.x;
        delta.y = destination.y - source.y;
        delta.z = destination.z - source.z;
        Vector3d angles = new Vector3d(Math.toDegrees(Math.atan2(-delta.z, Math.hypot(delta.x, delta.y))) - viewAngles.x,
            Math.toDegrees(Math.atan2(delta.y, delta.x)) - viewAngles.y, 0);
        angles.normalize();
        return angles;
    }

    private boolean isPlayerCached(EntityPlayer e){
        for(Map.Entry<EntityPlayer, Float> p : playersList.entrySet()){
            if(p.getKey() == e)
                return true;
        }
        return false;
    }

    public void drawNametag(EntityPlayer e, EventRender2D event) {

        if (!isPlayerCached(e))
            playersList.put(e, 0.01f);

        if (Harakiri.get().getFriendManager().isFriend(e) != null) {
            drawNametag(e.getName(), 0xFF80FFFF, e, event, false);
        } else {
            drawNametag(e.getName(), 0xFFFFFFFF, e, event, false);
        }
    }

    public void drawNametag(String name, Integer color, Entity ent, EventRender2D event, boolean onlyName) {
        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos blockPos = (ent).getPosition();

        camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);

        ArrayList<Pair<String, Integer>> toDraw = new ArrayList<>();

        for (Map.Entry<EntityPlayer, Float> p : playersList.entrySet()) {
            if (p.getValue() < 0.01f)
                p.setValue(0.02f);
            p.setValue(Math.min(p.getValue() + fadein.getValue() / 3.f, 255));
        }

        float alphaPerc = onlyName ? 1 : playersList.get(ent) / 255.f;

        Vec3d nametagvec = new Vec3d(ent.getPositionEyes(event.getPartialTicks()).x,
                ent.getPositionEyes(event.getPartialTicks()).y, ent.getPositionEyes(event.getPartialTicks()).z);

        Coordinate nametagMiddle = conv3Dto2DSpace(nametagvec.x, nametagvec.y + 0.67334f, nametagvec.z);
        if (nametagMiddle == null)
            return;

        double distancetoent = get3DDistance(ent);

        // Nametag string setup
        String nametagstr = "";

        if (!onlyName) {
            EntityPlayer e = (EntityPlayer)ent;
            int health = Math.round(e.getHealth());
            health += e.getAbsorptionAmount();

            if (health > 20) {
                nametagstr += "\2472\247o" + health;
                toDraw.add(new Pair<>(health + " ", 0xFF11DD11));
            } else if (health > 13) {
                nametagstr += "\2472" + health;
                toDraw.add(new Pair<>(health + " ", 0xFF11DD11));
            } else if (health > 7) {
                nametagstr += "\2476" + health;
                toDraw.add(new Pair<>(health + " ", 0xFFAAAA00));
            } else {
                nametagstr += "\247c" + health;
                toDraw.add(new Pair<>(health + " ", 0xFFFF3333));
            }

            nametagstr += "\247f ";
        }
    
        nametagstr += name;
        toDraw.add(new Pair<>(name + " ", color));

        if (!onlyName) {
            EntityPlayer e = (EntityPlayer) ent;
            nametagstr += "\247f ";

            final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(e.getUniqueID());
            int ping = -1;
            if (Objects.nonNull(playerInfo)) {
                if (playerInfo.getResponseTime() != 0)
                    ping = playerInfo.getResponseTime();
            }

            if (ping == -1) {
                nametagstr += "\2478?";
                toDraw.add(new Pair<>("?", 0xFF888888));
            } else if (ping > 200) {
                nametagstr += "\247c" + ping + "ms";
                toDraw.add(new Pair<>(ping + "ms", 0xFFFF5050));
            } else if (ping > 100) {
                nametagstr += "\2476" + ping + "ms";
                toDraw.add(new Pair<>(ping + "ms", 0xFFFFCC00));
            } else if (ping > 50) {
                nametagstr += "\2472" + ping + "ms";
                toDraw.add(new Pair<>(ping + "ms", 0xFFCCFF33));
            } else {
                nametagstr += "\247a" + ping + "ms";
                toDraw.add(new Pair<>(ping + "ms", 0xFF66FF66));
            }
        }

        float nametagX = 0;
        float nametagY = 0;
        float scale = 0;
        float textLength = 0;
        float xoffset = 0;
        Coordinate nametagMiddleNew = new Coordinate(0, 0);

        if (distancetoent > 5.f) {
            // draw without 3D scaling

            scale = 1.0f - (float) distancetoent / 500.f;
            scale *= additionalScale.getValue();

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);

            nametagX = (float) nametagMiddle.x;
            nametagY = (float) nametagMiddle.y - (float) distancetoent / 10.f;

            if (distancetoent > 80)
                nametagY = (float) nametagMiddle.y - (float) 80 / 10;

            nametagX /= scale;
            nametagY /= scale;

            nametagMiddleNew.x = nametagX;
            nametagMiddleNew.y = nametagY;

            textLength = Harakiri.get().getTTFFontUtil().getStringWidth(nametagstr);
            nametagX -= textLength / 2.f;
        } else {
            // draw with 3D scaling

            float strwidth = Harakiri.get().getTTFFontUtil().getStringWidth(nametagstr) / 43.f; // real units

            float playerYaw = (float) Math.toRadians(mc.player.rotationYaw);

            // float x1 = (float)(Math.cos(playerYaw) * 2) / strwidth;
            // float y1 = (float)(Math.sin(playerYaw) * 2) / strwidth;

            Coordinate nametagPoint = new Coordinate(nametagvec.x, nametagvec.z);

            Coordinate point = new Coordinate(nametagvec.x + strwidth / 2.f, nametagvec.z);

            Coordinate x1 = rotate_point(nametagPoint, point, playerYaw);
            Coordinate y1;
            if (playerYaw + 3.14f > 3.14f)
                y1 = rotate_point(nametagPoint, point, playerYaw - 3.14f);
            else
                y1 = rotate_point(nametagPoint, point, playerYaw + 3.14f);

            Coordinate right = conv3Dto2DSpace(x1.x, nametagvec.y + 0.67334f, x1.y);
            Coordinate left = conv3Dto2DSpace(y1.x, nametagvec.y + 0.67334f, y1.y);

            if (right == null && left == null) {
                return;
            }

            float scaledwidth;

            if (right == null) {
                scaledwidth = (float) (2 * Math.abs(nametagMiddle.x - left.x));
                right = new Coordinate(left.x - scaledwidth, left.y);
            } else if (left == null) {
                scaledwidth = (float) (2 * Math.abs(right.x - nametagMiddle.x));
                left = new Coordinate(right.x + scaledwidth, right.y);
            } else {
                scaledwidth = (float) Math.abs(right.x - left.x);
            }

            scale = scaledwidth / Harakiri.get().getTTFFontUtil().getStringWidth(nametagstr);
            scale *= additionalScale.getValue();

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);

            textLength = Harakiri.get().getTTFFontUtil().getStringWidth(nametagstr);

            right.x /= scale;
            right.y /= scale;
            left.x /= scale;
            left.y /= scale;

            nametagMiddleNew.x = Math.abs(right.x + left.x) / 2.f;
            nametagMiddleNew.y = left.y;

            if (left.x > right.x) {
                nametagY = (float) right.y;
            } else {
                nametagY = (float) left.y;
            }

            nametagX = (float) nametagMiddleNew.x - textLength / 2.f;

            // xoffset = 2.9f * this.additionalScale.getValue();
        }

        // Draw basic nametag

        RenderUtil.drawRect(nametagX - NAMETAG_SAFEAREA + xoffset, nametagY,
                nametagX + textLength + xoffset + NAMETAG_SAFEAREA,
                nametagY + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 2 * NAMETAG_SAFEAREA,
                ColorUtil.changeAlpha(0x551D1D1D, (int) (alphaPerc * 85)));

        // Harakiri.get().getTTFFontUtil().drawStringWithShadow(nametagstr, nametagX +
        // xoffset, nametagY + NAMETAG_SAFEAREA, (int)(0xFF * alphaPerc) * 0x1000000 +
        // 0xDDDDDD);

        // New rendering
        float xoff = 0;
        GlStateManager.enableBlend();
        for (int iter = 0; iter < toDraw.size(); iter++) {
            Pair<String, Integer> entry = toDraw.get(iter);
            xoff += Harakiri.get().getTTFFontUtil().drawStringWithShadow(entry.first(), nametagX + xoffset + xoff,
                    nametagY + NAMETAG_SAFEAREA, (int) (0xFF * alphaPerc) * 0x1000000 + entry.second());
        }
        GlStateManager.disableBlend();

        // Draw Armor and stuff

        if (!onlyName) {
            EntityPlayer e = (EntityPlayer) ent;

            final Iterator<ItemStack> items = e.getEquipmentAndArmor().iterator();
            final ArrayList<ItemStack> stacks = new ArrayList<>();

            final ItemStack offhandItem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
            final ItemStack mainHandItem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            final ItemStack feetitem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.FEET);
            final ItemStack legitem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.LEGS);
            final ItemStack chestItem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            final ItemStack headitem = ((EntityLivingBase) e).getItemStackFromSlot(EntityEquipmentSlot.HEAD);

            stacks.add(offhandItem);
            stacks.add(feetitem);
            stacks.add(legitem);
            stacks.add(chestItem);
            stacks.add(headitem);
            stacks.add(mainHandItem);

            final boolean[] toRemove = new boolean[6];

            for (int i = 0; i < stacks.size(); ++i) {
                if (stacks.get(i).getItem() == Items.AIR)
                    toRemove[i] = true;
            }

            for (int i = 5; i >= 0; --i) {
                if (toRemove[i])
                    stacks.remove(i);
            }

            // One stack is 16
            //

            nametagMiddleNew.x /= armorscale.getValue();
            nametagMiddleNew.y /= armorscale.getValue();

            GlStateManager.scale(armorscale.getValue(), armorscale.getValue(), armorscale.getValue());

            nametagMiddleNew.y -= NAMETAG_SAFEAREA * 5 + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 16;

            float rectWidth = NAMETAG_SAFEAREA * 2 + stacks.size() * 16
                    + NAMETAG_SAFEAREA * Math.max(stacks.size() - 1, 0);
            if (stacks.size() == 0)
                rectWidth = 0;
            RenderUtil.drawRect((float) nametagMiddleNew.x - rectWidth / 2.f,
                    (float) nametagMiddleNew.y + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 2 * NAMETAG_SAFEAREA,
                    (float) nametagMiddleNew.x + rectWidth / 2.f,
                    (float) nametagMiddleNew.y + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 16
                            + 4 * NAMETAG_SAFEAREA,
                    ColorUtil.changeAlpha(0x551D1D1D, (int) (alphaPerc * 85)));
            // BG drawn.

            float currentX = 0;

            for (ItemStack stack : stacks) {
                if (stack != null) {
                    final Item item = stack.getItem();
                    if (item != Items.AIR) {
                        GlStateManager.pushMatrix();
                        GlStateManager.enableBlend();
                        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.translate(nametagMiddleNew.x - rectWidth / 2.f + NAMETAG_SAFEAREA + currentX,
                                nametagMiddleNew.y + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 3 * NAMETAG_SAFEAREA,
                                0);

                        Harakiri.get().getRenderItemAlpha().alpha = alphaPerc;
                        Harakiri.get().getRenderItemAlpha().renderItemAndEffectIntoGUI(mc.player, stack, 0, 0);
                        Harakiri.get().getRenderItemAlpha().renderItemOverlayIntoGUI(mc.fontRenderer, stack, 0, 0,
                                (String) null);

                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.disableBlend();
                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.popMatrix();

                        currentX += 16 + NAMETAG_SAFEAREA;
                    }
                }
            }

            float nameScale = 0.8f;

            nametagMiddleNew.x /= 0.8f;
            nametagMiddleNew.y /= 0.8f;

            GlStateManager.scale(nameScale, nameScale, nameScale);

            // Render in-hand item name

            String itemName = mainHandItem.getDisplayName();

            if (mainHandItem.getItem() == Items.AIR)
                itemName = "";

            nametagMiddleNew.y -= Harakiri.get().getTTFFontUtil().FONT_HEIGHT / 2.f;
            float nameRectWidth = NAMETAG_SAFEAREA * 2 + Harakiri.get().getTTFFontUtil().getStringWidth(itemName);

            // RenderUtil.drawRect((float)nametagMiddleNew.x - nameRectWidth / 2.f,
            // (float)nametagMiddleNew.y + Harakiri.get().getTTFFontUtil().FONT_HEIGHT +
            // NAMETAG_SAFEAREA, (float)nametagMiddleNew.x + nameRectWidth / 2.f,
            // (float)nametagMiddleNew.y - NAMETAG_SAFEAREA, 0x551d1d1d);

            // New rendering
            GlStateManager.enableBlend();
            Harakiri.get().getTTFFontUtil().drawStringWithShadow(itemName,
                    (float) nametagMiddleNew.x - nameRectWidth / 2.f + NAMETAG_SAFEAREA,
                    (float) nametagMiddleNew.y + Harakiri.get().getTTFFontUtil().FONT_HEIGHT,
                    (int) (0xFF * alphaPerc) * 0x1000000 + 0xDDDDDD);
            GlStateManager.disableBlend();

            GlStateManager.scale(1 / nameScale, 1 / nameScale, 1 / nameScale);
            GlStateManager.scale(1 / armorscale.getValue(), 1 / armorscale.getValue(), 1 / armorscale.getValue());
        }

        GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);

        GlStateManager.popMatrix();
    }

}
