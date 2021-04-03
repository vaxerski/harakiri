package me.vaxry.harakiri.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Command;
import me.vaxry.harakiri.impl.module.config.ReloadConfigsModule;

public class CreateConfigCommand extends Command {

    private String[] clearAlias = new String[]{"Clear", "C"};

    public CreateConfigCommand() {
        super("CreateConfig", new String[]{"CC"}, "Allows you to create a new config.", "CreateConfig <name>");
    }

    @Override
    public void run(String input) {
        if (!this.verifyInput(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        ReloadConfigsModule reloadConfigsModule = ((ReloadConfigsModule) Harakiri.get().getModuleManager().find(ReloadConfigsModule.class));

        reloadConfigsModule.createNewConfig(split[1]);

        Harakiri.get().logChat("Created a new config: " + ChatFormatting.GREEN + split[1]);
    }
}
