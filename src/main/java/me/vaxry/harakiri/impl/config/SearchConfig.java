package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.impl.module.render.SearchModule;

import java.io.File;
import java.util.Objects;

public final class SearchConfig extends Configurable {

    private final SearchModule searchModule;

    public SearchConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "search"), "search.json");
        this.searchModule = (SearchModule) Harakiri.get().getModuleManager().find("Search");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.searchModule == null)
            return;

        JsonArray searchIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("SearchID");
        if (blockIds != null)
            searchIdsJsonArray = blockIds.getAsJsonArray();

        if (searchIdsJsonArray != null) {
            for (JsonElement jsonElement : searchIdsJsonArray) {
                ((SearchModule) Objects.requireNonNull(Harakiri.get().getModuleManager().find("Search"))).add(jsonElement.getAsInt());
            }
        }
    }

    @Override
    public void onSave() {
        if (this.searchModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray searchIdsJsonArray = new JsonArray();
        for (Integer i : this.searchModule.getIds())
            searchIdsJsonArray.add(i);

        save.add("SearchID", searchIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}
