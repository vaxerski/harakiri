package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class AutoWalkModule extends Module {

    public final Value<Boolean> pressKeybind = new Value<>("PressKeybind", new String[]{"Keybind", "Key-bind", "PK", "P"}, "Presses the w key for you.", true);
    public final Value<Boolean> autoDisable = new Value<>("AutoDisable", new String[]{"Disable", "ad"}, "Automatically disables the module on disconnect or death.", true);
    public final Value<Boolean> useBaritone = new Value<>("Baritone", new String[]{"Baritone", "B"}, "Sends a baritone command on enable.", false);
    public final Value<String> baritoneCommand = new Value<>("Command", new String[]{"Com", "C", "Text"}, "The message you want to send to baritone. (please add the prefix)", "#explore");
    public final Value<String> baritoneCancelCommand = new Value<>("Cancel", new String[]{"BaritoneCancel", "Cancel", "Stop", "Text"}, "The cancel baritone command to send when disabled. (please add the prefix)", "#cancel");
    public final Value<Float> waitTime = new Value<Float>("MsgDelay", new String[]{"MessageDelay", "CommandDelay", "Delay", "Wait", "Time", "md", "d"}, "Delay (in ms) between sending baritone commands when standing.", 3000.0f, 0.0f, 8000.0f, 100.0f);

    private final Timer sendCommandTimer = new Timer();

    public AutoWalkModule() {
        super("AutoWalk", new String[]{"AutomaticWalk"}, "Automatically walks for you.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        //if (this.useBaritone.getValue())
        //    this.sendBaritoneCommand();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.useBaritone.getValue()) {
            this.cancelBaritoneCommand();
        }

        if (this.pressKeybind.getValue())
            Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = false;
    }

    @Listener
    public void onLoadWorld(EventLoadWorld event) {
        if (event.getWorld() != null) {
            if (this.autoDisable.getValue()) {
                this.toggle(); // toggle off
            }
        }
    }

    @Listener
    public void onDisplayGui(EventDisplayGui event) {
        if (event.getScreen() != null) {
            if (this.autoDisable.getValue()) {
                if (event.getScreen() instanceof GuiDisconnected) {
                    this.toggle(); // toggle off
                }
            }
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (this.useBaritone.getValue())
            return;

        if (this.pressKeybind.getValue())
            Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = true;
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage().equals(EventStageable.EventStage.PRE)) {
            if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null)
                return;

            if (this.autoDisable.getValue()) {
                if (!Minecraft.getMinecraft().player.isEntityAlive()) { // player is dead, check auto disable
                    Harakiri.get().logChat(this.getDisplayName() + ": " + "Disabled automatically.");
                    this.toggle(); // toggle off
                }
            }

            if (this.useBaritone.getValue()) {
                boolean isStanding = Minecraft.getMinecraft().player.motionX == 0 && Minecraft.getMinecraft().player.motionZ == 0;
                if (isStanding && this.sendCommandTimer.passed(this.waitTime.getValue())) {
                    this.sendCommandTimer.reset();
                    this.sendBaritoneCommand();
                }
                return;
            }

            if (this.pressKeybind.getValue())
                Minecraft.getMinecraft().gameSettings.keyBindForward.pressed = true;
        }
    }

    private void sendBaritoneCommand() {
        if (this.baritoneCommand.getValue().length() > 0) {
            Minecraft.getMinecraft().player.sendChatMessage(this.baritoneCommand.getValue());
        } else {
            Harakiri.get().logChat(this.getDisplayName() + ": " + "Please enter a command to send to baritone.");
            this.toggle(); // toggle off
        }
    }

    private void cancelBaritoneCommand() {
        if (this.baritoneCancelCommand.getValue().length() > 0) {
            Minecraft.getMinecraft().player.sendChatMessage(this.baritoneCancelCommand.getValue());
        } else {
            Harakiri.get().logChat(this.getDisplayName() + ": " + "Please check your syntax for the \"" + this.baritoneCancelCommand.getName() + "\" value.");
        }
    }
}
