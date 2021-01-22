package me.vaxry.harakiri.api.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import net.minecraft.util.text.TextComponentString;

/**
 * Author Seth
 * 4/16/2019 @ 8:27 AM.
 */
public abstract class Command {

    private String displayName;
    private String[] alias;
    private String desc;
    private String usage;

    private TextComponentString textComponentUsage;

    public Command() {

    }

    public Command(String displayName, String[] alias, String desc, String usage) {
        this.displayName = displayName;
        this.alias = alias;
        this.desc = desc;
        this.usage = usage;
    }

    public Command(String displayName, String[] alias, String desc, TextComponentString textComponentUsage) {
        this(displayName, alias, desc, textComponentUsage.getText());
        this.textComponentUsage = textComponentUsage;
    }

    public abstract void exec(String input);

    public boolean clamp(String input, int min, int max) {
        String[] split = input.split(" ");
        if (split.length > max) {
            Harakiri.INSTANCE.errorChat("Too much input");
            return false;
        }
        if (split.length < min) {
            Harakiri.INSTANCE.errorChat("Not enough input");
            return false;
        }
        return true;
    }

    public boolean clamp(String input, int min) {
        String[] split = input.split(" ");
        if (split.length < min) {
            Harakiri.INSTANCE.errorChat("Not enough input");
            return false;
        }
        return true;
    }

    public boolean equals(String[] list, String input) {
        for (String s : list) {
            if (s.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }

    public void printUsage() {
        final String[] usage = this.getUsage().split("\n");
        Harakiri.INSTANCE.logChat(ChatFormatting.GRAY + this.getDisplayName() + " usage: ");

        if (this.textComponentUsage != null) {
            this.getTextComponentUsage().getSiblings().forEach(Harakiri.INSTANCE::logcChat);
        } else {
            for (String u : usage) {
                Harakiri.INSTANCE.logChat(u);
            }
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String[] getAlias() {
        return alias;
    }

    public void setAlias(String[] alias) {
        this.alias = alias;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public TextComponentString getTextComponentUsage() {
        return textComponentUsage;
    }

    public void setTextComponentUsage(TextComponentString textComponentUsage) {
        this.textComponentUsage = textComponentUsage;
    }
}
