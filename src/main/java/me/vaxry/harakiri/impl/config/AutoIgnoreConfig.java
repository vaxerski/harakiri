package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.config.Configurable;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.impl.module.misc.AutoIgnoreModule;

import java.io.File;

/**
 * @author noil
 */
public final class AutoIgnoreConfig extends Configurable {

    private AutoIgnoreModule autoIgnoreModule;

    public AutoIgnoreConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "AutoIgnored"));
        this.autoIgnoreModule = (AutoIgnoreModule) Harakiri.INSTANCE.getModuleManager().find(AutoIgnoreModule.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        final JsonArray autoIgnoredJsonArray = this.getJsonObject().get("AutoIgnored").getAsJsonArray();

        for (JsonElement jsonElement : autoIgnoredJsonArray) {
            final String blacklistedName = jsonElement.getAsString();
            this.autoIgnoreModule.getBlacklist().add(blacklistedName);
        }
    }

    @Override
    public void onSave() {
        JsonObject save = new JsonObject();
        JsonArray autoIgnoreListJsonArray = new JsonArray();
        this.autoIgnoreModule.getBlacklist().forEach(autoIgnoreListJsonArray::add);
        save.add("AutoIgnored", autoIgnoreListJsonArray);
        this.saveJsonObjectToFile(save);
    }
}