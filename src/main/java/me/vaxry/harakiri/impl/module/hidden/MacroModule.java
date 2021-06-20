package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.framework.Macro;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class MacroModule extends Module {

    public MacroModule() {
        super("Macros", new String[]{"mac"}, "Allows you to bind macros to keys", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.setEnabled(true);
        this.onEnable();
    }

    @Listener
    public void keyPress(EventKeyPress event) {
        for (Macro macro : Harakiri.get().getMacroManager().getMacroList()) {
            if (event.getKey() == Keyboard.getKeyIndex(macro.getKey()) && Keyboard.getKeyIndex(macro.getKey()) != Keyboard.KEY_NONE) {
                final String[] split = macro.getMacro().split(";");

                for (String s : split) {
                    if(s.indexOf("#") == 0) s = "." + s;
                    Minecraft.getMinecraft().player.sendChatMessage(s);
                }
            }
        }
    }
}
