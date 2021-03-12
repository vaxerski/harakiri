package me.vaxry.harakiri.impl.gui.menu;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.module.hidden.ReconnectModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.SoundEvents;

public class AutoReconnectButton extends GuiButton {

    private ReconnectModule reconnectModule;
    private Timer timer = new Timer();

    public AutoReconnectButton(int buttonId, int x, int y, String buttonText)
    {
        super(buttonId, x, y, buttonText);

        reconnectModule = (ReconnectModule) Harakiri.get().getModuleManager().find(ReconnectModule.class);

        this.timer.reset();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        super.drawButton(mc, mouseX, mouseY, partialTicks);

        if (visible)
        {
            if(reconnectModule.auto.getValue()){
                final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;
                if(seconds < 0.5f)
                    return;
                final float recAm = reconnectModule.delay.getValue()/1000.f;
                this.displayString = "Mechanically Re-Entering (" + ((int)recAm - (int)seconds) + "s)";
                if(this.timer.passed(recAm * 1000.f)){
                    if(!reconnectModule.reconnect())
                    {
                        this.displayString = "Mechanical Re-Entering failed.";
                    }
                }
            }else{
                this.displayString = "Mechanical Re-Entering Disabled.";
            }

        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        reconnectModule.auto.setValue(!reconnectModule.auto.getValue());
        this.timer.reset();
    }
}
