package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.impl.manager.WorldManager;

import java.io.File;

public final class WorldConfig extends Configurable {

    public WorldConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "world"), "world.json");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.getJsonObject().entrySet().forEach(entry -> {
            final String host = entry.getKey();
            final String seed = entry.getValue().getAsJsonArray().get(0).getAsString();
            Harakiri.get().getWorldManager().getWorldDataList().add(new WorldManager.WorldData(host, Long.parseLong(seed)));
        });
    }

    @Override
    public void onSave() {
        JsonObject worldListJsonObject = new JsonObject();
        Harakiri.get().getWorldManager().getWorldDataList().forEach(worldData -> {
            final JsonArray array = new JsonArray();
            array.add(worldData.getSeed());
            worldListJsonObject.add(worldData.getHost(), array);
        });
        this.saveJsonObjectToFile(worldListJsonObject);
    }
}