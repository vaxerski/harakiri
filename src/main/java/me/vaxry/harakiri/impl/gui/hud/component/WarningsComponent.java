package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class WarningsComponent extends DraggableHudComponent {

    protected Texture warningPNG;
    private int SAFEZONE_X = 3;
    private int SAFEZONE_Y = 0;

    public WarningsComponent() {
        super("Warnings");

        warningPNG = new Texture("warning128.png");

        this.setX(200);
        this.setY(200);

        Harakiri.get().getEventManager().addEventListener(this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX,mouseY,partialTicks);

        if(!this.isVisible())
            return;

        // Check how many warnings we get, get the strings.

        final ItemStack headItem = ((EntityLivingBase)mc.player).getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        final ItemStack chestItem = ((EntityLivingBase)mc.player).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        final ItemStack legItem = ((EntityLivingBase)mc.player).getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        final ItemStack feetItem = ((EntityLivingBase)mc.player).getItemStackFromSlot(EntityEquipmentSlot.FEET);

        int lowItems = 0;
        if(headItem.getItemDamage() > headItem.getMaxDamage() * 0.6f && headItem.isItemStackDamageable()){
            lowItems += 1;
        }
        if(chestItem.getItemDamage() > chestItem.getMaxDamage() * 0.6f && headItem.isItemStackDamageable() && chestItem.getItem() != Items.ELYTRA){
            lowItems += 1;
        }
        if(legItem.getItemDamage() > legItem.getMaxDamage() * 0.6f && headItem.isItemStackDamageable()){
            lowItems += 1;
        }
        if(feetItem.getItemDamage() > feetItem.getMaxDamage() * 0.6f && headItem.isItemStackDamageable()){
            lowItems += 1;
        }

        boolean brokenElytra = false;
        boolean lowElytra = false;
        // Check for broken elytra
        if(chestItem.getItem() == Items.ELYTRA){
            if(chestItem.getItemDamage() == chestItem.getMaxDamage()){
                brokenElytra = true;
            }else if(chestItem.getItemDamage() > chestItem.getMaxDamage() * 0.8f){
                lowElytra = true;
            }
        }

        // Check for totems
        boolean noTotems = getTotemCount() == 0;

        // Calculate and draw.
        ArrayList<String> warns = new ArrayList<>();

        if(lowItems > 0){
            warns.add("You have " + lowItems + " low armor pieces.");
        }
        if(brokenElytra){
            warns.add("Your elytra is broken.");
        }
        if(lowElytra){
            warns.add("Your elytra is low.");
        }
        if(noTotems){
            warns.add("You have no totems.");
        }

        float maxX = 0;

        if(warns.isEmpty() && mc.currentScreen instanceof GuiHudEditor){
            warns.add("(Warnings display)");
        }else if(warns.isEmpty()){
            return;
            // No need to draw shit when its empty and *not* in hud editor
        }

        for(String warn : warns){
            float w = Harakiri.get().getTTFFontUtil().getStringWidth(warn);
            if(w > maxX)
                maxX = w;
        }

        float maxY = 2;
        for(String warn : warns){
            float h = Harakiri.get().getTTFFontUtil().FONT_HEIGHT;
            maxY += h + 1;
        }

        float ICON_SIZE = Harakiri.get().getTTFFontUtil().FONT_HEIGHT;
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        maxX += ICON_SIZE; // PNG Exclamation point

        this.setW(maxX);
        this.setH(maxY);

        float offY = 1;
        if(this.getX() > res.getScaledWidth() / 2.f) {

            this.setW(-maxX);
            // Because this is cursed but works
            this.setX(Math.min(res.getScaledWidth(), this.getX()));

            RenderUtil.drawRect(this.getX() - this.SAFEZONE_X + this.getW(), this.getY() - this.SAFEZONE_Y, this.getX() /*No safezone cuz its ok*/, this.getY() + this.getH() + this.SAFEZONE_Y, 0x44000000);
            for (String warn : warns) {
                // Draw text
                Harakiri.get().getTTFFontUtil().drawStringWithShadow(warn, this.getX() -
                        Harakiri.get().getTTFFontUtil().getStringWidth(warn) - ICON_SIZE - 2, this.getY() + offY, 0xFFFFFFFF);

                GlStateManager.enableAlpha();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableBlend();

                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                this.warningPNG.bind();
                this.warningPNG.render(this.getX() - ICON_SIZE - 1, this.getY() + offY, ICON_SIZE, ICON_SIZE);

                GlStateManager.disableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();

                offY += Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1;
            }
        }else{
            RenderUtil.drawRect(this.getX() /*No safezone cuz its ok*/, this.getY() - this.SAFEZONE_Y, this.getX() + this.getW() + this.SAFEZONE_X, this.getY() + this.getH() + this.SAFEZONE_Y, 0x44000000);

            for (String warn : warns) {
                // Draw text
                Harakiri.get().getTTFFontUtil().drawStringWithShadow(warn, this.getX() + ICON_SIZE + 2, this.getY() + offY, 0xFFFFFFFF);

                GlStateManager.enableAlpha();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableBlend();

                GlStateManager.enableTexture2D();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                this.warningPNG.bind();
                this.warningPNG.render(this.getX() + 1, this.getY() + offY, ICON_SIZE, ICON_SIZE);

                GlStateManager.disableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();

                offY += Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1;
            }
        }
    }

    @Override
    public boolean isMouseInside(int mouseX, int mouseY) {
        if(this.getW() > 0)
            return mouseX >= this.getX() && mouseX <= this.getX() + this.getW() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
        else
            return mouseX >= this.getX() + this.getW() && mouseX <= this.getX() && mouseY >= this.getY() && mouseY <= this.getY() + this.getH();
    }

    private int getTotemCount() {
        int totems = 0;
        for (int i = 0; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }
        return totems;
    }
}
