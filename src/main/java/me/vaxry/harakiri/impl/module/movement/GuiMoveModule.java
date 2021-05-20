package me.vaxry.harakiri.impl.module.movement;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


public final class GuiMoveModule extends Module {

    public GuiMoveModule() {
        super("GUIMove", new String[]{"InvMove", "InventoryMove", "GUIM"}, "Allows you to move while having open gui.", "NONE", -1, ModuleType.MOVEMENT);
    }

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen instanceof GuiChat || mc.currentScreen == null) {
                return;
            }

            final int[] keys = new int[]{mc.gameSettings.keyBindForward.getKeyCode(), mc.gameSettings.keyBindLeft.getKeyCode(), mc.gameSettings.keyBindRight.getKeyCode(), mc.gameSettings.keyBindBack.getKeyCode()};

            for (int keyCode : keys) {
                if (Keyboard.isKeyDown(keyCode)) {
                    KeyBinding.setKeyBindState(keyCode, true);
                } else {
                    KeyBinding.setKeyBindState(keyCode, false);
                }
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                if (mc.player.isInLava() || mc.player.isInWater()) {
                    mc.player.motionY += 0.039f;
                } else {
                    if (mc.player.onGround) {
                        mc.player.jump();
                    }
                }
            }

            if (Mouse.isButtonDown(2)) {
                Mouse.setGrabbed(true);
                mc.inGameHasFocus = true;
            } else {
                Mouse.setGrabbed(false);
                mc.inGameHasFocus = false;
            }
        }
    });
}
