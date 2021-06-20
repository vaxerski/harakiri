package me.vaxry.harakiri.impl.module.config;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.impl.manager.ModuleManager;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ReloadConfigsModule extends Module {

    public ArrayList<Config> configs = new ArrayList<>();
    private ArrayList<Module> configModules = new ArrayList<>();

    private boolean loadWorldFirst = true;

    public ModuleManager moduleManager = null; // Used cuz Harakiri.get() wont work on the first launch

    private Config selected_config = null;

    public ReloadConfigsModule(ModuleManager moduleManager) {
        super("Reload Configs", new String[]{"ReloadCFG", "ReloadConfig"}, "Reload the config list", "NONE", -1, ModuleType.CONFIG);
        this.moduleManager = moduleManager;
        this.reloadConfigs();
        Harakiri.get().getEventManager().addEventListener(this);
    }

    @Override
    public void onEnable() {
        this.setEnabled(false);
        reloadConfigs();
        super.onEnable();
    }

    @Listener
    public void worldLoad(EventLoadWorld event){
        if(loadWorldFirst) {
            loadWorldFirst = false;
            reloadConfigs(); // Sort thing
        }
    }

    public void reloadConfigs(){

        // First, get the last used config. If none, create one.
        File file = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config" : "\\harakiri\\config"));
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        if(directories.length == 0) {
            if(!fixOldConfigStyle()) {}
            directories = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });
        }

        // We have correct configs now, in directories[]

        this.configs.clear();

        for(String configDir : directories){
            // Create our config classes

            this.configs.add(new Config(new File(configDir)));
        }

        // Clean up old (if existing) Modules

        for(Module mod : this.configModules){
            this.moduleManager.removeConfigModule(mod);
        }

        this.configModules.clear();

        // Add new
        for(Config cfg : this.configs){
            Module mod = new Module(cfg.cfgFile, cfg.name, ModuleType.CONFIG);
            this.configModules.add(mod);
            this.moduleManager.add(mod);
        }

        // Toggle the correct one on
        File lastConfigReader = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/configManager.txt" : "\\harakiri\\config\\configManager.txt"));
        try {
            if (!lastConfigReader.exists()) {
                lastConfigReader.createNewFile();
                saveStringToFile("default", lastConfigReader);
            }
        }catch (IOException e){ ; }

        String selected_config = loadRawFile(lastConfigReader);

        Config selectedCfg = null;
        for(Config cfg : this.configs){
            if(cfg.name.equalsIgnoreCase(selected_config)){
                selectedCfg = cfg;
                break;
            }
        }

        if(selectedCfg == null)
            selectedCfg = this.configs.get(0);

        // Selected correct config

        Module config_mod = this.moduleManager.getConfigFromName(selectedCfg.name);

        if(config_mod != null) {
            if (!config_mod.isEnabled()) {
                config_mod.toggle();
            }
        }

        this.selected_config = selectedCfg;

        saveStringToFile(this.selected_config.name, lastConfigReader);

        try{
            Harakiri.get().logChat(ChatFormatting.GRAY + "Reloaded " + ChatFormatting.RESET + this.configs.size() + ChatFormatting.GRAY + " configs.");
        }catch (Throwable t){ ; }
    }

    protected void saveStringToFile(String str, File f){
        try {
            FileWriter writer = new FileWriter(f);
            writer.write(str);
            writer.close();
        }catch (Throwable t){
            //Harakiri.get().errorChat("Couldn't save the config.");
        }
    }

    private String readAllBytesJava7(String filePath)
    {
        String content = "";

        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }

    protected String loadRawFile(File f){
        try {
            String res;
            res = readAllBytesJava7(f.getAbsolutePath());
            return res;
        }catch (Throwable t){
            ;
        }
        return "";
    }

    private boolean fixOldConfigStyle(){
        Path configPath = Paths.get(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/default" : "\\harakiri\\config\\default"));
        try {
            Files.createDirectories(configPath);

            File file = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/" : "\\harakiri\\config\\"));
            String[] files = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return !(new File(current, name).isDirectory()); // only files
                }
            });

            if(files.length == 0){
                // First launch...
                return false;
            }else{
                for(String f : files){
                    if(Harakiri.isNix())
                        f = Minecraft.getMinecraft().gameDir + "/harakiri/config/" + f;
                    else
                        f = Minecraft.getMinecraft().gameDir + "\\harakiri\\config\\" + f;
                    File fi = new File(f);
                    String renameLoc = f;
                    if(Harakiri.isNix())
                        f = f.substring(0, f.lastIndexOf('/')) + "default/" + f.substring(f.lastIndexOf('/') + 1);
                    else
                        f = f.substring(0, f.lastIndexOf('\\')) + "default\\" + f.substring(f.lastIndexOf('\\') + 1);
                    fi.renameTo(new File(f));
                }
            }

            return true;
        }catch (IOException e){
            // Woops
        }

        return true;
    }

    public File getCurrentConfigDir(){
        return this.selected_config.cfgFile;
    }

    public void setNewConfigFile(File dir){
        Harakiri.get().getConfigManager().current_config = dir;

        Config selectedCfg = null;
        for(Config cfg : this.configs){
            if(cfg.cfgFile.getAbsolutePath().equalsIgnoreCase(dir.getAbsolutePath())){
                selectedCfg = cfg;
                break;
            }
        }

        this.selected_config = selectedCfg;

        File lastConfigReader = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/configManager.txt" : "\\harakiri\\config\\configManager.txt"));
        try {
            if (!lastConfigReader.exists()) {
                lastConfigReader.createNewFile();
                saveStringToFile("default", lastConfigReader);
            }
        }catch (IOException e){ ; }
        saveStringToFile(this.selected_config.name, lastConfigReader);
    }

    public void setNewConfigFile2(File file, String n){
        Harakiri.get().getConfigManager().current_config = file;

        Config selectedCfg = null;
        for(Config cfg : this.configs){
            if(cfg.name.equalsIgnoreCase(n)){
                selectedCfg = cfg;
                break;
            }
        }

        this.selected_config = selectedCfg;

        File lastConfigReader = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/configManager.txt" : "\\harakiri\\config\\configManager.txt"));
        try {
            if (!lastConfigReader.exists()) {
                lastConfigReader.createNewFile();
                saveStringToFile("default", lastConfigReader);
            }
        }catch (IOException e){ ; }
        saveStringToFile(this.selected_config.name, lastConfigReader);
    }

    public void setNewConfigFromMod(Module mod){

        Harakiri.get().getConfigManager().saveAll();

        File dir = null;

        for(Config cfg : this.configs){
            if(cfg.name == mod.getDisplayName()) {
                dir = cfg.cfgFile;
                break;
            }
        }

        if(dir == null){
            try {
                Harakiri.get().logChat("Config not found: " + mod.getDisplayName());
            }catch (NullPointerException e) { ; }

            return;
        }

        Harakiri.get().getConfigManager().current_config = dir;

        Config selectedCfg = null;
        for(Config cfg : this.configs){
            if(cfg.cfgFile.getAbsolutePath().equalsIgnoreCase(dir.getAbsolutePath())){
                selectedCfg = cfg;
                break;
            }
        }

        this.selected_config = selectedCfg;

        Harakiri.get().getConfigManager().loadAll();

        File lastConfigReader = new File(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/configManager.txt" : "\\harakiri\\config\\configManager.txt"));
        try {
            if (!lastConfigReader.exists()) {
                lastConfigReader.createNewFile();
                saveStringToFile("default", lastConfigReader);
            }
        }catch (IOException e){ ; }
        saveStringToFile(this.selected_config.name, lastConfigReader);

        try{
            Harakiri.get().logChat(ChatFormatting.GRAY + "Loaded " + ChatFormatting.RESET + this.selected_config.name);
        }catch (Throwable t){ ; }
    }

    public void createNewConfig(String name) {
        Path configPath = Paths.get(Minecraft.getMinecraft().gameDir + (Harakiri.isNix() ? "/harakiri/config/" : "\\harakiri\\config\\") + name);
        try {
            Files.createDirectories(configPath);
        }catch(Throwable t){
            ;
        }

        this.reloadConfigs();

        this.setNewConfigFile2(new File(name), name);

        Harakiri.get().getConfigManager().saveAll(); // Save to the new config
    }

    public class Config{
        File cfgFile;
        String name;

        public Config(File file){
            cfgFile = file;
            name = file.getPath();
            if(!Harakiri.isNix()) {
                if (name.indexOf('\\') != -1)
                    name = name.substring(file.getPath().lastIndexOf('\\'));
            }else{
                if (name.indexOf('/') != -1)
                    name = name.substring(file.getPath().lastIndexOf('/'));
            }
        }
    }
}
