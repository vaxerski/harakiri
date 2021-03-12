package me.vaxry.harakiri.framework.mixin.render.layers;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.render.ChamsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LayerArmorBase.class)
public class MixinLayerArmorBase {

    @Shadow
    float alpha;

    @Shadow
    float colorR;

    @Shadow
    float colorB;

    @Shadow
    float colorG;


    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void renderArmorLayerPre(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci){
        if(!(entityLivingBaseIn instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)entityLivingBaseIn;

        ChamsModule chamsModule = (ChamsModule)Harakiri.get().getModuleManager().find(ChamsModule.class);
        chamsModule.lastPlayer = e;

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if(!chamsModule.isEnabled() || e == null) {
            GL11.glColor4f(1,1,1,1);
            alpha = 1;
            colorR = 1;
            colorB = 1;
            colorG = 1;
            return;
        }

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
                this.alpha = chamsModule.selfA.getValue() > 250 ? 1.f : chamsModule.selfA.getValue() / 255.f;
                if(chamsModule.selfNoAr.getValue())
                    ci.cancel();
                this.colorR = chamsModule.selfR.getValue() / 255f;
                this.colorG = chamsModule.selfG.getValue() / 255f;
                this.colorB = chamsModule.selfB.getValue() / 255f;
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
                this.alpha = chamsModule.friendA.getValue() > 250 ? 1.f : chamsModule.selfA.getValue() / 255.f;
                if(chamsModule.friendNoAr.getValue())
                    ci.cancel();
                this.colorR = chamsModule.friendR.getValue() / 255f;
                this.colorG = chamsModule.friendG.getValue() / 255f;
                this.colorB = chamsModule.friendB.getValue() / 255f;
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
                this.alpha = chamsModule.enemyA.getValue() > 250 ? 1.f : chamsModule.selfA.getValue() / 255.f;
                if(chamsModule.enemyNoAr.getValue())
                    ci.cancel();
                this.colorR = chamsModule.enemyR.getValue() / 255f;
                this.colorG = chamsModule.enemyG.getValue() / 255f;
                this.colorB = chamsModule.enemyB.getValue() / 255f;
            }
        }else{
            alpha = 1;
            colorR = 1;
            colorB = 1;
            colorG = 1;
            GL11.glColor4f(1,1,1,1);
        }
    }

    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void doRenderLayerPre(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if(!(entitylivingbaseIn instanceof EntityPlayer))
            return;
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
    }

    @Inject(method = "doRenderLayer", at = @At("RETURN"), cancellable = true)
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        if(!(entitylivingbaseIn instanceof EntityPlayer))
            return;
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1,1,1,1);
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    /*@Inject(method = "renderArmorLayer", at = @At("RETURN"), cancellable = true)
    public void renderArmorLayerPost(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci){
        if(!(entityLivingBaseIn instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)entityLivingBaseIn;

        ChamsModule chamsModule = (ChamsModule)Harakiri.get().getModuleManager().find(ChamsModule.class);
        chamsModule.lastPlayer = e;

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
            Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
                this.alpha = chamsModule.selfA.getValue() / 255.f;
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
                this.alpha = chamsModule.friendA.getValue() / 255.f;
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
                this.alpha = chamsModule.enemyA.getValue() / 255.f;
            }
        }
    }*/

    /*@Redirect(method = "renderArmorLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", remap = false))
    public void color(float r, float g, float b, float a){
        // Overwrite with our color
        ChamsModule chamsModule = (ChamsModule) Harakiri.get().getModuleManager().find(ChamsModule.class);
        EntityPlayer e = chamsModule.lastPlayer;

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }
    }*/

    @Inject(method = "renderEnchantedGlint", at = @At("HEAD"))
    private static void renderEnchantedGlintPre(RenderLivingBase p_188364_0_, EntityLivingBase p_188364_1_, ModelBase model, float p_188364_3_, float p_188364_4_, float p_188364_5_, float p_188364_6_, float p_188364_7_, float p_188364_8_, float p_188364_9_, CallbackInfo ci) {
        if(!(p_188364_1_ instanceof EntityPlayer))
            return;
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
    }

    /*@Inject(method = "renderEnchantedGlint", at = @At("RETURN"))
    private static void renderEnchantedGlint(RenderLivingBase p_188364_0_, EntityLivingBase p_188364_1_, ModelBase model, float p_188364_3_, float p_188364_4_, float p_188364_5_, float p_188364_6_, float p_188364_7_, float p_188364_8_, float p_188364_9_, CallbackInfo ci){
        if(!(p_188364_1_ instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)p_188364_1_;

        ChamsModule chamsModule = (ChamsModule)Harakiri.get().getModuleManager().find(ChamsModule.class);

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }
    }*/

}
