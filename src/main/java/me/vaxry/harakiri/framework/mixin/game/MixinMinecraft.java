package me.vaxry.harakiri.framework.mixin.game;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.minecraft.EventKeyPress;
import me.vaxry.harakiri.framework.event.minecraft.EventRunTick;
import me.vaxry.harakiri.framework.event.minecraft.EventUpdateFramebufferSize;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.duck.MixinMinecraftInterface;
import me.vaxry.harakiri.framework.util.GUIUtil;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.TTFFontUtil;
import me.vaxry.harakiri.impl.fml.core.HarakiriLoadingPlugin;
import me.vaxry.harakiri.impl.fml.harakiriMod;
import me.vaxry.harakiri.impl.gui.menu.HaraMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.SharedDrawable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.swing.*;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * @author cats
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements MixinMinecraftInterface {
    @Accessor(value = "timer")
    public abstract Timer getTimer();
    @Shadow
    GuiIngame ingameGUI;
    @Shadow
    WorldClient world;
    @Shadow
    EntityPlayerSP player;
    @Shadow
    GuiScreen currentScreen;
    @Shadow
    GameSettings gameSettings;
    @Shadow
    boolean skipRenderWorld;
    @Shadow
    SoundHandler soundHandler;

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo callbackInfo)
    {
        ingameGUI = new GUIUtil(Minecraft.getMinecraft());
    }

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info)
    {
        /*try {
            HarakiriLoadingPlugin.terminate = true;
            HarakiriLoadingPlugin.thread.join();
            GL11.glFlush();        // process any remaining GL calls before releaseContext (prevents missing textures on mac)
            HarakiriLoadingPlugin.d.releaseContext();
            Display.getDrawable().makeCurrent();
        }catch (Throwable t){ ; }*/

        if (guiScreenIn == null && this.world == null)
        {
            guiScreenIn = new HaraMainMenu();
        }
        else if (guiScreenIn == null && this.player.getHealth() <= 0.0F)
        {
            guiScreenIn = new GuiGameOver(null);
        }

        GuiScreen old = this.currentScreen;
        net.minecraftforge.client.event.GuiOpenEvent event = new net.minecraftforge.client.event.GuiOpenEvent(guiScreenIn);

        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            return;

        guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old)
        {
            old.onGuiClosed();
        }

        if (guiScreenIn instanceof HaraMainMenu || guiScreenIn instanceof GuiMultiplayer)
        {
            this.gameSettings.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages(true);
        }

        this.currentScreen = guiScreenIn;

        if(Harakiri.get().getUsername().equalsIgnoreCase(""))
            Harakiri.get().getApiManager().killThisThing(); // Anti crack, some sort of

        if (guiScreenIn != null)
        {
            Minecraft.getMinecraft().setIngameNotInFocus();
            KeyBinding.unPressAllKeys();

            while (Mouse.next())
            {
            }

            while (Keyboard.next())
            {
            }

            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            this.skipRenderWorld = false;
        }
        else
        {
            this.soundHandler.resumeSounds();
            Minecraft.getMinecraft().setIngameFocus();
        }

        final EventDisplayGui eventEE = new EventDisplayGui(guiScreenIn);
        Harakiri.get().getEventManager().dispatchEvent(eventEE);
        if (event.isCanceled()) info.cancel();

        info.cancel();
    }

    @Inject(method = "updateFramebufferSize", at = @At("HEAD"))
    private void onUpdateFramebufferSize(CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventUpdateFramebufferSize());
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTickPre(CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventRunTick(EventStageable.EventStage.PRE));
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void onRunTickPost(CallbackInfo ci) {
        Harakiri.get().getEventManager().dispatchEvent(new EventRunTick(EventStageable.EventStage.POST));
    }

    @Inject(method = "runTickKeyboard", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRunTickKeyboard(CallbackInfo ci, int i) {
        if (Keyboard.getEventKeyState()) {
            Harakiri.get().getEventManager().dispatchEvent(new EventKeyPress(i));
        }
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", cancellable = true, at = @At("HEAD"))
    private void onLoadWorld(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        final EventLoadWorld event = new EventLoadWorld(worldClientIn);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

//    @Inject(method = "drawSplashScreen", cancellable = true, at = @At("HEAD"))
//    public void drawSplashScreen(TextureManager textureManager, CallbackInfo ci){
//        // Loading screen
//        /*try {
//
//            try
//            {
//                HarakiriLoadingPlugin.d = new SharedDrawable(Display.getDrawable());
//                Display.getDrawable().releaseContext();
//                HarakiriLoadingPlugin.d.makeCurrent();
//            }
//            catch (LWJGLException e)
//            {
//
//            }
//
//            HarakiriLoadingPlugin.thread = new HarakiriLoadingPlugin.HaraLoadingThread();
//            HarakiriLoadingPlugin.thread.start();
//        }catch (Throwable t){
//            JOptionPane.showMessageDialog(null, t.toString(), "err!", JOptionPane.INFORMATION_MESSAGE);
//        }*/
//    // Animated. Didnt work well.
//
//        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
//        int i = scaledresolution.getScaleFactor();
//        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
//        framebuffer.bindFramebuffer(false);
//        GlStateManager.matrixMode(5889);
//        GlStateManager.loadIdentity();
//        GlStateManager.ortho(0.0D, (double)scaledresolution.getScaledWidth(), (double)scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
//        GlStateManager.matrixMode(5888);
//        GlStateManager.loadIdentity();
//        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
//        GlStateManager.disableLighting();
//        GlStateManager.disableFog();
//        GlStateManager.disableDepth();
//        GlStateManager.enableTexture2D();
//
//        try {
//            ResourceLocation rl = new ResourceLocation("harakirimod", "textures/loadingsplash.png");
//            Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
//            Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
//
//            final int x = 0;
//            final int y = 0;
//            final int width = scaledresolution.getScaledWidth();
//            final int height = scaledresolution.getScaledHeight();
//            final int u = 0;
//            final int v = 0;
//            final int t = 1;
//            final int s = 1;
//
//            final Tessellator tessellator = Tessellator.getInstance();
//            final BufferBuilder bufferbuilder = tessellator.getBuffer();
//            bufferbuilder.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
//            bufferbuilder.pos(x + width, y, 0F).tex(t, v).endVertex();
//            bufferbuilder.pos(x, y, 0F).tex(u, v).endVertex();
//            bufferbuilder.pos(x, y + height, 0F).tex(u, s).endVertex();
//            bufferbuilder.pos(x, y + height, 0F).tex(u, s).endVertex();
//            bufferbuilder.pos(x + width, y + height, 0F).tex(t, s).endVertex();
//            bufferbuilder.pos(x + width, y, 0F).tex(t, v).endVertex();
//            tessellator.draw();
//
//            final BufferBuilder bufferbuilder2 = Tessellator.getInstance().getBuffer();
//            bufferbuilder2.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
//            bufferbuilder2.pos(x + width, y, 0F).tex(t, v).endVertex();
//            bufferbuilder2.pos(x, y, 0F).tex(u, v).endVertex();
//            bufferbuilder2.pos(x, y + height, 0F).tex(u, s).endVertex();
//            bufferbuilder2.pos(x, y + height, 0F).tex(u, s).endVertex();
//            bufferbuilder2.pos(x + width, y + height, 0F).tex(t, s).endVertex();
//            bufferbuilder2.pos(x + width, y, 0F).tex(t, v).endVertex();
//            Tessellator.getInstance().draw();
//        }catch (Throwable t){
//            // If anything happens.
//            //JOptionPane.showMessageDialog(null, t.toString(), "Error in drawSplashScreen!", JOptionPane.INFORMATION_MESSAGE);
//        }
//        GlStateManager.disableLighting();
//        GlStateManager.disableFog();
//        framebuffer.unbindFramebuffer();
//        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
//        GlStateManager.enableAlpha();
//        GlStateManager.alphaFunc(516, 0.1F);
//        Minecraft.getMinecraft().updateDisplay();
//
//
//
//        ci.cancel();
//    }
}
