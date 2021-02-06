package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author Seth
 * 6/27/2019 @ 1:09 AM.
 */
public final class AutoSignModule extends Module {

    private String[] lines;

    public final Value<Boolean> overflow = new Value("Overflow", new String[]{"Ov"}, "Fill the sign with the maximum number of randomly generated characters.", false);

    public AutoSignModule() {
        super("AutoSign", new String[]{"AutomaticSign", "ASign"}, "Automatically writes text on signs.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.lines = null;
    }

    @Listener
    public void displayGui(EventDisplayGui event) {
        if (event.getScreen() != null && event.getScreen() instanceof GuiEditSign) {
            final GuiEditSign gui = (GuiEditSign) event.getScreen();

            final boolean shouldCancel = this.overflow.getValue() ? true : this.lines != null;

            if (gui != null && shouldCancel && gui.tileSign != null) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketUpdateSign(gui.tileSign.getPos(), new TextComponentString[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")}));
                Minecraft.getMinecraft().displayGuiScreen(null);
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketUpdateSign) {
                final CPacketUpdateSign packet = (CPacketUpdateSign) event.getPacket();

                if (this.overflow.getValue()) {
                    final IntStream gen = new Random().ints(0x80, 0x10ffff - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
                    final String line = gen.limit(4 * 384).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
                    for (int i = 0; i < 4; i++) {
                        packet.lines[i] = line.substring(i * 384, (i + 1) * 384);
                    }
                } else {
                    if (this.lines == null && packet.getLines() != null) {
                        this.lines = packet.getLines();
                        Harakiri.INSTANCE.logChat("Sign text set");
                    } else {
                        packet.lines = this.lines;
                    }
                }
            }
        }
    }

}
