package me.vaxry.harakiri.framework.util;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GUIUtil extends GuiIngameForge {

    public GUIUtil(Minecraft mc)
    {
        super(mc);
    }

    @Override
    public void renderGameOverlay(float partialTicks)
    {
        super.renderGameOverlay(partialTicks);

        Harakiri.get().getEventManager().dispatchEvent(new EventRender2D(partialTicks, new ScaledResolution(Minecraft.getMinecraft())));
    }
}
