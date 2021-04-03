package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.impl.module.render.XrayModule;

import java.io.File;
import java.util.Objects;

public final class XrayConfig extends Configurable {

    private final XrayModule xrayModule;

    public XrayConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "xray"), "xray.json");
        this.xrayModule = (XrayModule) Harakiri.get().getModuleManager().find("Xray");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.xrayModule == null)
            return;

        JsonArray xrayIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("XrayBlockIds");
        if (blockIds != null)
            xrayIdsJsonArray = blockIds.getAsJsonArray();

        if (xrayIdsJsonArray != null) {
            for (JsonElement jsonElement : xrayIdsJsonArray) {
                ((XrayModule) Objects.requireNonNull(Harakiri.get().getModuleManager().find("Xray"))).add(jsonElement.getAsInt());
            }
        }
    }

    @Override
    public void onSave() {
        if (this.xrayModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray xrayIdsJsonArray = new JsonArray();
        for (Integer i : this.xrayModule.getIds())
            xrayIdsJsonArray.add(i);

        save.add("XrayBlockIds", xrayIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}
