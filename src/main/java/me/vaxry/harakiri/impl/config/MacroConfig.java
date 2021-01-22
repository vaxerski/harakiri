package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.config.Configurable;
import me.vaxry.harakiri.api.macro.Macro;
import me.vaxry.harakiri.api.util.FileUtil;

import java.io.File;

/**
 * @author noil
 */
public final class MacroConfig extends Configurable {

    public MacroConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "Macros"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.getJsonObject().entrySet().forEach(entry -> {
            final String name = entry.getKey();
            final String key = entry.getValue().getAsJsonArray().get(0).getAsString();
            final String macro = entry.getValue().getAsJsonArray().get(1).getAsString();
            Harakiri.INSTANCE.getMacroManager().getMacroList().add(new Macro(name, key, macro));
        });
    }

    @Override
    public void onSave() {
        JsonObject macroListObject = new JsonObject();
        Harakiri.INSTANCE.getMacroManager().getMacroList().forEach(macro -> {
            JsonArray array = new JsonArray();
            array.add(macro.getKey());
            array.add(macro.getMacro());
            macroListObject.add(macro.getName(), array);
        });
        this.saveJsonObjectToFile(macroListObject.getAsJsonObject());
    }
}
