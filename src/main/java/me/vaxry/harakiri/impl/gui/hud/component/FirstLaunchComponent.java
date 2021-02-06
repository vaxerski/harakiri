package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.module.render.HudModule;
import net.minecraft.util.text.TextFormatting;

/**
 * created by noil on 5/7/2020
 */
public final class FirstLaunchComponent extends DraggableHudComponent {

    private final Module hudModule;

    private String textData;

    public FirstLaunchComponent() {
        super("FirstLaunch");

        final String textData = TextFormatting.WHITE + "Welcome to the " + TextFormatting.RESET + "harakiri Client\n\n" +
                TextFormatting.WHITE + "Press ~ (GRAVE) or RSHIFT to open the GUI.";
        this.setTextData(textData);

        this.setVisible(true);
        this.setSnappable(false);
        this.setW(188);
        this.setH(38);
        this.setX(2);
        this.setY(2);

        this.hudModule = Harakiri.INSTANCE.getModuleManager().find(HudModule.class);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        // Background
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0x99202020);

        // Render text data
        mc.fontRenderer.drawSplitString(this.textData, (int) this.getX() + 2, (int) this.getY() + 2, 200, 0xFF9900EE);
    }

    public void onClose() {
        if (this.hudModule != null) {
            if (this.hudModule.isEnabled()) {
                this.hudModule.onEnable();
            } else {
                this.hudModule.toggle();
            }
            this.hudModule.setEnabled(true);
        }

        this.setVisible(false);
    }

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }
}
