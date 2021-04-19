package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.event.client.EventLoadConfig;
import me.vaxry.harakiri.framework.event.client.EventSaveConfig;
import me.vaxry.harakiri.impl.config.*;
import me.vaxry.harakiri.impl.module.config.ReloadConfigsModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ConfigManager {

    private File configDir;
    private File moduleConfigDir;
    private File hudComponentConfigDir;
    private File luaDir;

    public File current_config;

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
        this.backupConfigs(); // backup configs
        this.current_config = ((ReloadConfigsModule)Harakiri.get().getModuleManager().find(ReloadConfigsModule.class)).getCurrentConfigDir();

        this.configurableList.add(new ModuleConfig(this.current_config));
        this.configurableList.add(new HudConfig(this.current_config));
        this.configurableList.add(new LuaConfig(this.current_config));
        this.configurableList.add(new FriendConfig(this.current_config));
        this.configurableList.add(new XrayConfig(this.current_config));
        this.configurableList.add(new SearchConfig(this.current_config));
        this.configurableList.add(new MacroConfig(this.current_config));
        this.configurableList.add(new WorldConfig(this.current_config));

        if (this.firstLaunch) {
            this.saveAll();
        } else {
            this.loadAll();
        }
    }

    public void save(Class configurableClassType) {
        for (Configurable cfg : configurableList) {
            if (cfg.getClass().isAssignableFrom(configurableClassType)) {
                if(cfg instanceof FriendConfig)
                    cfg.forceGlobalDir(); // root path, dont
                else // Config-reliant settings
                    cfg.setNewConfigFileDir(this.current_config.getPath());
                cfg.onSave();
            }
        }

        Harakiri.get().getEventManager().dispatchEvent(new EventSaveConfig());
    }

    public void saveAll() {
        for (Configurable cfg : configurableList) {
            if(cfg instanceof FriendConfig)
                cfg.forceGlobalDir(); // root path, dont
            else // Config-reliant settings
                cfg.setNewConfigFileDir(this.current_config.getPath());
            cfg.onSave();
        }
        Harakiri.get().getEventManager().dispatchEvent(new EventSaveConfig());
    }

    public void load(Class configurableClassType) {
        for (Configurable cfg : configurableList) {
            if (cfg.getClass().isAssignableFrom(configurableClassType)) {
                if(cfg instanceof FriendConfig)
                    cfg.forceGlobalDir(); // root path, dont
                else // Config-reliant settings
                    cfg.setNewConfigFileDir(this.current_config.getPath());
                cfg.onLoad();
            }
        }
        Harakiri.get().getEventManager().dispatchEvent(new EventLoadConfig());
    }

    public void loadAll() {
        for (Configurable cfg : configurableList) {
            if(cfg instanceof FriendConfig)
                cfg.forceGlobalDir(); // root path, dont
            else // Config-reliant settings
                cfg.setNewConfigFileDir(this.current_config.getPath());
            cfg.onLoad();
        }
        Harakiri.get().getEventManager().dispatchEvent(new EventLoadConfig());
    }

    private void addDirToZipArchive(ZipOutputStream zos, File fileToZip, String parrentDirectoryName) throws Exception {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }

        String zipEntryName = fileToZip.getName();
        if (parrentDirectoryName!=null && !parrentDirectoryName.isEmpty()) {
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
        }

        if (fileToZip.isDirectory()) {
            System.out.println("+" + zipEntryName);
            for (File file : fileToZip.listFiles()) {
                addDirToZipArchive(zos, file, zipEntryName);
            }
        } else {
            System.out.println("   " + zipEntryName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    private void backupConfigs() {
        try {
            Path path = Paths.get(System.getenv("APPDATA") + "\\harakiri\\backup\\");
            Files.createDirectories(path);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            final String filename = "config " + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth() + " " + now.getHour() + "_" + now.getMinute() + ".zip";

            FileOutputStream fos = new FileOutputStream(System.getenv("APPDATA") + "\\harakiri\\backup\\" + filename);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addDirToZipArchive(zos, new File(System.getenv("APPDATA") + "\\.minecraft\\" + CONFIG_PATH), null);
            zos.flush();
            fos.flush();
            zos.close();
            fos.close();

        }catch (Throwable t){
            return;
        }
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
