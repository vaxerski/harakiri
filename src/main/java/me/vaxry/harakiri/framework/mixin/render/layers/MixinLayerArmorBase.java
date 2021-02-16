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

    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void renderArmorLayerPre(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci){
        if(!(entityLivingBaseIn instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)entityLivingBaseIn;

        ChamsModule chamsModule = (ChamsModule)Harakiri.INSTANCE.getModuleManager().find(ChamsModule.class);
        chamsModule.lastPlayer = e;

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.INSTANCE.getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
                this.alpha = chamsModule.selfA.getValue() / 255.f;
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
                this.alpha = chamsModule.friendA.getValue() / 255.f;
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
                this.alpha = chamsModule.enemyA.getValue() / 255.f;
            }
        }
    }

    @Inject(method = "renderArmorLayer", at = @At("RETURN"), cancellable = true)
    public void renderArmorLayerPost(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo ci){
        if(!(entityLivingBaseIn instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)entityLivingBaseIn;

        ChamsModule chamsModule = (ChamsModule)Harakiri.INSTANCE.getModuleManager().find(ChamsModule.class);
        chamsModule.lastPlayer = e;

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
            Harakiri.INSTANCE.getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
                this.alpha = chamsModule.selfA.getValue() / 255.f;
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
                this.alpha = chamsModule.friendA.getValue() / 255.f;
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
                this.alpha = chamsModule.enemyA.getValue() / 255.f;
            }
        }
    }

    @Redirect(method = "renderArmorLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", remap = false))
    public void color(float r, float g, float b, float a){
        // Overwrite with our color
        ChamsModule chamsModule = (ChamsModule) Harakiri.INSTANCE.getModuleManager().find(ChamsModule.class);
        EntityPlayer e = chamsModule.lastPlayer;

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.INSTANCE.getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }
    }

    @Inject(method = "renderEnchantedGlint", at = @At("RETURN"))
    private static void renderEnchantedGlint(RenderLivingBase p_188364_0_, EntityLivingBase p_188364_1_, ModelBase model, float p_188364_3_, float p_188364_4_, float p_188364_5_, float p_188364_6_, float p_188364_7_, float p_188364_8_, float p_188364_9_, CallbackInfo ci){
        if(!(p_188364_1_ instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)p_188364_1_;

        ChamsModule chamsModule = (ChamsModule)Harakiri.INSTANCE.getModuleManager().find(ChamsModule.class);

        if(!chamsModule.isEnabled() || e == null)
            return;

        if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.INSTANCE.getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.INSTANCE.getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }
    }

}
