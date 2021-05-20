package me.vaxry.harakiri.impl.module.world;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;


public final class TimerModule extends Module {

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Tickrate multiplier.", 4.0f, 0.0f, 10.0f, 0.1f);

    public TimerModule() {
        super("Timer", new String[]{"Time", "Tmr"}, "Speeds up the client's tick rate.", "NONE", -1, ModuleType.WORLD);
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

    Attender<EventPlayerUpdate> onUpdatePlayer = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().timer.tickLength = 50.0f / speed.getValue();
        }
    });
}
