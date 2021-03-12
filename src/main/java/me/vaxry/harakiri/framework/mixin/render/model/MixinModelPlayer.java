package me.vaxry.harakiri.framework.mixin.render.model;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.render.ChamsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer {

    @Shadow
    ModelRenderer bipedLeftArmwear;
    @Shadow
    ModelRenderer bipedRightArmwear;
    @Shadow
    ModelRenderer bipedLeftLegwear;
    @Shadow
    ModelRenderer bipedRightLegwear;
    @Shadow
    ModelRenderer bipedBodyWear;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", remap = false))
    public void pushMatrix(){
        //GlStateManager.enableAlpha();
        //GlStateManager.enableBlend();
        //GlStateManager.color(1.f,0.11f,1f,0.2f); // TEST
        GL11.glPushMatrix();
    }

    @Inject(method = "renderCape", at = @At("HEAD"), cancellable = true)
    public void renderCape(float scale, CallbackInfo ci){
        // Overwrite with our color
        ChamsModule chamsModule = (ChamsModule) Harakiri.get().getModuleManager().find(ChamsModule.class);
        EntityPlayer e = chamsModule.lastPlayer;

        if(!chamsModule.isEnabled() || e == null) {
            GL11.glColor4f(1,1,1,1);
            return;
        }

        if(Harakiri.get().getFriendManager().isFriend(e) != null && chamsModule.friend.getValue() ||
                Harakiri.get().getFriendManager().isFriend(e) == null && chamsModule.enemy.getValue() ||
                Minecraft.getMinecraft().player.getName().equalsIgnoreCase(e.getName()) && chamsModule.self.getValue()){

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

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
            GL11.glColor4f(1,1,1,1);
        }
    }

    @Inject(method = "render", at = @At("RETURN"), cancellable = false)
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci){

    }
}
