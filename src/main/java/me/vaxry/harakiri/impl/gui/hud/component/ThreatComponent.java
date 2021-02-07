package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.PotionUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;

import me.vaxry.harakiri.Harakiri;

import java.util.Objects;

public class ThreatComponent extends DraggableHudComponent {

    final int ModelX = 100 / 2;
    final int InfoX = 250 / 2;
    final int BoxY = 150 / 2;
    final int BorderPadding = 2 / 2;
    final int BoxX = ModelX + InfoX + BorderPadding*2;

    public ThreatComponent() {
        super("Threat");
        this.setH(BoxY);
        this.setX(BoxX);

        Harakiri.INSTANCE.getEventManager().addEventListener(this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Threat Display", this.getX() + this.getW() / 2 - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("Threat Display") / 2, this.getY() + this.getH() / 2 - Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT / 2, 0x99FFFFFF);
            return;
        }

        if (mc.player != null && mc.world != null) {

            final EntityPlayer threat = getThreat();
            if(threat == null){
                return;
            }

            // Get NPI
            final NetworkPlayerInfo playerInfo = mc.player.connection.getPlayerInfo(threat.getUniqueID());


            // Set the W and H
            this.setW(ModelX + BorderPadding * 2 + InfoX);
            this.setH(BoxY + BorderPadding * 2);

            // Render the text in the BG with 4x Scale
            GlStateManager.pushMatrix();
            GlStateManager.scale(4,4,4);
            Harakiri.INSTANCE.getTTFFontUtil().drawString("Threat",(this.getX() + BoxX/2.0f)/4 + 1 - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("Threat")/2, (this.getY() + BoxY/2.0f)/4 - Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT/2.0f + 1, 0x44000000);
            GlStateManager.scale(1,1,1);
            GlStateManager.popMatrix();



            // BG
            RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x75101010);

            if(Harakiri.INSTANCE.getFriendManager().isFriend(threat) != null) {
                RenderUtil.drawLine(this.getX(),this.getY(), this.getX() + this.getW(), this.getY(), 1, 0xFF00E6E6); // top
                RenderUtil.drawLine(this.getX(),this.getY(), this.getX(), this.getY() + this.getH(), 1, 0xFF00E6E6); // left
                RenderUtil.drawLine(this.getX(),this.getY() + this.getH(), this.getX() + this.getW(), this.getY() + this.getH(), 1, 0xFF00E6E6); // bottom
                RenderUtil.drawLine(this.getX() + this.getW(),this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 1, 0xFF00E6E6); // right
            }

            // Render the miniplayer
            GlStateManager.pushMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            mc.getTextureManager().bindTexture(playerInfo.getLocationSkin());
            GuiInventory.drawEntityOnScreen((int)(this.getX() + this.BorderPadding + this.ModelX/2), (int)(this.getY() + this.BorderPadding + this.BoxY - 2), 36, 0, 0, threat);

            GlStateManager.popMatrix();

            // Render Name
            if(Harakiri.INSTANCE.getFriendManager().isFriend(threat) != null)
                Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(threat.getName(),this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f * Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(threat.getName()), this.getY() + this.BorderPadding + Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 1, 0xFF00E6E6);
            else
                Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(threat.getName(),this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f * Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(threat.getName()), this.getY() + this.BorderPadding + Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 1, isDangerous(threat) ? 0xFFFF0000 : 0xFF11FF11);

            // Render Info
            String info = "";
            if(isDangerous(threat))
                if(threat.getTotalArmorValue() == 20)
                    info += "\247cFull Armor\2478 | ";
                else
                    info += "\2476Light Armor\2478 | ";
            else
                info += "\2472Naked\2478 | ";


            String ms = "?";
            if (Objects.nonNull(playerInfo)) {
                ms = playerInfo.getResponseTime() != 0 ? playerInfo.getResponseTime() + "ms" : "?";
            }

            info += "\247f" + ms + "\2478 | ";

            final String distance = get3DDistance(threat) + "m";

            info += "\247f" + distance;

            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(info,this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(info)/2.0f, this.getY() + this.BorderPadding + 2*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 2, 0xFFFFFFFF);

            // Render Health

            final boolean isIllegal = threat.getHealth() > 20.0f;
            final int health = (int)threat.getHealth();
            final boolean isAbsorption = threat.getAbsorptionAmount() > 0;
            final double healthPerc = isIllegal || isAbsorption ? 1.0f : health / 20.0f;
            int colorForRect;
            if(isAbsorption) colorForRect = 0xFFFFCC00;
            else if(healthPerc > 0.8f) colorForRect = 0xFF73E600;
            else if(healthPerc > 0.4f) colorForRect = 0xFFFF7733;
            else colorForRect = 0xFFE62E00;

            if(isIllegal)
                colorForRect = 0xFFFF0000;

            // Rect
            RenderUtil.drawRect(this.getX() + this.ModelX + this.BorderPadding * 2, this.getY() + this.BorderPadding + 4*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4, (int)(this.getX() + this.ModelX + this.BorderPadding + healthPerc * this.InfoX), this.getY() + this.BorderPadding + 4*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4 + Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 1, colorForRect);
            // Text
            if(!isIllegal){
                if(!isAbsorption)
                    Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(String.valueOf(health), this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f*Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(String.valueOf(health)), this.getY() + this.BorderPadding + 4*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4, 0xFFFFFFFF);
                else
                    Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(String.valueOf((int)(health + threat.getAbsorptionAmount())), this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f*Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(String.valueOf(health)), this.getY() + this.BorderPadding + 4*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4, 0xFFFFFFFF);
            } else {
                Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("ILLEGAL (" + String.valueOf(health) + ")", this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f*Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("ILLEGAL (" + String.valueOf(health) + ")"), this.getY() + this.BorderPadding + 4*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 4, 0xFFFF0000);
            }

            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow("Health", this.getX() + this.ModelX + this.BorderPadding + this.InfoX/2 - 0.5f*Harakiri.INSTANCE.getTTFFontUtil().getStringWidth("Health"), this.getY() + this.BorderPadding + 3.2f*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 3.2f, 0xFFFFFFFF);


            // Render Additional

            String moreInfo = "";

            final Item offhandItem = ((EntityLivingBase)threat).getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem();
            final Item mainHandItem = ((EntityLivingBase)threat).getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem();
            final Item chestItem = ((EntityLivingBase)threat).getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

            if(isIllegal)
                moreInfo += "\247cRUN!!!! \2478 | ";

            if(mainHandItem == Items.END_CRYSTAL || offhandItem == Items.END_CRYSTAL)
                moreInfo += "\247cCrystal \2478 | ";
            
            if(chestItem == Items.ELYTRA)
                moreInfo += "\2476Elytra \2478 | ";

            for (PotionEffect effect : threat.getActivePotionEffects()) {
                if (effect.getDuration() <= 0)
                    continue;

                final String effectString = PotionUtil.getFriendlyPotionName(effect);
                if (effectString != null) { // will return null if it doesn't exist as a valid formatted name

                    if(effectString.contains("Strength")){
                        moreInfo += "\2474Strength \2478 | ";
                    }

                    if(effectString.contains("Weakness")){
                        moreInfo += "\2471Weakness \2478 | ";
                    }
                }
            }

            if(moreInfo.endsWith("| ")){
                moreInfo = moreInfo.substring(0, moreInfo.length()-3);
            }

            Harakiri.INSTANCE.getTTFFontUtil().drawStringWithShadow(moreInfo, this.getX() + this.ModelX + this.BorderPadding + this.InfoX - Harakiri.INSTANCE.getTTFFontUtil().getStringWidth(moreInfo), this.getY() + this.BorderPadding + 6.0f*Harakiri.INSTANCE.getTTFFontUtil().FONT_HEIGHT + 6.0f, 0xFFFFFFFF);

            // Illegal Health red border
            if(isIllegal) {
                RenderUtil.drawLine(this.getX(),this.getY(), this.getX() + this.getW(), this.getY(), 1, 0xFFFF0000); // top
                RenderUtil.drawLine(this.getX(),this.getY(), this.getX(), this.getY() + this.getH(), 1, 0xFFFF0000); // left
                RenderUtil.drawLine(this.getX(),this.getY() + this.getH(), this.getX() + this.getW(), this.getY() + this.getH(), 1, 0xFFFF0000); // bottom
                RenderUtil.drawLine(this.getX() + this.getW(),this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 1, 0xFFFF0000); // right
            }

        }
    }

    private EntityPlayer getThreat(){

        float bestdist = 999999999.0f;
        EntityPlayer threat = null;

        for(Entity e : mc.world.loadedEntityList){
            if(e instanceof EntityPlayer){
                if(get3DDistance((EntityPlayer)e) < bestdist && e.getUniqueID() != mc.player.getUniqueID() && e.getEntityId() != 420420420 /* FakeLag Fake Ent */){
                    bestdist = get3DDistance((EntityPlayer)e);
                    threat = (EntityPlayer)e;
                }
            }
        }

        return threat;
        //return null;
    }

    private boolean isDangerous(EntityPlayer e) {
        if(e.getTotalArmorValue() == 0) return false;
        return true;
    }

    private int get3DDistance(EntityPlayer e) {
        return (int)(Math.sqrt(Math.pow((mc.player.posX - e.posX),2) + Math.pow((mc.player.posY - e.posY),2) + Math.pow((mc.player.posZ - e.posZ),2)));
    }
}