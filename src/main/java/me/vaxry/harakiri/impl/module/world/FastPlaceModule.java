package me.vaxry.harakiri.impl.module.world;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.event.player.EventUpdateWalkingPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemExpBottle;


public final class FastPlaceModule extends Module {

    public final Value<Boolean> xp = new Value<Boolean>("OnlyXP", new String[]{"EXP"}, "Only activate while holding XP bottles.", false);

    public FastPlaceModule() {
        super("FastPlace", new String[]{"Fp"}, "Removes place delay.", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().rightClickDelayTimer = 6;
    }

    Attender<EventPlayerUpdate> onPlayerUpdate = new Attender<>(EventPlayerUpdate.class, event -> {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.xp.getValue()) {
                if (Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemExpBottle || Minecraft.getMinecraft().player.getHeldItemOffhand().getItem() instanceof ItemExpBottle) {
                    Minecraft.getMinecraft().rightClickDelayTimer = 0;
                }
            } else {
                Minecraft.getMinecraft().rightClickDelayTimer = 0;
            }
        }
    });
}
