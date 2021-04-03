package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import me.vaxry.harakiri.impl.gui.hud.component.SpeedComponent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

public class DiscordRPCModule extends Module {

    public final Value<DiscordRPCModule.details> detailsV = new Value<details>("Details", new String[]{"Details", "D"}, "Details scheme.", DiscordRPCModule.details.SERVERIP);
    public final Value<DiscordRPCModule.status> statusV = new Value<status>("Status", new String[]{"Status", "S"}, "Status scheme.", DiscordRPCModule.status.FLEXING);

    private enum details {
        SERVERIP, USERNAME, SOMEWHERE, VERSION
    }
    private enum status {
        FLEXING, BEINGEPIC, HP, SPEED
    }

    public DiscordRPCModule(){
        super("Discord RPC", new String[]{"DiscordRPC", "RPC", "Discord"}, "Enables discord rich presence when in-game.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Harakiri.get().getDiscordManager().enable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            Harakiri.get().getDiscordManager().disable();
        }catch(Throwable t){
            Harakiri.get().logChat("Couldn't disable discord RPC because of: " + t.toString());
        }
    }

    public String getRPCDetails(){
        if(Minecraft.getMinecraft().world == null){
            String ret = "";
            ret += "In the main menu";
            return ret;
        }
        String ret = "";
        switch (detailsV.getValue()){
            case SERVERIP:
                ret += "Playing on ";
                ret += Minecraft.getMinecraft().isSingleplayer() ? "Singleplayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP;
                return ret;
            case VERSION:
                ret += "Rocking Harakiri v" + harakiriMod.VERSION;
                return ret;
            case USERNAME:
                ret += "Playing as ";
                ret += Minecraft.getMinecraft().player.getName();
                return ret;
            case SOMEWHERE:
                ret += "Being somewhere";
                return ret;
        }
        return "";
    }

    public String getRPCStatus(){
        if(Minecraft.getMinecraft().world == null){
            String ret = "";
            ret += "Flexing their client";
            return ret;
        }
        String ret = "";
        switch (statusV.getValue()){
            case HP:
                ret += "At " + (int)(Minecraft.getMinecraft().player.getHealth() + Minecraft.getMinecraft().player.getAbsorptionAmount()) + " health";
                return ret;
            case SPEED:
                ret += "Going at ";
                final DecimalFormat df = new DecimalFormat("#.#");
                SpeedComponent sc = (SpeedComponent)Harakiri.get().getHudManager().findComponent(SpeedComponent.class);
                ret += df.format(sc.speed);
                ret += "km/h";
                return ret;
            case FLEXING:
                ret += "Flexing their client";
                return ret;
            case BEINGEPIC:
                ret += "And being epic";
                return ret;
        }
        return "";
    }
}
