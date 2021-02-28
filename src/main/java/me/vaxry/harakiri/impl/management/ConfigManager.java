package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.config.Configurable;
import me.vaxry.harakiri.framework.event.client.EventLoadConfig;
import me.vaxry.harakiri.framework.event.client.EventSaveConfig;
import me.vaxry.harakiri.impl.config.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author noil
 */
public final class ConfigManager {

    private File configDir;
    private File moduleConfigDir;
    private File hudComponentConfigDir;
    private File luaDir;

    private boolean firstLaunch = false;

    private List<Configurable> configurableList = new ArrayList<>();

    public static final String CONFIG_PATH = "harakiri/config/";
    public static final String LUA_PATH = "harakiri/Lua/";

    public ConfigManager() {
        this.generateDirectories();
    }

    private void generateDirectories() {
        this.configDir = new File(CONFIG_PATH);
        if (!this.configDir.exists()) {
            this.setFirstLaunch(true);
            this.configDir.mkdirs();
        }
        this.luaDir = new File(LUA_PATH);
        if (!this.luaDir.exists()) {
            this.luaDir.mkdirs();
        }
    }

    public void init() {
        this.configurableList.add(new ModuleConfig(configDir));
        this.configurableList.add(new HudConfig(configDir));
        this.configurableList.add(new FriendConfig(configDir));
        this.configurableList.add(new XrayConfig(configDir));
        this.configurableList.add(new SearchConfig(configDir));
        this.configurableList.add(new MacroConfig(configDir));
        this.configurableList.add(new WorldConfig(configDir));

        if (this.firstLaunch) {
            this.saveAll();
        } else {
            this.loadAll();
        }
    }

    public void save(Class configurableClassType) {
        for (Configurable cfg : configurableList) {
            if (cfg.getClass().isAssignableFrom(configurableClassType)) {
                cfg.onSave();
            }
        }

        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventSaveConfig());
    }

    public void saveAll() {
        for (Configurable cfg : configurableList) {
            cfg.onSave();
        }
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventSaveConfig());
    }

    public void load(Class configurableClassType) {
        for (Configurable cfg : configurableList) {
            if (cfg.getClass().isAssignableFrom(configurableClassType)) {
                cfg.onLoad();
            }
        }
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventLoadConfig());
    }

    public void loadAll() {
        for (Configurable cfg : configurableList) {
            cfg.onLoad();
        }
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventLoadConfig());
    }

    public File getConfigDir() {
        return configDir;
    }

    public File getModuleConfigDir() {
        return moduleConfigDir;
    }

    public File getHudComponentConfigDir() {
        return hudComponentConfigDir;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    public void addConfigurable(Configurable configurable) {
        this.configurableList.add(configurable);
    }

    public List<Configurable> getConfigurableList() {
        return configurableList;
    }

    public void setConfigurableList(List<Configurable> configurableList) {
        this.configurableList = configurableList;
    }
}
