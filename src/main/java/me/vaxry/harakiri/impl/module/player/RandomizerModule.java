package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Random;

public class RandomizerModule extends Module {

    public final Value<Integer> start = new Value<Integer>("Start", new String[]{"Start"}, "Start at slot", 0, 0, 9, 1);
    public final Value<Integer> end = new Value<Integer>("End", new String[]{"End"}, "End at slot", 0, 0, 9, 1);
    public final Value<Integer> wait = new Value<Integer>("Wait", new String[]{"Wait"}, "Wait for x ticks between switches", 0, 0, 20, 1);

    private int waitedTicks = 0;

    public RandomizerModule() {
        super("Randomizer", new String[]{"Randomizer"}, "Randomizes the current slot.", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if(waitedTicks < wait.getValue()){
            waitedTicks++;
            return;
        }

        waitedTicks = 0;

        Random rand = new Random();

        int randomOut = rand.nextInt(end.getValue() - start.getValue());

        mc.player.inventory.currentItem = randomOut + start.getValue();
        mc.playerController.updateController();
    }
}
