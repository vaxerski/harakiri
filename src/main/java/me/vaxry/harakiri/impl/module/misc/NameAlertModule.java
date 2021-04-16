package me.vaxry.harakiri.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NameAlertModule extends Module {

    //public final Value<Boolean> saveToFile = new Value<Boolean>("SaveToFile", new String[]{"Save", "Saves"}, "Saves the alert to a file in your harakiri 'Config' directory.", false);

    //private final String REGEX_NAME = "(?<=<).*?(?=>)";
    private final String REGEX_NAME = "<(\\S+)\\s*(\\S+?)?>\\s(.*)";

    public NameAlertModule() {
        super("MentionAlert", new String[]{"MentionAlert", "SayMyName", "WhoSaid"}, "Alerts you when someone says your name in chat.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onChat(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packetChat = (SPacketChat) event.getPacket();
                String text = packetChat.getChatComponent().getFormattedText();
                final String localUsername = Minecraft.getMinecraft().getSession().getUsername();

                if ((text.contains(":") && text.toLowerCase().contains(ChatFormatting.LIGHT_PURPLE + "from")) ||
                        (text.toLowerCase().contains(ChatFormatting.GRAY + "") && StringUtils.stripControlCodes(text).contains("whispers to you"))) {
                    Harakiri.get().getNotificationManager().addNotification("Whisper", "Someone whispered to you.");
                    return;
                }

                if (text.toLowerCase().contains(localUsername.toLowerCase())) {
                    text = StringUtils.stripControlCodes(text);
                    // code below is for public chat
                    Pattern chatUsernamePattern = Pattern.compile(REGEX_NAME);
                    Matcher chatUsernameMatcher = chatUsernamePattern.matcher(text);

                    if (chatUsernameMatcher.find()) {
                        String username = chatUsernameMatcher.group(1).replaceAll(">", "");
                        if (!username.equals(localUsername)) {
                            Harakiri.get().getNotificationManager().addNotification("Chat", String.format("<%s> mentioned you in chat.", username));
                        }
                    }
                }
            }
        }
    }
}
