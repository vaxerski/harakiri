package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.HudComponent;
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

        final boolean isInside = isMouse(mouseX, mouseY, x - margin - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f, y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin, Harakiri.get().getTTFFontUtil().getStringWidth(str) + 2 * margin, Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 2 * margin);

        if(didClick && isInside)
            y = 0 + Harakiri.get().getTTFFontUtil().FONT_HEIGHT + margin;

        if(isInside && !didClick){
            RenderUtil.drawRoundedRect(x - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f - margin,
                    y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin,
                    Harakiri.get().getTTFFontUtil().getStringWidth(str) + 2 * margin,
                    Harakiri.get().getTTFFontUtil().FONT_HEIGHT + margin,
                    2,
                    0x77555555);
        }else{
            RenderUtil.drawRoundedRect(x - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f - margin,
                    y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin,
                    Harakiri.get().getTTFFontUtil().getStringWidth(str) + 2 * margin,
                    Harakiri.get().getTTFFontUtil().FONT_HEIGHT + margin,
                    2,
                    0x771D1D1D);
        }

        Harakiri.get().getTTFFontUtil().drawStringWithShadow(str, x - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f, y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin, 0xFFFFFFFF);
    }

    public void mouseClicked(int mouseX, int mouseY, int state){
        Minecraft mc = Minecraft.getMinecraft();
        if(!isMouse(mouseX, mouseY, x - margin - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f, y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin, Harakiri.get().getTTFFontUtil().getStringWidth(str) + 2 * margin, Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 2 * margin))
        {
            didClick = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        Minecraft mc = Minecraft.getMinecraft();
        if(isMouse(mouseX, mouseY, x - margin - Harakiri.get().getTTFFontUtil().getStringWidth(str)/2.f, y - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - margin, Harakiri.get().getTTFFontUtil().getStringWidth(str) + 2 * margin, Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 2 * margin))
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
