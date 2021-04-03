package me.vaxry.harakiri.impl.module.ui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public final class HudEditorModule extends Module {

    public final Value<Color> color = new Value<Color>("AccentColor", new String[]{"color", "accentcolor"}, "Change the accent color.", new Color(255, 255, 255));
    public final Value<Integer> colorR = new Value<Integer>("AccentColorR", new String[]{"colorR", "accentcolorR"}, "Change the accent color R.", 0xCC);
    public final Value<Integer> colorG = new Value<Integer>("AccentColorG", new String[]{"colorG", "accentcolorG"}, "Change the accent color G.", 0xFF);
    public final Value<Integer> colorB = new Value<Integer>("AccentColorB", new String[]{"colorB", "accentcolorB"}, "Change the accent color B.", 0x66);
    public final Value<Boolean> blur = new Value<Boolean>("Blur", new String[]{"b"}, "Apply a blur effect to the Hud Editor's background.", false);
    public final Value<Boolean> tooltips = new Value<Boolean>("ToolTips", new String[]{"TT", "Tool"}, "Displays tooltips for modules.", true);
    public final Value<Float> rainspeed = new Value<Float>("RainbowSpeed", new String[]{"rs"}, "Rainbow effect speed.", 0.1f, 0.001f, 1.f, 0.01f);
    //public final Value<Float> test = new Value<Float>("test", new String[]{"test"}, "test", 0.1f, 0.001f, 1000.f, 0.01f);

    private boolean open;

    public HudEditorModule() {
        super("Gui", new String[]{"Gui"}, "Displays the gui", "RSHIFT", -1, ModuleType.UI);
        this.setHidden(true);
    }

    @Override
    public void onToggle() {
        super.onToggle();
        this.displayHudEditor();
    }

    public void displayHudEditor() {
        final Minecraft mc = Minecraft.getMinecraft();

        this.color.setValue(new Color(colorR.getValue(), colorG.getValue(), colorB.getValue()));

        if (mc.world != null) {
            mc.displayGuiScreen(Harakiri.get().getHudEditor());

            if (this.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "assets/harakirimod/shaders/post/blura.json"));
                }
            }

            this.open = true;

            this.setEnabled(false);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
