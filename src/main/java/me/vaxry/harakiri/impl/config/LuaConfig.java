package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.lua.ReloadLuasModule;

import java.awt.*;
import java.io.File;

public final class LuaConfig extends Configurable {

    private final ReloadLuasModule reloadLuasModule;

    public LuaConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "lua"), "lua.json");
        this.reloadLuasModule = (ReloadLuasModule) Harakiri.get().getModuleManager().find(ReloadLuasModule.class);
        reloadLuasModule.loadLuas();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.reloadLuasModule == null)
            return;

        JsonArray luaJsonArr = null;

        final JsonElement luaSettings = this.getJsonObject().get("lua");
        if (luaSettings != null)
            luaJsonArr = luaSettings.getAsJsonArray();

        if (luaJsonArr != null) {
            for (JsonElement jsonElement : luaJsonArr) {
                try {
                    String luaname = jsonElement.getAsString().substring(0, jsonElement.getAsString().indexOf(':'));
                    String valuename = jsonElement.getAsString().substring(jsonElement.getAsString().indexOf(':') + 1, jsonElement.getAsString().indexOf('>'));
                    String valueSett = jsonElement.getAsString().substring(jsonElement.getAsString().indexOf('>') + 1);

                    LUAAPI.LuaModule luaModule = reloadLuasModule.getLuaModuleByName(luaname);
                    Module thislua = Harakiri.get().getModuleManager().findLua(luaModule.getLuaName());

                    if (valuename.equalsIgnoreCase("enabled")) {
                        if (Boolean.valueOf(valueSett) != thislua.isEnabled()) {
                            thislua.toggle();
                        }
                    } else if (valuename.equalsIgnoreCase("hidden")) {
                        if (Boolean.valueOf(valueSett))
                            thislua.setHidden(true);
                    } else {
                        Value value = thislua.findValue(valuename);
                        if (value.getValue().getClass() == Float.class) {
                            value.setValueForceFloat((double) (Double.valueOf(valueSett)));
                        } else if (value.getValue().getClass() == Double.class) {
                            value.setValue((double) (Double.valueOf(valueSett)));
                        } else if (value.getValue().getClass() == Boolean.class) {
                            value.setValue(Boolean.valueOf(valueSett));
                        } else if (value.getValue().getClass() == Color.class) {
                            value.setValue(Integer.valueOf(valueSett));
                        } else if (value.getValue().getClass() == Integer.class) {
                            value.setValueForceInt((double) (Double.valueOf(valueSett)));
                        } else {
                            // it's an enum, prolly
                            try {
                                value.setEnumValue(String.valueOf(valueSett));
                            } catch (Throwable t) {
                                //ignoer
                            }
                        }
                    }
                }catch(Throwable t) {
                    // Ignore, probably means a missing lua.
                }
            }
        }
    }

    @Override
    public void onSave() {
        if (this.reloadLuasModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray luaSettingsArray = new JsonArray();
        for (LUAAPI.LuaModule lm : this.reloadLuasModule.loadedLuas) {
            Module thislua = Harakiri.get().getModuleManager().findLua(lm.getLuaName());

            luaSettingsArray.add(lm.getLuaName() + ":" + "enabled" + ">" + thislua.isEnabled());
            luaSettingsArray.add(lm.getLuaName() + ":" + "hidden" + ">" + thislua.isHidden());

            for(Value v : thislua.getValueList()){
                luaSettingsArray.add(lm.getLuaName() + ":" + v.getName() + ">" + v.getValue());
            }
        }

        save.add("lua", luaSettingsArray);

        this.saveJsonObjectToFile(save);
    }
}
