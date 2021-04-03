package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;

public final class ServerBrandComponent extends DraggableHudComponent {

    public ServerBrandComponent() {
        super("ServerBrand");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);


        final String brand = mc.getCurrentServerData() == null ? ChatFormatting.GRAY + "Vanilla" :ChatFormatting.GRAY + mc.getCurrentServerData().gameVersion;

        this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(brand));
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(brand, this.getX(), this.getY(), -1);

    }

}