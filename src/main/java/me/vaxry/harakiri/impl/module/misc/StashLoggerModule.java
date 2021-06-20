package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.util.math.Vec2f;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class StashLoggerModule extends Module {

    public final Value<Integer> min = new Value<Integer>("MinimumTE", new String[]{"MinTE", "min"}, "Minimal amount of TEs to log.", 1, 1, 25, 1);

    public StashLoggerModule() {
        super("StashLogger", new String[]{"StashLogger", "StashLog"}, "Logs possible stashes to a file in .minecraft/harakiri.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChunkData) {
                final SPacketChunkData packet = (SPacketChunkData) event.getPacket();
                final Vec2f position = new Vec2f(packet.getChunkX() * 16, packet.getChunkZ() * 16);
                final ArrayList<String> foundStorage = new ArrayList<>();

                for (NBTTagCompound tag : packet.getTileEntityTags()) {
                    final String id = tag.getString("id");
                    if (
                            ((id.equals("minecraft:chest") || id.equals("minecraft:trapped_chest"))) ||
                                    (id.equals("minecraft:ender_chest")) ||
                                    (id.equals("minecraft:shulker_box")) ||
                                    (id.equals("minecraft:hopper")) ||
                                    (id.equals("minecraft:dropper")) ||
                                    (id.equals("minecraft:dispenser")) ||
                                    (id.equals("minecraft:brewing_stand"))
                    ) {
                        foundStorage.add(id);
                    }
                }

                if (foundStorage.size() >= this.min.getValue()) {
                    String finalMsg = "LOCATION: [" + position.x + ", " + position.y + "] -> FOUND: [";

                    for (String type : foundStorage) {
                        finalMsg += type + ", ";
                    }

                    finalMsg += "]\n";

                    String lastFileRead = getFileStringCreateIfNone();

                    finalMsg = lastFileRead + finalMsg;

                    //finalMsg = finalMsg.replace("\0", "");

                    saveStringToFile(finalMsg);
                }
            }
        }
    }

    public String getFileStringCreateIfNone() {
        return loadRawFile();
    }

    protected void saveStringToFile(String str){
        try {
            FileWriter writer = new FileWriter(new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "harakiri/stashlogger.txt" : "harakiri\\stashlogger.txt")));
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
            res = readAllBytesJava7(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "harakiri/stashlogger.txt" : "harakiri\\stashlogger.txt"));
            return res;
        }catch (Throwable t){
            ;
        }
        return "";
    }
}
