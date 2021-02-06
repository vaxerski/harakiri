package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.framework.gui.hud.component.HudComponent;
import me.vaxry.harakiri.framework.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class SwitchViewComponent extends HudComponent {

    public boolean isModules;

    private float x = 0;
    private float y = 0;
    private float margin = 2.f;
    private String str = "";

    private boolean didClick;

    public SwitchViewComponent() {
        super("Switch view");
        this.ignore = true;
        didClick = false;
        isModules = true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {

        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        str = this.isModules ? "HUD Components Editor" : "Module Manager";

        x = res.getScaledWidth() / 2.f;
        y = res.getScaledHeight();

        if(didClick && isMouse(mouseX, mouseY, x - margin - mc.fontRenderer.getStringWidth(str)/2.f, y - mc.fontRenderer.FONT_HEIGHT - margin, mc.fontRenderer.getStringWidth(str) + 2 * margin, mc.fontRenderer.FONT_HEIGHT + 2 * margin))
            y = 0 + mc.fontRenderer.FONT_HEIGHT + margin;

        RenderUtil.drawRect(x - mc.fontRenderer.getStringWidth(str)/2.f - margin,
                y - mc.fontRenderer.FONT_HEIGHT - margin,
                x + mc.fontRenderer.getStringWidth(str)/2.f + margin,
                y,
                0x551D1D1D);

        mc.fontRenderer.drawStringWithShadow(str, x - mc.fontRenderer.getStringWidth(str)/2.f, y - mc.fontRenderer.FONT_HEIGHT - margin, 0xFFFFFFFF);
    }

    public void mouseClicked(int mouseX, int mouseY, int state){
        Minecraft mc = Minecraft.getMinecraft();
        if(!isMouse(mouseX, mouseY, x - margin - mc.fontRenderer.getStringWidth(str)/2.f, y - mc.fontRenderer.FONT_HEIGHT - margin, mc.fontRenderer.getStringWidth(str) + 2 * margin, mc.fontRenderer.FONT_HEIGHT + 2 * margin))
        {
            didClick = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        Minecraft mc = Minecraft.getMinecraft();
        if(isMouse(mouseX, mouseY, x - margin - mc.fontRenderer.getStringWidth(str)/2.f, y - mc.fontRenderer.FONT_HEIGHT - margin, mc.fontRenderer.getStringWidth(str) + 2 * margin, mc.fontRenderer.FONT_HEIGHT + 2 * margin))
        {
            isModules = !isModules;
        }
        didClick = false;
    }

    private boolean isMouse(int mx, int my, float x, float y, float w, float h) {
        if(mx > x && mx < x + w && my > y && my < y + h)
            return true;
        return false;
    }


}
