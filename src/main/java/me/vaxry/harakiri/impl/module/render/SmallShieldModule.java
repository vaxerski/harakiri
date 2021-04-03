package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class SmallShieldModule extends Module {

    final Minecraft mc = Minecraft.getMinecraft();
    ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;

    public SmallShieldModule() {
        super("SmallShield", new String[]{"SmallShield", "SS"}, "Smaller offhand item.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void changeOffhandProgress(EventPlayerUpdate event) {
        itemRenderer.equippedProgressOffHand = 0.5F;
    }
}
