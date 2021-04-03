package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.Friend;
import me.vaxry.harakiri.framework.util.FileUtil;

import java.io.File;

public final class FriendConfig extends Configurable {

    public FriendConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "friend"), "friend.json");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.getJsonObject().entrySet().forEach(entry -> {
            final String name = entry.getKey();

            String alias = "";
            String uuid = "";
            JsonArray jsonArray = entry.getValue().getAsJsonArray();

            if (jsonArray != null) {
                if (jsonArray.size() > 0) {
                    alias = jsonArray.get(0).getAsString();
                    if (jsonArray.get(1).isJsonNull()) {
                        uuid = "";
                    } else {
                        uuid = jsonArray.get(1).getAsString();
                    }
                }
            }

            Harakiri.get().getFriendManager().getFriendList().add(new Friend(name, uuid, alias));
        });
    }

    @Override
    public void onSave() {
        JsonObject friendsListJsonObject = new JsonObject();
        Harakiri.get().getFriendManager().getFriendList().forEach(friend -> {
            JsonArray array = new JsonArray();
            array.add(friend.getAlias());
            array.add(friend.getUuid());
            friendsListJsonObject.add(friend.getName(), array);
        });
        this.saveJsonObjectToFile(friendsListJsonObject);
    }
}
