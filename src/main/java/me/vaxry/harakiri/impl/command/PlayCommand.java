package me.vaxry.harakiri.impl.command;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;
import me.vaxry.harakiri.impl.module.world.NoteBotModule;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

/**
 * @author noil
 */
public final class PlayCommand extends Command {

    private final ScriptEngineManager scriptFactory = new ScriptEngineManager();
    private final ScriptEngine scriptEngine = this.scriptFactory.getEngineByName("JavaScript");

    private File directory = null;

    public PlayCommand() {
        super("Play", new String[]{"playsong", "begin"}, "Plays a song file from your /harakiri/Songs/ directory.", ".play <song file name>");

        this.directory = new File(Harakiri.INSTANCE.getConfigManager().getConfigDir(), "Songs");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final NoteBotModule notebotModule = (NoteBotModule) Harakiri.INSTANCE.getModuleManager().find(NoteBotModule.class);
        if (notebotModule != null) {
            try {
                File file = new File(directory, split[1] + ".js");
                if (file.exists()) {
                    this.scriptEngine.getBindings(100).put("bot", notebotModule.getNotePlayer());
                    notebotModule.getNotePlayer().getNotesToPlay().clear();
                    notebotModule.setCurrentNote(0);
                    notebotModule.getState().setEnumValue("PLAYING");
                    this.scriptEngine.eval(new FileReader(file.getAbsolutePath()));
                    Harakiri.INSTANCE.logChat("Playing '" + ChatFormatting.YELLOW + file.getName().replaceAll(".js", "") + ChatFormatting.GRAY + "'");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
