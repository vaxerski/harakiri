package me.vaxry.harakiri.framework;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.lua.LUAAPI;
import me.vaxry.harakiri.impl.module.config.ReloadConfigsModule;
import me.vaxry.harakiri.impl.module.lua.ReloadLuasModule;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Module {

    private String displayName;
    private String[] alias;
    private String desc;
    private String key;
    private int color;
    private boolean hidden;
    private boolean enabled;
    private ModuleType type;

    public float activexOffset = 0;
    public float xOffset = 0;
    public String luaName = "";
    public float highlightA = 0;

    private List<Value> valueList = new ArrayList<Value>();

    public Module() {

    }

    public Module(String luaname, ModuleType type){
        String[] aliases = new String[1];
        aliases[0] = luaname;
        this.displayName = luaname;
        this.alias = aliases;
        this.hidden = false;
        this.desc = "A custom LUA script.";
        this.luaName = luaname;
        this.type = type;
        this.key = "NONE";
    }

    public Module(File configfile, String configName, ModuleType type){
        // For cfgs
        String[] aliases = new String[1];
        aliases[0] = configName;
        this.displayName = configName;
        this.alias = aliases;
        this.hidden = true;
        this.desc = "A config.";
        this.luaName = configName;
        this.type = type;
        this.key = "NONE";
    }

    public Module(String displayName, String[] alias, String key, int color, ModuleType type) {
        this.displayName = displayName;
        this.alias = alias;
        this.key = key;
        this.color = color;
        this.type = type;

    }

    public Module(String displayName, String[] alias, String desc, String key, int color, ModuleType type) {
        this(displayName, alias, key, color, type);
        this.desc = desc;
    }

    public Module(String displayName, String[] alias, String desc, String key, int color, boolean hidden, boolean enabled, ModuleType type) {
        this(displayName, alias, desc, key, color, type);
        this.hidden = hidden;
        this.enabled = enabled;
    }

    public void onEnable() {
        Harakiri.get().getEventManager().addEventListener(this);

        if(this.type == ModuleType.LUA && !(this instanceof ReloadLuasModule)){
            try {
                LUAAPI.onEnabled(
                        ((ReloadLuasModule)Harakiri.get().getModuleManager().find(ReloadLuasModule.class))
                                .getLuaModuleByName(this.luaName));
            }catch (Throwable t){
                // Something failed. too bad!
            }
        }

        if(this.type == ModuleType.CONFIG && !(this instanceof ReloadConfigsModule)){
            for(Module mod : Harakiri.get().getModuleManager().getModuleList()){
                if(mod.type != ModuleType.CONFIG)
                    continue;

                if(mod == this)
                    continue;

                mod.forceDisableConfigModule();
            }

            // Push the change to the handler

            ReloadConfigsModule reloadConfigsModule = ((ReloadConfigsModule)Harakiri.get().getModuleManager().find(ReloadConfigsModule.class));

            if(reloadConfigsModule != null) // First launch will be null, thats why i set it in the module
                reloadConfigsModule.setNewConfigFromMod(this);
        }
    }

    public void onDisable() {
        if(this instanceof ReloadLuasModule)
            return; // Never

        if(this.type == ModuleType.CONFIG){
            this.setEnabled(true); // Never x2
            return;
        }

        Harakiri.get().getEventManager().removeEventListener(this);

        if(this.type == ModuleType.LUA /*&& !(this instanceof ReloadLuasModule)  Redundant  */){
            try {
                LUAAPI.onDisabled(
                        ((ReloadLuasModule)Harakiri.get().getModuleManager().find(ReloadLuasModule.class))
                                .getLuaModuleByName(this.luaName));
            }catch (Throwable t){
                // Something failed. too bad!
            }
        }
    }

    public void forceDisableConfigModule(){
        this.setEnabled(false);
    }

    public void onToggle() {

    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
        if (this.isEnabled()) {
            this.onEnable();
            this.activexOffset = Harakiri.get().getTTFFontUtil().getStringWidth(this.getDisplayName());
        } else {
            this.onDisable();
        }
        this.onToggle();
    }

    public String getMetaData() {
        return null;
    }

    public TextComponentString toUsageTextComponent() {
        if (this.valueList.size() <= 0) {
            return null;
        }

        final String valuePrefix = " " + ChatFormatting.RESET;
        final TextComponentString msg = new TextComponentString("");
        final DecimalFormat df = new DecimalFormat("#.##");

        for (Value v : this.getValueList()) {
            if (v.getValue() instanceof Boolean) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ": " + ((Boolean) v.getValue() ? ChatFormatting.GREEN : ChatFormatting.RED) + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "No description." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<true / false>")))));
            }

            if (v.getValue() instanceof Number && !(v.getValue() instanceof Enum)) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <amount>" + ChatFormatting.RESET + ": " + ChatFormatting.AQUA + (df.format(v.getValue()))).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "No description." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<" + v.getMin() + " - " + v.getMax() + ">")))));
            }

            if (v.getValue() instanceof String) {
                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <text>" + ChatFormatting.RESET + ": " + v.getValue()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "No description." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<text>")))));
            }

            if (v.getValue() instanceof Enum) {
                final Enum val = (Enum) v.getValue();
                final StringBuilder options = new StringBuilder();
                final int size = val.getClass().getEnumConstants().length;

                for (int i = 0; i < size; i++) {
                    final Enum option = val.getClass().getEnumConstants()[i];
                    options.append(option.name().toLowerCase() + ((i == size - 1) ? "" : ", "));
                }

                msg.appendSibling(new TextComponentString(valuePrefix + v.getName() + ChatFormatting.GRAY + " <" + options.toString() + ">" + ChatFormatting.RESET + ": " + ChatFormatting.YELLOW + val.name().toLowerCase()).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(v.getName() + "\n" + ChatFormatting.GOLD + ((v.getDesc() == null || v.getDesc().equals("")) ? "No description." : v.getDesc()) + ChatFormatting.RESET + "\n " + ChatFormatting.GRAY + "<" + options.toString() + ">")))));
            }
        }

        return msg;
    }

    public Value findValue(String alias) {
        for (Value v : this.getValueList()) {
            for (String s : v.getAlias()) {
                if (alias.equalsIgnoreCase(s)) {
                    return v;
                }
            }

            if (v.getName().equalsIgnoreCase(alias)) {
                return v;
            }
        }
        return null;
    }

    public void unload() {
        this.valueList.clear();
    }

    public void onFullLoad(){

    }

    public enum ModuleType {
        COMBAT, MOVEMENT, RENDER, PLAYER, WORLD, MISC, HIDDEN, UI, LUA, CONFIG
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName.equals("true") || displayName.equals("false")) {
            this.displayName = this.getAlias()[0];
            return;
        }
        this.displayName = displayName;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String getDesc() {
        if (this.desc == null) {
            return "No description to be found.";
        }
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getColor() {
        return color;
    }

    public int getColorByGroup(){
        switch (this.type){
            case UI:
            case HIDDEN:
                return 0xFF000000;
            case MISC:
                return 0xFF3BB300;
            case WORLD:
                return 0xFF007ACC;
            case COMBAT:
                return 0xFFFF6600;
            case PLAYER:
                return 0xFF00CC99;
            case RENDER:
                return 0xFFB366FF;
            case MOVEMENT:
                return 0xFF009933;
            case LUA:
                return 0xFF7777FF;
            case CONFIG:
                return 0xFF3399FF;
        }
        return 0xFF000000;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ModuleType getType() {
        return type;
    }

    public void setType(ModuleType type) {
        this.type = type;
    }

    public List<Value> getValueList() {
        return valueList;
    }

    public void clearValues(){
        valueList.clear();
    }

    public void addValueInt(String n, String[] a, int def, int min, int max){
        valueList.add(new Value<Integer>(n, a, n, def, min, max, 1));
    }

    public void addValueFloat(String n, String[] a, float def, float min, float max){
        valueList.add(new Value<Float>(n, a, n, def, min, max, 0.1f));
    }

    public void addValueBool(String n, String[] a, boolean def){
        valueList.add(new Value<Boolean>(n, a, n, def));
    }

    public void setValueList(List<Value> valueList) {
        this.valueList = valueList;
    }
}
