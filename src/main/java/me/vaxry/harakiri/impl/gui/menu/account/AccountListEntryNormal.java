package me.vaxry.harakiri.impl.gui.menu.account;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import me.vaxry.harakiri.impl.gui.menu.HaraAccountManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class AccountListEntryNormal implements GuiListExtended.IGuiListEntry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final HaraAccountManager owner;
    private final Minecraft mc;
    private final Account account;
    private final ResourceLocation serverIcon = null;
    private String lastIconB64;
    private DynamicTexture icon;
    private long lastClickTime;

    protected AccountListEntryNormal(HaraAccountManager ownerIn, Account serverIn) {
        this.owner = ownerIn;
        this.account = serverIn;
        this.mc = Minecraft.getMinecraft();
        //this.serverIcon = new ResourceLocation("servers/" + serverIn.serverIP + "/icon");
        //this.icon = (DynamicTexture)this.mc.getTextureManager().getTexture(this.serverIcon);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        //JOptionPane.showMessageDialog(null, "drawn", "E", JOptionPane.INFORMATION_MESSAGE);
        this.mc.fontRenderer.drawString(this.account.name, x + 32 + 3, y + 1, 16777215);

        this.mc.fontRenderer.drawString(this.account.email, x + 32 + 3, y + 12, 8421504);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.mc.gameSettings.touchscreen || isSelected) {
            this.mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;
            /*if (this.canJoin()) {
                if (k1 < 32 && k1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }*/

            if (this.owner.canMoveUp(this, slotIndex)) {
                if (k1 < 16 && l1 < 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (this.owner.canMoveDown(this, slotIndex)) {
                if (k1 < 16 && l1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
        }

    }

    protected void drawTextureAt(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_) {
        this.mc.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    private boolean canJoin() {
        return true;
    }

    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        if (relativeX <= 32) {
            if (relativeX < 32 && relativeX > 16 && this.canJoin()) {
                this.owner.selectServer(slotIndex);
                this.owner.connectToSelected();
                return true;
            }

            if (relativeX < 16 && relativeY < 16 && this.owner.canMoveUp(this, slotIndex)) {
                this.owner.moveServerUp(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }

            if (relativeX < 16 && relativeY > 16 && this.owner.canMoveDown(this, slotIndex)) {
                this.owner.moveServerDown(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }
        }

        this.owner.selectServer(slotIndex);
        if (Minecraft.getSystemTime() - this.lastClickTime < 250L) {
            this.owner.connectToSelected();
        }

        this.lastClickTime = Minecraft.getSystemTime();
        return false;
    }

    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
    }

    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    public Account getServerData() {
        return this.account;
    }
}
