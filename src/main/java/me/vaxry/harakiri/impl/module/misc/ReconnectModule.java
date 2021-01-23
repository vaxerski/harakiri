package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.api.event.minecraft.EventRunTick;
import me.vaxry.harakiri.api.event.network.EventSendPacket;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.Timer;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/22/2019 @ 6:17 AM.
 */
public final class ReconnectModule extends Module {

    private String lastIp;
    private int lastPort;
    private boolean reconnect;
    private Timer timer = new Timer();

    private GuiButton reconnectButton;

    public final Value<Float> delay = new Value<Float>("Delay", new String[]{"Del"}, "Delay in MS (milliseconds) between reconnect attempts.", 3000.0f, 0.0f, 10000.0f, 500.0f);

    public ReconnectModule() {
        super("Reconnect", new String[]{"Rejoin", "Recon", "AutoReconnect"}, "Automatically reconnects to the last server after being kicked", "NONE", -1, ModuleType.MISC);
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        int w = 300;
        int h = 40;
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
                    Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, Minecraft.getMinecraft(), this.lastIp, this.lastPort));
                    this.timer.reset();
                    this.reconnect = false;
                }
            }
        }
    }

    //todo: make this work xd

    @SubscribeEvent
    public static void onInitGuiEvent(final GuiScreenEvent.InitGuiEvent event) {
        final GuiScreen gui = event.getGui();
        if (gui instanceof GuiIngameMenu) {
            int maxY = 0;
            for (final GuiButton button : event.getButtonList()) {
                maxY = Math.max(button.y, maxY);
            }
            event.getButtonList().add(new GuiButton(42042069, event.getButtonList().get(0).x, maxY + 24, "Tets"));
        }
    }

    @Listener
    public void displayGui(EventDisplayGui event) {
        if (event.getScreen() != null) {
            if (event.getScreen() instanceof GuiDisconnected) {
                this.reconnect = true;
            }
        }
    }

}
