package me.vaxry.harakiri.framework;

public final class Macro {

    private String name;
    private String key;
    private String macro;

    public Macro(String name, String key, String macro) {
        this.name = name;
        this.key = key;
        this.macro = macro;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMacro() {
        return macro;
    }

    public void setMacro(String macro) {
        this.macro = macro;
    }
}
