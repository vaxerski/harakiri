package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderPotions;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.gui.HudComponent;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.framework.gui.anchor.AnchorPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class HudModule extends Module {

    public final Value<Boolean> hidePotions = new Value<Boolean>("HidePotions", new String[]{"HidePotions", "HidePots", "Hide_Potions"}, "Hides the Vanilla potion hud (at the top right of the screen).", true);

    /* rainbow */
    public final Value<Boolean> rainbow = new Value<Boolean>("Rainbow", new String[]{"Rainbow", "rb", "rain_bow"}, "Enables rainbow color features across the hud if applicable.", false);
    public final Value<Float> rainbowHueDifference = new Value<Float>("HueDifference", new String[]{"HueDiff", "Hd", "RainbowHueDifference", "Rhd"}, "Control the rainbow hue difference.", 2.0f, 1.0f, 5.0f, 0.1f);
    public final Value<Float> rainbowHueSpeed = new Value<Float>("HueSpeed", new String[]{"Hs", "RainbowHueSpeed", "Rhs"}, "Control the rainbow hue speed.", 1.0f, 0.5f, 3.0f, 0.1f);
    public final Value<Float> rainbowSaturation = new Value<Float>("Saturation", new String[]{"sat", "str", "satur", "RainbowSaturation", "Rs"}, "Control the rainbow saturation.", 1.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Float> rainbowBrightness = new Value<Float>("Brightness", new String[]{"bri", "bright", "RainbowBrightness", "Rb"}, "Control the rainbow brightness.", 1.0f, 0.0f, 1.0f, 0.1f);

    public HudModule() {
        super("Hud", new String[]{"Overlay"}, "Renders hud components on the screen.", "NONE", -1, ModuleType.RENDER);
        this.setHidden(true);
        this.setEnabled(true);
        this.onEnable();
    }

    @Listener
    public void render(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.gameSettings.showDebugInfo || mc.currentScreen instanceof GuiHudEditor || mc.player == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        for (HudComponent component : Harakiri.get().getHudManager().getComponentList()) {
            if (component.isVisible()) {
                //dont render components with the TOP_CENTER anchor if we are looking at the tab list
                if (component instanceof DraggableHudComponent) {
                    final DraggableHudComponent draggableComponent = (DraggableHudComponent) component;
                    if (draggableComponent.getAnchorPoint() != null && draggableComponent.getAnchorPoint().getPoint() == AnchorPoint.Point.TOP_CENTER) {
                        if (!mc.gameSettings.keyBindPlayerList.isKeyDown()) {
                            draggableComponent.render(0, 0, mc.getRenderPartialTicks());
                        }
                    } else {
                        draggableComponent.render(0, 0, mc.getRenderPartialTicks());
                    }
                } else {
                    component.render(0, 0, mc.getRenderPartialTicks());
                }
            }
        }

        if(Harakiri.get().getUsername().equalsIgnoreCase(""))
            Harakiri.get().getApiManager().killThisThing(); // Anti crack, some sort of

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Listener
    public void renderPotions(EventRenderPotions event) {
        if (this.hidePotions.getValue()) {
            event.setCanceled(true);
        }
    }
}
