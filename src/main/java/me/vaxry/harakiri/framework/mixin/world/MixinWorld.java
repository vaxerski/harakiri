package me.vaxry.harakiri.framework.mixin.world;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.*;
import me.vaxry.harakiri.impl.module.render.NoLagModule;
import me.vaxry.harakiri.impl.module.world.SkyModule;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void onCheckLightFor(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        final EventLightUpdate event = new EventLightUpdate();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (!Minecraft.getMinecraft().isSingleplayer() && event.isCanceled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "getRainStrength", at = @At("HEAD"), cancellable = true)
    private void onGetRainStrength(float delta, CallbackInfoReturnable<Float> cir) {
        final EventRainStrength event = new EventRainStrength();
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) {
            cir.setReturnValue(0f);
            cir.cancel();
        }
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    private void onEntityAdded(Entity entityIn, CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventAddEntity(entityIn));
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    private void onEntityRemoved(Entity entityIn, CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventRemoveEntity(entityIn));
    }

    @Inject(method = "spawnEntity", at = @At("HEAD"))
    private void spawnEntity(Entity entityIn, CallbackInfoReturnable<Boolean> ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventSpawnEntity(entityIn));
        if (entityIn instanceof EntityFireworkRocket && ((NoLagModule)Harakiri.get().getModuleManager().find(NoLagModule.class)).firework.getValue()) {
            entityIn.setDead();
        }

        NoLagModule noLagModule = (NoLagModule)Harakiri.get().getModuleManager().find(NoLagModule.class);

        if(noLagModule.hardRemove.getValue()) {
            if(entityIn instanceof EntityItem && noLagModule.items.getValue()){
                entityIn.setDead();
            }

            if(entityIn instanceof EntityWitherSkull && noLagModule.witherSkulls.getValue()){
                entityIn.setDead();
            }

            if(entityIn instanceof EntityArmorStand && noLagModule.removeChunkBan.getValue()){
                entityIn.setDead();
            }
        }
    }

    @Inject(method = "getCelestialAngle", at = @At("HEAD"))
    public void getCelestialAngle(float pt, CallbackInfoReturnable<Float> cir){
        if(Minecraft.getMinecraft().player == null)
            return;

        if(!(((SkyModule) Harakiri.get().getModuleManager().find(SkyModule.class)).timerWorld).passed(1000))
            return;

        if(Harakiri.get().getModuleManager().find(SkyModule.class).isEnabled()){
            ((SkyModule)Harakiri.get().getModuleManager().find(SkyModule.class)).worldtime = Minecraft.getMinecraft().world.getWorldTime();
            Minecraft.getMinecraft().world.setWorldTime(((SkyModule)Harakiri.get().getModuleManager().find(SkyModule.class)).celestialAng.getValue());
        }
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    public void setBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> cir)
    {
        EventSetBlockState event = new EventSetBlockState(pos, newState, flags);

        Harakiri.get().getEventManager().dispatchEvent(event);

        if (event.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }
}
