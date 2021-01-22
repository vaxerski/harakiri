package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.network.EventSendPacket;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.network.Packet;
import net.minecraft.util.StringUtils;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * created by noil on 8/3/2019 at 6:32 PM
 */
public final class PacketLoggerModule extends Module {

    private Packet[] packets;

    public final Value<Boolean> incoming = new Value<Boolean>("Incoming", new String[]{"in"}, "Log incoming packets when enabled.", true);
    public final Value<Boolean> outgoing = new Value<Boolean>("Outgoing", new String[]{"out"}, "Log outgoing packets when enabled.", true);

    public final Value<Boolean> chat = new Value<Boolean>("Chat", new String[]{"ch"}, "Logs packet traffic to chat.", true);
    public final Value<Boolean> console = new Value<Boolean>("Console", new String[]{"con"}, "Logs packet traffic to console.", true);

    public final Value<Boolean> data = new Value<Boolean>("Data", new String[]{"dat"}, "Include data about the packet's class in the log when enabled.", true);

    public PacketLoggerModule() {
        super("PacketLogger", new String[]{"pktlgr"}, "Log incoming and/or outgoing packets to console.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onToggle() {
        super.onToggle();
        this.packets = null;
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (this.incoming.getValue()) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (this.console.getValue()) {
                    Harakiri.INSTANCE.getLogger().log(Level.INFO, "\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Harakiri.INSTANCE.getLogger().log(Level.INFO, StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Harakiri.INSTANCE.getLogger().log(Level.INFO, "}");
                }

                if (this.chat.getValue()) {
                    Harakiri.INSTANCE.logChat("\2477IN: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Harakiri.INSTANCE.logChat(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Harakiri.INSTANCE.logChat("}");
                }
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (this.outgoing.getValue()) {
            if (event.getStage() == EventStageable.EventStage.PRE) {
                if (this.console.getValue()) {
                    Harakiri.INSTANCE.getLogger().log(Level.INFO, "\2477OUT: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Harakiri.INSTANCE.getLogger().log(Level.INFO, StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Harakiri.INSTANCE.getLogger().log(Level.INFO, "}");
                }

                if (this.chat.getValue()) {
                    Harakiri.INSTANCE.logChat("\2477OUT: \247r" + event.getPacket().getClass().getSimpleName() + " {");

                    if (this.data.getValue()) {
                        try {

                            Class clazz = event.getPacket().getClass();

                            while (clazz != Object.class) {

                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field != null) {
                                        if (!field.isAccessible()) {
                                            field.setAccessible(true);
                                        }
                                        Harakiri.INSTANCE.logChat(StringUtils.stripControlCodes("      " + field.getType().getSimpleName() + " " + field.getName() + " = " + field.get(event.getPacket())));
                                    }
                                }

                                clazz = clazz.getSuperclass();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Harakiri.INSTANCE.logChat("}");
                }
            }
        }
    }
}
