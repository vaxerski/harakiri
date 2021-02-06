package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import net.minecraft.client.Minecraft;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 7/27/2019 @ 7:44 PM.
 */
public final class CoordsComponent extends DraggableHudComponent {

    public CoordsComponent() {
        super("Coords");
        this.setH(Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player != null && mc.world != null) {
            final DecimalFormat df = new DecimalFormat("#.#");

            String coordz = ChatFormatting.GRAY + "XYZ: " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                    df.format(Minecraft.getMinecraft().player.posY) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posZ);

            NetherCoordsComponent ncc = (NetherCoordsComponent)Harakiri.INSTANCE.getHudManager().findComponent(NetherCoordsComponent.class);
            if(ncc.isVisible()) {
                if (mc.player.dimension == 0) {
                    coordz += ChatFormatting.GRAY + " [" + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX / 8.f) +
                            ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posY / 8.f) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posZ / 8.f) +
                            ChatFormatting.GRAY + "]";
                }
                else if (mc.player.dimension == -1){ // Nether
                    coordz += ChatFormatting.GRAY + " [" + ChatFormatting.RESET + df.format(Minecraft.getMinecraft().player.posX * 8.f) +
                            ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posY * 8.f) + ChatFormatting.GRAY + ", " + ChatFormatting.RESET +
                            df.format(Minecraft.getMinecraft().player.posZ * 8.f) +
                            ChatFormatting.GRAY + "]";
                }else{
                    // End
                }
            }
            this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth(coordz));

            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(coordz, this.getX(), this.getY(), -1);
        } else {
            this.setW(Minecraft.getMinecraft().fontRenderer.getStringWidth("(coordinates)"));
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("(coordinates)", this.getX(), this.getY(), 0xFFAAAAAA);
        }
    }

}