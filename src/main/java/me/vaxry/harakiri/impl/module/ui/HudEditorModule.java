package me.vaxry.harakiri.impl.module.ui;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

/**
 * Author Seth
 * 7/25/2019 @ 4:16 AM.
 */
public final class HudEditorModule extends Module {

    public final Value<Boolean> blur = new Value<Boolean>("Blur", new String[]{"b"}, "Apply a blur effect to the Hud Editor's background.", false);
    public final Value<Boolean> tooltips = new Value<Boolean>("ToolTips", new String[]{"TT", "Tool"}, "Displays tooltips for modules.", true);
    public final Value<Float> rainspeed = new Value<Float>("RainbowSpeed", new String[]{"rs"}, "Rainbow effect speed.", 0.1f, 0.001f, 1.f, 0.01f);

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

        if (mc.world != null) {
            mc.displayGuiScreen(Harakiri.INSTANCE.getHudEditor());

            if (this.blur.getValue()) {
                if (OpenGlHelper.shadersSupported) {
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/blur.json"));
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
