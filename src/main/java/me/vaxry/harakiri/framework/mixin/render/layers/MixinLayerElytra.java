package me.vaxry.harakiri.framework.mixin.render.layers;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.render.ChamsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LayerElytra.class)
public class MixinLayerElytra {

    @Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", remap = false))
    public void color(float r, float g, float b, float a){
        // Overwrite with our color
        ChamsModule chamsModule = (ChamsModule) Harakiri.get().getModuleManager().find(ChamsModule.class);
        EntityPlayer e = chamsModule.lastPlayer;

        //GlStateManager.enableBlend();
        //GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if(!chamsModule.isEnabled() || e == null) {
            GL11.glColor4f(1F,1F,1F,1F);
            return;
        }

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            //GlStateManager.enableBlend();
            //GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if(Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName())){
                GL11.glColor4f(chamsModule.selfR.getValue() / 255.f,chamsModule.selfG.getValue() / 255.f,chamsModule.selfB.getValue() / 255.f,chamsModule.selfA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) != null){
                //friend settings
                GL11.glColor4f(chamsModule.friendR.getValue() / 255.f,chamsModule.friendG.getValue() / 255.f,chamsModule.friendB.getValue() / 255.f,chamsModule.friendA.getValue() / 255.f);
            } else if(Harakiri.get().getFriendManager().isFriend(e) == null){
                //enemy settings
                GL11.glColor4f(chamsModule.enemyR.getValue() / 255.f,chamsModule.enemyG.getValue() / 255.f,chamsModule.enemyB.getValue() / 255.f,chamsModule.enemyA.getValue() / 255.f);
            }
        }else{
            GL11.glColor4f(1F,1F,1F,1F);
        }
    }

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/LayerArmorBase;renderEnchantedGlint(Lnet/minecraft/client/renderer/entity/RenderLivingBase;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/model/ModelBase;FFFFFFF)V"),remap = false)
    public void renderEnchantedGlint(RenderLivingBase rlb, EntityLivingBase elb, ModelBase mb, float p_188364_3_, float p_188364_4_, float p_188364_5_, float p_188364_6_, float p_188364_7_, float p_188364_8_, float p_188364_9_, CallbackInfo ci){
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
    }
}
