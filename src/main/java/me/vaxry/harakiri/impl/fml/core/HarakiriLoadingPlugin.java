package me.vaxry.harakiri.impl.fml.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import javax.swing.*;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import org.lwjgl.opengl.*;

@IFMLLoadingPlugin.TransformerExclusions(value = "me.vaxry.harakiri.impl.fml.core")
@IFMLLoadingPlugin.Name(value = "Harakiri")
@IFMLLoadingPlugin.MCVersion(value = "1.12.2")
public final class HarakiriLoadingPlugin implements IFMLLoadingPlugin {

    public HarakiriLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.harakiri.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return HarakiriAccessTransformer.class.getName();
    }

    //
    // LEFT UNUSED - DIDNT WORK WELL
    // Animated loading screen
    //

    public static Drawable d;
    public static HaraLoadingThread thread;
    public static boolean terminate = false;

    public static class HaraLoadingThread extends Thread {
        float r = 1;
        boolean down = false;
        private static final Lock lock = new ReentrantLock(true);
        private ResourceLocation rl = new ResourceLocation("harakirimod", "loadingsplash.png");
        private int name = 0;
        private static final Semaphore mutex = new Semaphore(1);

        private void setGL()
        {
            lock.lock();
            try
            {
                Display.getDrawable().makeCurrent();
            }
            catch (LWJGLException e)
            {
                throw new RuntimeException(e);
            }
            glClearColor(0F,0F,0F,1F);
            glDisable(GL_LIGHTING);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        private void setColor(int color)
        {
            glColor3ub((byte)((color >> 16) & 0xFF), (byte)((color >> 8) & 0xFF), (byte)(color & 0xFF));
        }

        private void clearGL()
        {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayWidth = Display.getWidth();
            mc.displayHeight = Display.getHeight();
            mc.resize(mc.displayWidth, mc.displayHeight);
            glClearColor(1, 1, 1, 1);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, .1f);
            try
            {
                Display.getDrawable().releaseContext();
            }
            catch (LWJGLException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                lock.unlock();
            }
        }

        public float getU(int frame, float u)
        {
            return Display.getWidth() * (frame % (Display.getWidth() / Display.getWidth()) + u) / Display.getWidth();
        }

        public float getV(int frame, float v)
        {
            return Display.getHeight() * (frame / (Display.getHeight() / Display.getHeight()) + v) / Display.getHeight();
        }

        public void run() {
            try {
                setGL();

                // Create tex
                glEnable(GL_TEXTURE_2D);
                synchronized(HaraLoadingThread.class)
                {
                    name = glGenTextures();
                    glBindTexture(GL_TEXTURE_2D, name);
                }
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Display.getWidth(), Display.getHeight(), 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer)null);

                while (!(Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu)) {
                    if(terminate)
                        break;

                    ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
                    int i = scaledresolution.getScaleFactor();
                    Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
                    framebuffer.bindFramebuffer(false);
                    GlStateManager.matrixMode(5889);
                    GlStateManager.loadIdentity();
                    GlStateManager.ortho(0.0D, (double)scaledresolution.getScaledWidth(), (double)scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
                    GlStateManager.matrixMode(5888);
                    GlStateManager.loadIdentity();
                    GlStateManager.translate(0.0F, 0.0F, -2000.0F);
                    GlStateManager.disableLighting();
                    GlStateManager.disableFog();
                    GlStateManager.disableDepth();
                    GlStateManager.enableTexture2D();

                    try {
                        GlStateManager.color(r, r, r, 1F);
                        if (down) {
                            if (r <= 0.05f) {
                                down = false;
                            }

                            r -= 0.02f;
                        } else {
                            if (r >= 0.95f) {
                                down = true;
                            }

                            r += 0.02f;
                        }

                        ResourceLocation rl = new ResourceLocation("harakirimod", "textures/loadingsplash.png");
                        Minecraft.getMinecraft().getTextureManager().bindTexture(rl);
                        Minecraft.getMinecraft().getTextureManager().bindTexture(rl);

                        final int x = 0;
                        final int y = 0;
                        final int width = scaledresolution.getScaledWidth();
                        final int height = scaledresolution.getScaledHeight();
                        final int u = 0;
                        final int v = 0;
                        final int t = 1;
                        final int s = 1;

                        mutex.acquireUninterruptibly();

                        glEnable(GL_TEXTURE_2D);
                        glBindTexture(GL_TEXTURE_2D, name);
                        glBegin(GL_QUADS);
                        glTexCoord2f(getU(0, 0), getV(0, 0));
                        //haraTex.texCoord(0, 0, 0);
                        glVertex2f(320 - 256, 240 - 256);
                        glTexCoord2f(getU(0, 0), getV(0, 1));
                        glVertex2f(320 - 256, 240 + 256);
                        glTexCoord2f(getU(0, 1), getV(0, 1));
                        glVertex2f(320 + 256, 240 + 256);
                        glTexCoord2f(getU(0, 1), getV(0, 0));
                        glVertex2f(320 + 256, 240 - 256);
                        glEnd();
                        glDisable(GL_TEXTURE_2D);

                        GlStateManager.disableLighting();
                        GlStateManager.disableFog();
                        framebuffer.unbindFramebuffer();
                        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
                        GlStateManager.enableAlpha();
                        GlStateManager.alphaFunc(516, 0.1F);


                        Display.update();
                        mutex.release();

                    } catch (Throwable t) {
                        // If anything happens.
                        JOptionPane.showMessageDialog(null, t.toString(), "Error in drawSplashScreen 1!", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }catch (Throwable t) {
                // If anything happens.
                JOptionPane.showMessageDialog(null, t.toString(), "Error in drawSplashScreen 2!", JOptionPane.INFORMATION_MESSAGE);
            }

            try {
                Display.update();
                mutex.release();
                clearGL();
                Minecraft.getMinecraft().currentScreen.updateScreen();
            }catch (Throwable t){ ; }
        }
    }
}
