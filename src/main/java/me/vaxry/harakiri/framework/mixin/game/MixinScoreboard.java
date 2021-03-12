package me.vaxry.harakiri.framework.mixin.game;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.render.ESPModule;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = Scoreboard.class, priority = 1001)
public class MixinScoreboard extends Scoreboard{
    @Shadow
    Map<String, ScorePlayerTeam> teamMemberships;

   /* @Inject(method = "addPlayerToTeam", at = @At("HEAD"))
    public void addPlayerToTeam(String player, String newTeam, CallbackInfoReturnable<Boolean> cir){
        ESPModule espModule = (ESPModule) Harakiri.get().getModuleManager().find(ESPModule.class);
        if(!newTeam.equalsIgnoreCase(espModule.red.toString()) &&
            !newTeam.equalsIgnoreCase(espModule.green.toString()) &&
            !newTeam.equalsIgnoreCase(espModule.lblue.toString()) &&
            !newTeam.equalsIgnoreCase(espModule.purple.toString())){
            // Incorrect team.
            cir.setReturnValue(true);
        }else{

            ScorePlayerTeam scoreplayerteam = espModule.getTeamFromStr(newTeam);
            if(scoreplayerteam == null)
                cir.setReturnValue(false);
            else{
                if (this.teamMemberships.get(player) != null)
                {
                    this.teamMemberships.remove(player);
                    this.teamMemberships.get(player).getMembershipCollection().remove(player);
                }

                this.teamMemberships.put(player, scoreplayerteam);
                scoreplayerteam.getMembershipCollection().add(player);
                cir.setReturnValue(true);

                newTeam = "HARAKIRI CHANGED";
            }
        }
    }*/
}
