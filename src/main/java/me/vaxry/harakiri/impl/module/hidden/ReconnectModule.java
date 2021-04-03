package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventRunTick;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.gui.menu.AutoReconnectButton;
import me.vaxry.harakiri.impl.gui.menu.ReconnectButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;


public final class ReconnectModule extends Module {

    private String lastIp;
    private int lastPort;
    private boolean reconnect;
    private Timer timer = new Timer();

    public final Value<Boolean> auto = new Value<Boolean>("AutoReconnect", new String[]{"Auto"}, "Auto Reconnect.", true);
    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "Delay (in ms) between reconnect attempts.", 3000.0f, 0.0f, 10000.0f, 500.0f);

    public ReconnectModule() {
        super("Reconnect", new String[]{"Rejoin", "Recon", "AutoReconnect"}, "Automatically reconnects to the last server after being disconnected.", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        if(!this.isEnabled()) {
            this.setEnabled(true);
            this.onEnable();
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof C00Handshake) {
                final C00Handshake packet = (C00Handshake) event.getPacket();
                if (packet.getRequestedState() == EnumConnectionState.LOGIN) {
                    this.lastIp = packet.ip;
                    this.lastPort = packet.port;
                }
            }
        }
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

    public boolean reconnect(){
        if (this.lastIp != null && this.lastPort > 0) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(Minecraft.getMinecraft().currentScreen, Minecraft.getMinecraft(), this.lastIp, this.lastPort));
            this.timer.reset();
            this.reconnect = false;
        }else
            return false;
        return true;
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
            event.getButtonList().add(new ReconnectButton(lastId + 1, x, maxY + 23, "Re-Enter thy Stage"));
            event.getButtonList().add(new AutoReconnectButton(lastId + 1, x, maxY + 46, "Mechanical Re-Entering Disabled."));
        }
    }

}
