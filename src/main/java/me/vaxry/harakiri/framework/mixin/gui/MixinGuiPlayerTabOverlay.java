package me.vaxry.harakiri.framework.mixin.gui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.misc.ExtraTabModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.util.List;

@Mixin(value = GuiPlayerTabOverlay.class, priority = 2147483647)
public class MixinGuiPlayerTabOverlay {

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
        ExtraTabModule extraTabModule = (ExtraTabModule)Harakiri.get().getModuleManager().find(ExtraTabModule.class);
        if(extraTabModule.isEnabled())
            return list.subList(0, Math.min(list.size(), 275));
        else
            return list.subList(fromIndex, toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        try {
            String name = "";
            ExtraTabModule extraTabModule = (ExtraTabModule) Harakiri.get().getModuleManager().find(ExtraTabModule.class);

            name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getUnformattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());

            if(extraTabModule == null){
                name = "Error";
            }
            else if (extraTabModule.isEnabled()) {

                if (networkPlayerInfoIn.getGameProfile().getName().equalsIgnoreCase(Minecraft.getMinecraft().player.getGameProfile().getName())) {
                    // us
                    name = TextFormatting.GOLD.toString() + networkPlayerInfoIn.getGameProfile().getName();
                } else if (Harakiri.get().getFriendManager().find(networkPlayerInfoIn.getGameProfile().getName()) != null) {
                    //friend
                    name = TextFormatting.AQUA.toString() + networkPlayerInfoIn.getGameProfile().getName();
                }
            }

            cir.setReturnValue(name);
        }catch (Throwable t){
            cir.setReturnValue("Error");
        }
    }

}