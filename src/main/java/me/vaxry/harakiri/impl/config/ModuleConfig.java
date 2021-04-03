package me.vaxry.harakiri.impl.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.framework.Value;

import java.awt.*;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;


public class ModuleConfig extends Configurable {

    public ModuleConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "mods"), "mods.json");
    }

    class ModuleConfigJSON{
        public ModuleConfigJSON(String name, boolean hidden, String key, boolean enabled){
            this.name = name;
            this.hidden = hidden;
            this.keybind = key;
            this.enabled = enabled;
        }
        public String name;
        public boolean hidden;
        public String keybind;
        public boolean enabled;
        public ArrayList<Value> values = new ArrayList<>();
    }

    class ColorShitKillMePls{
        public int value;
        public float falpha;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        String rawdata = loadRawFile();
        if(rawdata.equalsIgnoreCase(""))
            return;

        Gson gson = new Gson();

        //JsonObject obj = gson.fromJson(rawdata, JsonObject.class);
        JsonReader reader = new JsonReader(new StringReader(rawdata));
        reader.setLenient(true);

        // Retrieve array
        ModuleConfigJSON[] moduleConfigJSONS = gson.fromJson(reader, ModuleConfigJSON[].class);

        for(ModuleConfigJSON settings : moduleConfigJSONS) {

            if(settings == null)
                continue;

            Module mod = Harakiri.get().getModuleManager().find(settings.name);

            if(mod == null)
                continue;

            if(mod.getType() == Module.ModuleType.CONFIG)
                continue; // Avoid loops

            if(!mod.isEnabled() && settings.enabled || mod.isEnabled() && !settings.enabled)
                mod.toggle();
            mod.setHidden(settings.hidden);
            mod.setKey(settings.keybind);

            // Retrieve values :)
            for(Value value : mod.getValueList()){
                if(value.getValue().getClass() == Float.class){
                    try {
                        for (Value<Double> savedValue : settings.values) {
                            if (value.getName().equalsIgnoreCase(savedValue.getName())) {
                                value.setValueForceFloat((double) (savedValue.getValue()));
                            }
                        }
                    }catch (Throwable t){ }
                } else if(value.getValue().getClass() == Double.class){
                    try {
                        for (Value<Double> savedValue : settings.values) {
                            if (value.getName().equalsIgnoreCase(savedValue.getName())) {
                                value.setValue(savedValue.getValue());
                            }
                        }
                    }catch (Throwable t){ }
                } else if(value.getValue().getClass() == Boolean.class){
                    try {
                        for (Value<Boolean> savedValue : settings.values) {
                            if (value.getName().equalsIgnoreCase(savedValue.getName())) {
                                value.setValue(Boolean.valueOf(savedValue.getValue()));
                            }
                        }
                    }catch (Throwable t){ }
                } else if(value.getValue().getClass() == Color.class){
                    try {
                        for (Value<Integer> savedValue : settings.values) { // Color is an int
                            if (value.getName().equalsIgnoreCase(savedValue.getName())) {
                                try {
                                    String colorVal = String.valueOf(savedValue.getValue());
                                    ColorShitKillMePls colorShitKillMePls = gson.fromJson(colorVal, ColorShitKillMePls.class);
                                    Color col = new Color(Integer.valueOf(colorShitKillMePls.value));
                                    value.setValue(col);
                                } catch (Throwable t) {
                                    // oh well
                                }
                            }
                        }
                    }catch (Throwable t){ }
                } else if(value.getValue().getClass() == Integer.class){
                    try {
                        for(Value savedValue : settings.values){
                            if(value.getName().equalsIgnoreCase(savedValue.getName())){
                                value.setValueForceInt((double)(savedValue.getValue()));
                            }
                        }
                    }catch (Throwable t){ }
                } /*else if(value.getValue().getClass() == String.class){
                    for(Value<String> savedValue : settings.values){
                        if(value.getName().equalsIgnoreCase(savedValue.getName())){
                            value.setValue((String) savedValue.getValue());
                        }
                    }
                }*/ else{
                    // it's an enum, prolly
                    try {
                        for(Value savedValue : settings.values){
                            if(value.getName().equalsIgnoreCase(savedValue.getName())){
                                value.setEnumValue(String.valueOf(savedValue.getValue()));
                            }
                        }
                    }catch(Throwable t){
                        //ignoer
                    }
                }
            }
        }
    }

    @Override
    public void onSave() {

        ArrayList<ModuleConfigJSON> moduleConfigJSONS = new ArrayList<>();

        for(Module mod : Harakiri.get().getModuleManager().getModuleList()){

            if(mod.getType() == Module.ModuleType.LUA)
                continue; // We save luas separately

            if(mod.getType() == Module.ModuleType.CONFIG)
                continue; // Avoid loops

            ModuleConfigJSON config = new ModuleConfigJSON(mod.getDisplayName(), mod.isHidden(), mod.getKey(), mod.isEnabled());

            for(Value val : mod.getValueList()){
                config.values.add(val);
            }

            moduleConfigJSONS.add(config);
        }

        String strObj = new Gson().toJson(moduleConfigJSONS);

        this.saveStringToFile(strObj);
    }
}
