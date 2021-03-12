package me.vaxry.harakiri.impl.gui.menu;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.impl.module.hidden.ReconnectModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.SoundEvents;

public class ReconnectButton extends GuiButton {

    private ReconnectModule reconnectModule;

    public ReconnectButton(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);

        reconnectModule = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);

        if (visible)
        {
            this.displayString = "Re-Enter thy Stage";
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        reconnectModule.reconnect();
    }
}
