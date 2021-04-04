package me.vaxry.harakiri.impl.module.combat;

import akka.japi.Pair;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.BlockInteractionUtil;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.render.HolesModule;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class AnchorModule extends Module {

    private HolesModule holesModule = null;

    public final Value<Boolean> spacedisable = new Value<Boolean>("JumpDisables", new String[]{"JumpDisables"}, "If on, doesnt stop you when you hold the jump key.", false);

    public AnchorModule() {
        super("Anchor", new String[]{"Anchor", "Anch"}, "Automatically stops you over a hole.", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void onFullLoad() {
        super.onFullLoad();
        holesModule = ((HolesModule)Harakiri.get().getModuleManager().find(HolesModule.class));
    }

    @Listener
    public void move(EventMove event) {

        // Check if over a hole.

        if(!holesModule.isEnabled())
            holesModule.forceHoleRecalc();

        BlockPos localPos = BlockInteractionUtil.GetLocalPlayerPosFloored();
        HolesModule.Hole foundHole = null;

        for(Pair<HolesModule.Hole, Integer> pair : holesModule.holes){
            final HolesModule.Hole hole = pair.first();

            if(hole.getX() == localPos.getX() && hole.getZ() == localPos.getZ() && hole.getY() + 0.1F < Minecraft.getMinecraft().player.posY){
                foundHole = hole;
                break;
            }
        }

        if(foundHole == null)
            return;

        if(!canLocalFitIntoHole(foundHole))
            return;

        if(this.spacedisable.getValue() && Minecraft.getMinecraft().gameSettings.keyBindJump.pressed)
            return;

        event.setX(0);
        event.setZ(0);
    }

    private boolean canLocalFitIntoHole(HolesModule.Hole hole){
        EntityPlayer local = Minecraft.getMinecraft().player;

        if(local.posX >= hole.getX() + 0.3F &&
            local.posX <= hole.getX() + 0.7F &&
                local.posZ >= hole.getZ() + 0.3F &&
                local.posZ <= hole.getZ() + 0.7F)
            return true;

        return false;
    }
}
