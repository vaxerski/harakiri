package me.vaxry.harakiri.impl.module.hidden;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.framework.Module;
import org.lwjgl.input.Keyboard;


public final class KeybindsModule extends Module {

    public KeybindsModule() {
        super("Keybinds", new String[]{"Binds"}, "Allows you to bind modules to keys", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        if(!this.isEnabled())
            this.toggle();
    }

    Attender<EventKeyPress> onKeyPress = new Attender<>(EventKeyPress.class, event -> {
        for (Module mod : Harakiri.get().getModuleManager().getModuleList()) {
            if (mod != null) {
                if (mod.getType() != ModuleType.HIDDEN && event.getKey() == Keyboard.getKeyIndex(mod.getKey()) && Keyboard.getKeyIndex(mod.getKey()) != Keyboard.KEY_NONE) {
                    mod.toggle();
                }
            }
        }
    });

}
