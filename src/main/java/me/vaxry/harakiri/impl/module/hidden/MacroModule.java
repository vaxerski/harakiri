package me.vaxry.harakiri.impl.module.hidden;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.framework.Macro;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;


public final class MacroModule extends Module {

    public MacroModule() {
        super("Macros", new String[]{"mac"}, "Allows you to bind macros to keys", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.setEnabled(true);
        this.onEnable();
    }

    Attender<EventKeyPress> onKeyPress = new Attender<>(EventKeyPress.class, event -> {
        for (Macro macro : Harakiri.get().getMacroManager().getMacroList()) {
            if (event.getKey() == Keyboard.getKeyIndex(macro.getKey()) && Keyboard.getKeyIndex(macro.getKey()) != Keyboard.KEY_NONE) {
                final String[] split = macro.getMacro().split(";");

                for (String s : split) {
                    s = "." + s;
                    Minecraft.getMinecraft().player.sendChatMessage(s);
                }
            }
        }
    });
}
