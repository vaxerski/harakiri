package me.vaxry.harakiri.impl.gui.screen;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.module.hidden.CommandsModule;
import me.vaxry.harakiri.impl.module.render.HudModule;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class HaraGuiChat extends GuiChat {
    private float openPerc = 0;
    private Timer timer = new Timer();

    public HaraGuiChat(){
        super();
        openPerc = 0F;
        timer.reset();
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = 100F;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, desiredTimePerSecond);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final CommandsModule commandsModule = (CommandsModule) Harakiri.get().getModuleManager().find(CommandsModule.class);
        final HudEditorModule hem = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);

        openPerc = (float)MathUtil.parabolic(openPerc, 100F, MathUtil.TIME_TO_INCLINE / getJitter());
        if(openPerc >= 98F)
            openPerc = 100F;

        if(this.inputField.getText().length() >= commandsModule.prefix.getValue().length()) {
            if (this.inputField.getText().substring(0, commandsModule.prefix.getValue().length()).equals(commandsModule.prefix.getValue())) {
                // We are inputting a command!
                final int color = ((HudModule) Harakiri.get().getModuleManager().find(HudModule.class)).rainbow.getValue() ? Harakiri.get().getHudManager().rainbowColor :
                        hem.color.getValue().getRGB() + 0xFF000000;
                RenderUtil.drawLine(2, this.height - 14, this.width - 2, this.height - 14, 1F, color);
                RenderUtil.drawLine(2, this.height - 2, this.width - 2, this.height - 2, 1F, color);
                RenderUtil.drawLine(2, this.height - 2, 2, this.height - 14, 1F, color);
                RenderUtil.drawLine(this.width - 2, this.height - 2, this.width - 2, this.height - 14, 1F, color);
            }
        }

        drawRect(2, (int)(this.height - (12 * (openPerc / 100F)) - 2), this.width - 2, this.height - 2, Integer.MIN_VALUE);

        this.inputField.drawTextBox();
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if(this.inputField.getText().equalsIgnoreCase("")){
            this.fontRenderer.drawStringWithShadow(ChatFormatting.GRAY + "" + ChatFormatting.ITALIC + "Enter a command (Prefix: " + ChatFormatting.GREEN + commandsModule.prefix.getValue() + ChatFormatting.GRAY + " ), or a chat message...", (float)4, this.height - 12, 0xFFFFFFFF);
        }

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null)
        {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }
    }
}
