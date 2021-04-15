package me.vaxry.harakiri.impl.gui.menu;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldServerDemo;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.client.GuiModList;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.IOException;
import java.util.ArrayList;

public class HaraMainMenu extends GuiMainMenu {
    private Texture backgroundTex;
    private Texture backgroundTexLogo;
    private ArrayList<HaraMainMenuButton> mainMenuButtons = new ArrayList<>();
    private boolean first = true;

    private float lastButX = 0;
    private final float Y_OFFSET = 0;

    public HaraMainMenu(){
        Harakiri.get().getEventManager().addEventListener(this);
    }

    @Listener
    public void displayGui(EventDisplayGui event){
        if (event.getScreen() == null && mc.world == null) {
            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(this);
        }

        if (Minecraft.getMinecraft().currentScreen instanceof HaraMainMenu && event.getScreen() == null)
            event.setCanceled(true);

        if (event.getScreen() != null)
            if (event.getScreen() instanceof GuiMainMenu && !(event.getScreen() instanceof HaraMainMenu)) {
                event.setCanceled(true);
                Minecraft.getMinecraft().displayGuiScreen(this);
            }

        if(Harakiri.get().getUsername().equalsIgnoreCase("")){
            // debug
            if(event.getScreen() instanceof GuiMultiplayer)
                Harakiri.get().getApiManager().debugDie();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        backgroundTex       = new Texture("haramenu.png");
        backgroundTexLogo   = new Texture("haramainlogo.png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(Harakiri.get().getUsername().equalsIgnoreCase("")){
            // debug
            disableMultiplayer();
        }

        if(first){
            this.mainMenuButtons.clear();
            float delaySeconds = 0.25F;
            for(GuiButton button : this.buttonList){
                this.mainMenuButtons.add(new HaraMainMenuButton(button.id, button.x, (int)(button.y + Y_OFFSET), button.width, button.height, button.displayString, delaySeconds));
                delaySeconds += 0.25F;
            }
            first = false;
        }

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        this.backgroundTex.bind();
        this.backgroundTex.render(0,0, res.getScaledWidth(), res.getScaledHeight());

        this.backgroundTexLogo.bind();
        this.backgroundTexLogo.render(0,0, res.getScaledWidth(), res.getScaledHeight());

        for(HaraMainMenuButton button : this.mainMenuButtons){
            button.drawButton(mc, mouseX, mouseY, partialTicks);
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        Harakiri.get().getTTFFontUtil().drawStringWithShadow("Do not distribute", 0, res.getScaledHeight() -
                2*Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 2, 0xFFFFFFFF);
        Harakiri.get().getTTFFontUtil().drawStringWithShadow("Copyright Mojang AB", 0, res.getScaledHeight()
                - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 1, 0xFFFFFFFF);

        Harakiri.get().getTTFFontUtil().drawStringWithShadow("Harakiri v" + harakiriMod.VERSION, res.getScaledWidth() -
                Harakiri.get().getTTFFontUtil().getStringWidth("Harakiri v" + harakiriMod.VERSION) - 2,
                res.getScaledHeight() - Harakiri.get().getTTFFontUtil().FONT_HEIGHT - 1,
                0xFFFFFFFF);

        Harakiri.get().getTTFFontUtil().drawStringWithShadow("Logged in as " + Harakiri.get().getUsername(),
                res.getScaledWidth() - Harakiri.get().getTTFFontUtil().getStringWidth("Logged in as " + Harakiri.get().getUsername()) - 2,
                0,
                0xFFFFFFFF);

        if(!Harakiri.get().getLoggedAccount().equalsIgnoreCase(""))
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("Selected Account: " + ChatFormatting.GREEN + Harakiri.get().getLoggedAccount(),
                res.getScaledWidth() - Harakiri.get().getTTFFontUtil().getStringWidth("Selected Account: " + ChatFormatting.GREEN + Harakiri.get().getLoggedAccount()) - 2,
                Harakiri.get().getTTFFontUtil().FONT_HEIGHT,
                0xFFFFFFFF);

        Harakiri.get().getTTFFontUtil().drawStringWithShadow("Account Manager",
                0,
                0,
                0xFFFFFFFF);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        // Reload main menu buttonz
        if(needsAnUpdate()) {
            this.mainMenuButtons.clear();
            for (GuiButton button : this.buttonList) {
                this.mainMenuButtons.add(new HaraMainMenuButton(button.id, button.x, (int)(button.y + Y_OFFSET), button.width, button.height, button.displayString, -1F /*Fadein is 1s, skip it*/));
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if(mouseX > 0 &&
            mouseX < Harakiri.get().getTTFFontUtil().getStringWidth("Account Manager") * 1.5f &&
            mouseY > 0 &&
            mouseY < Harakiri.get().getTTFFontUtil().FONT_HEIGHT * 1.5f){
            // Clicked the manager

            mc.displayGuiScreen(new HaraAccountManager(this));
        }

    }

    private boolean needsAnUpdate(){
        if(lastButX != this.buttonList.get(0).x){
            lastButX = this.buttonList.get(0).x;
            return true;
        }
        return false;
    }

    private void disableMultiplayer(){
        for(GuiButton button : this.buttonList) {
            if(button.id == 2){
                // Multiplayer
                button.displayString = "Debug Mode: Multiplayer Disabled.";
            }
        }

        for(HaraMainMenuButton button : this.mainMenuButtons){
            if(button.id == 2){
                // Multiplayer
                button.displayString = "Debug Mode: Multiplayer Disabled.";
            }
        }
    }

}
