package me.vaxry.harakiri.framework;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.impl.manager.ConfigManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public abstract class Configurable {

    private File file;
    private String configType;

    private JsonObject jsonObject;

    public Configurable(File file, String configType) {
        this.file = file;
        this.configType = configType;
    }

    public File getFile() {
        return file;
    }

    public void onLoad() {
        this.jsonObject = this.convertJsonObjectFromFile();
    }

    public void onSave() {

    }

    protected void saveJsonObjectToFile(JsonObject object) {
        FileUtil.saveJsonFile(FileUtil.recreateFile(this.getFile()), object);
    }

    protected void saveStringToFile(String str){
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(str);
            writer.close();
        }catch (Throwable t){
            //Harakiri.get().errorChat("Couldn't save the config.");
        }
    }

    protected String loadRawFile(){
        try {
            FileReader reader = FileUtil.createReader(this.getFile());
            char[] buffer = new char[128000]; // 128k
            reader.read(buffer);
            FileUtil.closeReader(reader);
            return String.valueOf(buffer);
        }catch (Throwable t){
            //Harakiri.get().errorChat("Couldn't load the config.");
        }
        return "";
    }

    protected JsonObject convertJsonObjectFromFile() {
        if (!this.getFile().exists())
            return new JsonObject();

        FileReader reader = FileUtil.createReader(this.getFile());
        if (reader == null)
            return new JsonObject();

        JsonElement element = new JsonParser().parse(reader);
        if (!element.isJsonObject()) {
            FileUtil.closeReader(reader);
            return new JsonObject();
        }

        FileUtil.closeReader(reader);

        return element.getAsJsonObject();
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public void forceGlobalDir(){
        File newFile = new File(ConfigManager.CONFIG_PATH + this.configType);
        this.file = newFile;
    }

    public void setNewConfigFileDir(String dir){
        File newFile = new File(ConfigManager.CONFIG_PATH + dir + "/" + this.configType);
        this.file = newFile;
    }
}