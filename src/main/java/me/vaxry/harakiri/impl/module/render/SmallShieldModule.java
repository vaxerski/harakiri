package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.render.EventRender3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;


public final class SmallShieldModule extends Module {

    final Minecraft mc = Minecraft.getMinecraft();
    ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;

    public SmallShieldModule() {
        super("SmallShield", new String[]{"SmallShield", "SS"}, "Smaller offhand item.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventPlayerUpdate> onplayer = new Attender<>(EventPlayerUpdate.class, event -> {
        itemRenderer.equippedProgressOffHand = 0.5F;
    });
}
