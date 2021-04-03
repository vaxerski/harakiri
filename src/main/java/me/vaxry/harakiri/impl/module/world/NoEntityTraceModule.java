package me.vaxry.harakiri.impl.module.world;

import me.vaxry.harakiri.framework.event.player.EventGetMouseOver;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemTool;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoEntityTraceModule extends Module {

    public Value<Boolean> toolsOnly = new Value<Boolean>("Tools", new String[]{"OnlyTools", "Tool", "Pickaxe", "Axe", "Shovel"}, "Only enable when holding a tool.", true);

    public NoEntityTraceModule() {
        super("NoEntityHit", new String[]{"MineThrough", "MineThrough", "MineThrough", "MineT"}, "Do stuff through entities.", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onGetMouseOver(EventGetMouseOver event) {
        if (this.toolsOnly.getValue()) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null) {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemTool ||
                        mc.player.getHeldItemOffhand().getItem() instanceof ItemTool) {
                    event.setCanceled(true);
                }
            }
            return; // return so we don't cancel swords, swinging hand, etc
        }

        event.setCanceled(true);
    }
}
