package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.math.Vec2f;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PacketLogModule extends Module {

    public final Value<Boolean> in = new Value<Boolean>("In", new String[]{"in"}, "Log packets you recieve.", true);
    public final Value<Boolean> out = new Value<Boolean>("Out", new String[]{"out"}, "Log packets you send.", false);

    public PacketLogModule() {
        super("PacketLog", new String[]{"PacketLog", "PacketL"}, "Logs packets to a file in .minecraft/harakiri", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE && this.in.getValue()) {
            final String time = new SimpleDateFormat("h:mm:ss").format(new Date());

            String finalMsg = "[" + time + "] " + "Packet of type (" + event.getPacket().getClass().getSimpleName() + ") is IN: \n";

            for (Field field : event.getPacket().getClass().getDeclaredFields()) {
                if (field != null) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    try {
                        finalMsg += field.getType().getSimpleName() + " " + field.getName() + " -> " + field.get(event.getPacket()) + "\n";
                    }catch (Throwable t) { ; }
                }
            }

            finalMsg += "\n- - - - -\n\n";

            String lastFileRead = getFileStringCreateIfNone();

            finalMsg = lastFileRead + finalMsg;

            //finalMsg = finalMsg.replace("\0", "");

            saveStringToFile(finalMsg);

            Harakiri.get().logChat("Logged an incoming packet of type: " + event.getPacket().getClass().getSimpleName());
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE && this.out.getValue()) {
            final String time = new SimpleDateFormat("h:mm:ss").format(new Date());

            String finalMsg = "[" + time + "] " + "Packet of type (" + event.getPacket().getClass().getSimpleName() + ") is OUT: \n";

            for (Field field : event.getPacket().getClass().getDeclaredFields()) {
                if (field != null) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    try {
                        finalMsg += field.getType().getSimpleName() + " " + field.getName() + " -> " + field.get(event.getPacket()) + "\n";
                    }catch (Throwable t) { ; }
                }
            }

            finalMsg += "\n- - - - -\n\n";

            String lastFileRead = getFileStringCreateIfNone();

            finalMsg = lastFileRead + finalMsg;

            //finalMsg = finalMsg.replace("\0", "");

            saveStringToFile(finalMsg);

            Harakiri.get().logChat("Logged an outgoing packet of type: " + event.getPacket().getClass().getSimpleName());
        }
    }

    public String getFileStringCreateIfNone() {
        return loadRawFile();
    }

    protected void saveStringToFile(String str){
        try {
            FileWriter writer = new FileWriter(new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "harakiri/packetlog.txt" : "harakiri\\packetlog.txt")));
            writer.write(str);
            writer.close();
        }catch (Throwable t){
            //Harakiri.get().errorChat("Couldn't save the config.");
        }
    }

    private String readAllBytesJava7(String filePath)
    {
        String content = "";

        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }

    protected String loadRawFile(){
        try {
            String res;
            res = readAllBytesJava7(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "harakiri/packetlog.txt" : "harakiri\\packetlog.txt"));
            return res;
        }catch (Throwable t){
            ;
        }
        return "";
    }
}
