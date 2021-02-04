package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.api.event.minecraft.EventRunTick;
import me.vaxry.harakiri.api.event.network.EventSendPacket;
import me.vaxry.harakiri.api.event.render.EventRender3D;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.Timer;
import me.vaxry.harakiri.api.value.Value;
import me.vaxry.harakiri.impl.gui.menu.ReconnectButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public final class ReconnectModule extends Module {

    private String lastIp;
    private int lastPort;
    private boolean reconnect;
    private Timer timer = new Timer();

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "Delay (in ms) between reconnect attempts.", 3000.0f, 0.0f, 10000.0f, 500.0f);

    public ReconnectModule() {
        super("Reconnect", new String[]{"Rejoin", "Recon", "AutoReconnect"}, "Automatically reconnects to the last server after being disconnected.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onRender(EventRender3D event) {
        if(Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().isSingleplayer())
            return;

        this.lastIp = Minecraft.getMinecraft().getCurrentServerData().serverIP;
    }

    @Listener
    public void runTick(EventRunTick event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (this.lastIp != null && this.lastPort > 0 && this.reconnect) {
                if (this.timer.passed(this.delay.getValue())) {
                    reconnect();
                }
            }
        }
    }

    public void reconnect(){
        //todo: fix this.
        Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), this.lastIp, this.lastPort));
        this.timer.reset();
        this.reconnect = false;
    }

    @SubscribeEvent
    public void onInitGuiEventPost(GuiScreenEvent.InitGuiEvent.Post event) {
        final GuiScreen gui = event.getGui();
        if (gui instanceof GuiDisconnected) {
            int maxY = 0;
            int x = 0;
            int lastId = 0;
            for (final GuiButton button : event.getButtonList()) {
                x = button.x;
                maxY = Math.max(button.y, maxY);
                lastId = button.id;
            }
            event.getButtonList().add(new ReconnectButton(lastId + 1, x, maxY + 20, "Re-Enter thy Stage"));
        }
    }

}
