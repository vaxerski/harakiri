package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.config.Configurable;
import me.vaxry.harakiri.api.util.FileUtil;
import me.vaxry.harakiri.impl.management.WorldManager;

import java.io.File;

/**
 * @author noil
 */
public final class WorldConfig extends Configurable {

    public WorldConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Worlds"));
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.getJsonObject().entrySet().forEach(entry -> {
            final String host = entry.getKey();
            final String seed = entry.getValue().getAsJsonArray().get(0).getAsString();
            Harakiri.INSTANCE.getWorldManager().getWorldDataList().add(new WorldManager.WorldData(host, Long.parseLong(seed)));
        });
    }

    @Override
    public void onSave() {
        JsonObject worldListJsonObject = new JsonObject();
        Harakiri.INSTANCE.getWorldManager().getWorldDataList().forEach(worldData -> {
            final JsonArray array = new JsonArray();
            array.add(worldData.getSeed());
            worldListJsonObject.add(worldData.getHost(), array);
        });
        this.saveJsonObjectToFile(worldListJsonObject);
    }
}