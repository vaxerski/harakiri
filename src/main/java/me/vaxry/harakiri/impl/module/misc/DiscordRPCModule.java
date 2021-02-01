package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.impl.management.DiscordManager;
import net.minecraft.client.Minecraft;

public class DiscordRPCModule extends Module {

    public DiscordRPCModule(){
        super("Discord RPC", new String[]{"DiscordRPC", "RPC", "Discord"}, "Enables discord rich presence when in-game.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Harakiri.INSTANCE.getDiscordManager().enable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            Harakiri.INSTANCE.getDiscordManager().disable();
        }catch(Throwable t){
            Harakiri.INSTANCE.logChat("Couldn't disable discord RPC because of: " + t.toString());
        }
    }

    public String getRPCDetails(){
        if(Minecraft.getMinecraft().world == null){
            String ret = "";
            ret += "In the main menu";
            return ret;
        }
        String ret = "";
        ret += "Playing on ";
        ret += Minecraft.getMinecraft().isSingleplayer() ? "Singleplayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP;
        return ret;
    }

    public String getRPCStatus(){
        if(Minecraft.getMinecraft().world == null){
            String ret = "";
            ret += "Flexing their client";
            return ret;
        }
        String ret = "";
        ret += "And being epic";
        return ret;
    }
}
