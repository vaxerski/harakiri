package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventSendChatMessage;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.impl.module.hidden.CommandsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class ChatSuffixModule extends Module {

    private final String prefix = "\u23D0 \u8179\u5207harakiri";

    public ChatSuffixModule() {
        super("ChatSuffix", new String[]{"Suffix", "Chat_Suffix", "CustomChat", "Custom_Chat"}, "Add a custom Harakiri suffix to your chat messages.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onSendChatMessage(EventSendChatMessage event) {
        final CommandsModule cmds = (CommandsModule) Harakiri.get().getModuleManager().find(CommandsModule.class);
        if (cmds == null)
            return;

        if (event.getMessage().startsWith("/") || event.getMessage().startsWith(cmds.prefix.getValue()))
            return;

        event.setCanceled(true);
        Minecraft.getMinecraft().getConnection().sendPacket(new CPacketChatMessage(event.getMessage() + " " + prefix));
    }
}
