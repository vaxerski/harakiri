package me.vaxry.harakiri.impl.manager;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;


import java.util.ArrayList;
import java.util.List;

public final class ChatManager {

    private Timer timer = new Timer();

    private List<String> chatBuffer = new ArrayList<>();

    private World world;

    public ChatManager() {
        Harakiri.get().getEventManager().registerAttender(this);
        Harakiri.get().getEventManager().build();
    }

    public void add(String s) {
        this.chatBuffer.add(s);
    }

    public void unload() {
        this.chatBuffer.clear();
        Harakiri.get().getEventManager().unregisterAttender(this);
    }

    Attender<EventPlayerUpdate> onUpdatePlayer = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            if (this.world != Minecraft.getMinecraft().world) {
                this.world = Minecraft.getMinecraft().world;
                this.chatBuffer.clear();
            }

            for (int i = 0; i < this.chatBuffer.size(); i++) {
                final String s = this.chatBuffer.get(i);
                if (s != null) {
                    if (this.timer.passed(200.0f)) {
                        Minecraft.getMinecraft().player.sendChatMessage(s);
                        this.chatBuffer.remove(s);
                        this.timer.reset();
                        i--;
                    }
                }
            }
        }
    });

}
