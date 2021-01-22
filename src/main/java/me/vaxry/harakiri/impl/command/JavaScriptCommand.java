package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.command.Command;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author Seth
 * 7/27/2019 @ 7:19 PM.
 * Credits https://stackoverflow.com/questions/1601246/java-scripting-api-how-to-stop-the-evaluation
 */
public final class JavaScriptCommand extends Command {

    public JavaScriptCommand() {
        super("JavaScript", new String[]{"Js"}, "Allows you to execute javascript client-side", "JavaScript <Syntax>");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final StringBuilder sb = new StringBuilder();

        for (int i = 1; i < split.length; i++) {
            final String s = split[i];
            sb.append(s + (i == split.length - 1 ? "" : " "));
        }

        final String syntax = sb.toString();

        try {
            Executors.newCachedThreadPool().submit(new ScriptRunnable(syntax)).get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Harakiri.INSTANCE.errorChat("Took too long to execute");
            Executors.newCachedThreadPool().shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Harakiri.INSTANCE.errorChat(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            Harakiri.INSTANCE.errorChat(e.getMessage());
        }
    }

    public static class ScriptRunnable implements Runnable {
        private String syntax;

        public ScriptRunnable(String syntax) {
            this.syntax = syntax;
        }

        @Override
        public void run() {
            try {
                final long time = System.nanoTime();
                ScriptEngine scriptEngine = new ScriptEngineManager(null).getEngineByName("nashorn");
                Harakiri.INSTANCE.logChat(scriptEngine.eval(syntax).toString());
                Harakiri.INSTANCE.logChat("Execution time: " + (System.nanoTime() - time) / 1000000 + "ms");
            } catch (ScriptException e) {
                e.printStackTrace();
                Harakiri.INSTANCE.errorChat(e.getMessage());
            }
        }

    }

}
