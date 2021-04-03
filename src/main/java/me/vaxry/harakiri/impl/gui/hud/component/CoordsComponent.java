package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

public final class CoordsComponent extends DraggableHudComponent {

    public CoordsComponent() {
        super("Coords");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final DecimalFormat df = new DecimalFormat("#.#");

            String coordz = ChatFormatting.GRAY + "XYZ: " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                    df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posZ);

            NetherCoordsComponent ncc = (NetherCoordsComponent)Harakiri.get().getHudManager().findComponent(NetherCoordsComponent.class);
            if(ncc.isVisible()) {
                if (mc.player.dimension == 0) {
                    coordz += ChatFormatting.GRAY + " [" + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX / 8.f) +
                            ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posZ / 8.f) +
                            ChatFormatting.GRAY + "]";
                }
                else if (mc.player.dimension == -1){ // Nether
                    coordz += ChatFormatting.GRAY + " [" + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX * 8.f) +
                            ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posZ * 8.f) +
                            ChatFormatting.GRAY + "]";
                }else{
                    // End
                }
            }
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(coordz));

            Harakiri.get().getTTFFontUtil().drawStringWithShadow(coordz, this.getX(), this.getY(), -1);
        } else {
            this.setW(Harakiri.get().getTTFFontUtil().getStringWidth("(coordinates)"));
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("(coordinates)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}