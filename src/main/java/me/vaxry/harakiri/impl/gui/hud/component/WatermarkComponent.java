package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.HudComponent;
import me.vaxry.harakiri.framework.texture.Texture;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import me.vaxry.harakiri.impl.module.ui.WatermarkModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Author Seth
 * 7/25/2019 @ 4:55 AM.
 */
public final class WatermarkComponent extends HudComponent {

    private final String WATERMARK = ChatFormatting.BOLD + "Harakiri | " + ChatFormatting.LIGHT_PURPLE + harakiriMod.VERSION;

    protected Texture watermarkTex;

    private final float OFFSET_X = 2;
    private final float OFFSET_Y = 2;

    public WatermarkComponent() {
        super("Watermark");
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        this.setH(res.getScaledHeight());
        this.setW(res.getScaledWidth());
        this.setX(0);
        this.setY(0);
        //watermarkTex = new Texture("harawatermark.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        // Fid hud editor
        this.setX(0);
        this.setY(0);
        this.setW(0);
        this.setH(0);

        if((Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor))
            return;

        if(Minecraft.getMinecraft().world == null)
            return;

        WatermarkModule watermarkModule = (WatermarkModule) Harakiri.get().getModuleManager().find(WatermarkModule.class);
        watermarkModule.setWMOnState(this.isVisible());

        final NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().player.connection.getPlayerInfo(Minecraft.getMinecraft().player.getUniqueID());
        String ping = "";
        if (Objects.nonNull(playerInfo)) {
            final String ms = playerInfo.getResponseTime() != 0 ? playerInfo.getResponseTime() + "ms" : "?";
            ping = ms;
        }

        final HudModule hudModule = (HudModule)Harakiri.get().getModuleManager().find(HudModule.class);
        final boolean useRainbow = hudModule.rainbow.getValue();

        final HudEditorModule hem = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);

        final String time = new SimpleDateFormat("h:mm a").format(new Date());

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        final String watermarkText = "HARAKIRI v" + harakiriMod.VERSION + " | "
                + Minecraft.getMinecraft().player.getGameProfile().getName()
                + " (" + Harakiri.get().getUsername() + ") | "
                + (Minecraft.getMinecraft().isSingleplayer() ? "singleplayer" : Minecraft.getMinecraft().getCurrentServerData().serverIP) + " | "
                + "ping: " + ping + " | "
                + time;

        float SCALE = 0.8f;

        GlStateManager.scale(SCALE, SCALE, SCALE);

        // Back
        RenderUtil.drawRoundedRect(OFFSET_X, OFFSET_Y, Harakiri.get().getTTFFontUtil().getStringWidth(watermarkText) + 5, Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 4, 2, 0xFF444444);
        // Top Box
        RenderUtil.drawRoundedRect(OFFSET_X, OFFSET_Y, Harakiri.get().getTTFFontUtil().getStringWidth(watermarkText) + 5, 1, 0.4f, useRainbow ? Harakiri.get().getHudManager().rainbowColor : 0xFF000000 + hem.color.getValue().getRGB());
        GlStateManager.scale(1f/SCALE, 1f/SCALE, 1f/SCALE);

        // Text
        Harakiri.get().getTTFFontUtil().drawStringScaled(watermarkText, (int)((OFFSET_X + 2) * SCALE), (int)((OFFSET_Y + 2) * SCALE), 0xFFDDDDDD, SCALE);


    }
}
