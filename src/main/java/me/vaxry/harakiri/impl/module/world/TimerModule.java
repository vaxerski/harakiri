package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/19/2019 @ 10:06 PM.
 */
public final class TimerModule extends Module {

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Tick-rate multiplier. [(20tps/second) * (this value)]", 4.0f, 0.0f, 10.0f, 0.1f);

    public TimerModule() {
        super("Timer", new String[]{"Time", "Tmr"}, "Speeds up the client tick rate", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().timer.tickLength = 50;
    }

    @Override
    public String getMetaData() {
        return "" + this.speed.getValue();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().timer.tickLength = 50.0f / speed.getValue();
        }
    }

}
