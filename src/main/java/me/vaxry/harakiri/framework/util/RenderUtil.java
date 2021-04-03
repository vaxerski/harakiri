package me.vaxry.harakiri.framework.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public final class RenderUtil {

    private static final IntBuffer VIEWPORT = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);

    public static void updateModelViewProjectionMatrix() {
        glGetFloat(GL_MODELVIEW_MATRIX, MODELVIEW);
        glGetFloat(GL_PROJECTION_MATRIX, PROJECTION);
        glGetInteger(GL_VIEWPORT, VIEWPORT);
        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
        GLUProjection.getInstance().updateMatrices(VIEWPORT, MODELVIEW, PROJECTION, (float) res.getScaledWidth() / (float) Minecraft.getMinecraft().displayWidth, (float) res.getScaledHeight() / (float) Minecraft.getMinecraft().displayHeight);
    }

    public static void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, h, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, h, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(w, y, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
    }

    public static void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        glColor(color);

        GL11.glBegin(GL11.GL_POLYGON);
        int SIDES = 32; // 32 sides
        int SIDES34 = 8 * 3;


        // Bottom Line
        GL11.glVertex2d(x + radius, y + h);
        GL11.glVertex2d(x + w - radius, y + h);

        float x1 = x + w - radius;
        float y1 = y + h - radius;

        for (int i = 0; i <= SIDES / 4; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        // Right Line
        GL11.glVertex2d(x + w, y + radius);

        x1 = x + w - radius;
        y1 = y + radius;

        for (int i = SIDES / 4; i <= SIDES / 2; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        // Top Line
        GL11.glVertex2d(x + radius, y);

        x1 = x + radius;
        y1 = y + radius;

        for (int i = SIDES / 2; i <= SIDES34; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        // Left Line
        GL11.glVertex2d(x, y + h - radius);

        x1 = x + radius;
        y1 = y + h - radius;

        for (int i = SIDES34; i <= SIDES; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        GL11.glEnd();

        glColor(0xFFFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawRoundedRectTop(float x, float y, float w, float h, float radius, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        glColor(color);

        GL11.glBegin(GL11.GL_POLYGON);
        int SIDES = 32; // 32 sides
        int SIDES34 = 8 * 3;


        // Bottom Line
        GL11.glVertex2d(x, y + h);
        GL11.glVertex2d(x + w, y + h);

        float x1;
        float y1;

        // Right Line
        GL11.glVertex2d(x + w, y + radius);

        x1 = x + w - radius;
        y1 = y + radius;

        for (int i = SIDES / 4; i <= SIDES / 2; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        // Top Line
        GL11.glVertex2d(x + radius, y);

        x1 = x + radius;
        y1 = y + radius;

        for (int i = SIDES / 2; i <= SIDES34; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        // Left Line
        GL11.glVertex2d(x, y + h);

        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    //todo: fix this
    public static void drawResizeAnchor(float x, float y, float w, float h, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        glColor(color);

        GL11.glBegin(GL11.GL_POLYGON);

        int SIDES = 32;
        float radius = 0.5f * w;

        GL11.glVertex2d(x, y + h);
        GL11.glVertex2d(x + w - radius, y + h);

        float x1 = x + w - radius;
        float y1 = y + h - radius;

        for (int i = 0; i <= SIDES / 4; i++)
        {
            GL11.glVertex2d(x1 + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y1 + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }

        GL11.glVertex2d(x + w, y);
        GL11.glVertex2d(x, y + h);

        GL11.glEnd();

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectLeftRight(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawTriangle(float x, float y, float size, float theta, int color) {
        GL11.glTranslated(x, y, 0);
        GL11.glRotatef(180 + theta, 0F, 0F, 1.0F);

        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glVertex2d(0, (1F * size));
        GL11.glVertex2d((1 * size), -(1F * size));
        GL11.glVertex2d(-(1 * size), -(1F * size));

        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glRotatef(-180 - theta, 0F, 0F, 1.0F);
        GL11.glTranslated(-x, -y, 0);
        GL11.glColor4f(1,1,1,1);
    }

    public static void drawHueQuad(float left, float top, float right, float bottom, int color) {
        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 0).color(1F, 1F, 1F, 1F).endVertex();
        bufferbuilder.pos(left, bottom, 0).color(0F,0F,0F,1F).endVertex();
        bufferbuilder.pos(right, bottom, 0).color(0F,0F,0F,1F).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawOutlineRect(float x, float y, float w, float h, float lineWidth, int c) {
        drawRect(x, y, x - lineWidth, h, c);
        drawRect(w + lineWidth, y, w, h, c);
        drawRect(x, y, w, y - lineWidth, c);
        drawRect(x, h + lineWidth, w, h, c);
    }

    public static void drawBorderedRect(float x, float y, float x1, float y1, float lineWidth, int insideColor, int borderColor) {
        drawRect(x + lineWidth, y + lineWidth, x1 - lineWidth, y1 - lineWidth, insideColor);
        drawRect(x + lineWidth, y, x1 - lineWidth, y + lineWidth, borderColor);
        drawRect(x, y, x + lineWidth, y1, borderColor);
        drawRect(x1 - lineWidth, y, x1, y1, borderColor);
        drawRect(x + lineWidth, y1 - lineWidth, x1 - lineWidth, y1, borderColor);
    }

    public static void drawBorderedRectBlurred(float x, float y, float x1, float y1, float lineWidth, int insideColor, int borderColor) {
        drawRect(x, y, x1, y1, insideColor);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(lineWidth);
        glColor(borderColor);
        GL11.glBegin(3);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);
        glLineWidth(thickness);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawOutlinePolygon(Geometry geometry, float thickness, int hex, boolean rainbow, float hue) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        int lastbegin = 0;

        Coordinate[] coordinates = geometry.getCoordinates();

        Coordinate startCoord = coordinates[0];

        double geometryLength = geometry.getLength();
        double elapsedLength = 0;

        int i = 0;
        for(;;){
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.shadeModel(GL_SMOOTH);
            glLineWidth(thickness);
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferbuilder = tessellator.getBuffer();

            bufferbuilder.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

            for(; i < coordinates.length; ++i){

                float huehue = hue + (float)elapsedLength / (float)geometryLength;
                if(huehue > 1)
                    huehue -= 1;

                Color rainbowColor = Color.getHSBColor(huehue, 1, 1);

                if(i > 0)
                    elapsedLength += Math.sqrt(Math.pow(coordinates[i - 1].x - coordinates[i].x, 2) + Math.pow(coordinates[i - 1].y - coordinates[i].y, 2));

                // because some have holes.
                if(i > lastbegin + 1){
                    if(coordinates[i - 1].x == startCoord.x && coordinates[i - 1].y == startCoord.y){
                        // We are back at the start. If there is more to draw, dont draw THIS line
                        // and set the new start to the new start
                        startCoord = coordinates[i];
                        lastbegin = i;
                        break;
                    }
                }
                if(rainbow)
                    bufferbuilder.pos(coordinates[i].x, coordinates[i].y, 0).color(rainbowColor.getRed() / 255.f, rainbowColor.getGreen() / 255.f, rainbowColor.getBlue() / 255.f, alpha).endVertex();
                else
                    bufferbuilder.pos(coordinates[i].x, coordinates[i].y, 0).color(red, green, blue, alpha).endVertex();
            }

            tessellator.draw();
            GlStateManager.shadeModel(GL_FLAT);
            glDisable(GL_LINE_SMOOTH);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.enableTexture2D();

            if(i >= coordinates.length)
                break;
        }
    }

    public static void drawOutlinePolygon3D(ArrayList<Coordinate> geometry3D, float thickness, int hex, boolean rainbow, float hue) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        int lastbegin = 0;

        Coordinate startCoord = geometry3D.get(0);

        double geometryLength = geometry3D.size();
        double elapsedLength = 0;

        int i = 0;
        for(;;){
            glLineWidth(thickness);
            glEnable(GL32.GL_DEPTH_CLAMP);
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            for(; i < geometry3D.size(); ++i){

                float huehue = hue + (float)elapsedLength / (float)geometryLength;
                if(huehue > 1)
                    huehue -= 1;

                Color rainbowColor = Color.getHSBColor(huehue, 1, 1);

                if(i > 0)
                    elapsedLength += Math.sqrt(Math.pow(geometry3D.get(i - 1).x - geometry3D.get(i).x, 2) + Math.pow(geometry3D.get(i - 1).y - geometry3D.get(i).y, 2));

                // because some have holes.
                if(i > lastbegin + 1){
                    if(geometry3D.get(i - 1).x == startCoord.x && geometry3D.get(i - 1).y == startCoord.y){
                        // We are back at the start. If there is more to draw, dont draw THIS line
                        // and set the new start to the new start
                        startCoord = geometry3D.get(i);
                        lastbegin = i;
                        break;
                    }
                }
                if(rainbow)
                    bufferbuilder.pos(geometry3D.get(i).x, geometry3D.get(i).y, geometry3D.get(i).z).color(rainbowColor.getRed() / 255.f, rainbowColor.getGreen() / 255.f, rainbowColor.getBlue() / 255.f, alpha).endVertex();
                else
                    bufferbuilder.pos(geometry3D.get(i).x, geometry3D.get(i).y, geometry3D.get(i).z).color(red, green, blue, alpha).endVertex();
            }
            tessellator.draw();
            glDisable(GL32.GL_DEPTH_CLAMP);

            if(i >= geometry3D.size())
                break;
        }
    }

    public static void drawLine2(float x, float y, float x1, float y1, float thickness, int hex, int hex1) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        float red1 = (hex1 >> 16 & 0xFF) / 255.0F;
        float green1 = (hex1 >> 8 & 0xFF) / 255.0F;
        float blue1 = (hex1 & 0xFF) / 255.0F;
        float alpha1 = (hex1 >> 24 & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);
        glLineWidth(1);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0).color(red1, green1, blue1, alpha1).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawThinLine(float x, float y, float x1, float y1, int hex) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);
        glLineWidth(1);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, 0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawLine3D(double x, double y, double z, double x1, double y1, double z1, float thickness, int hex) {
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        float alpha = (hex >> 24 & 0xFF) / 255.0F;

        glLineWidth(thickness);
        glEnable(GL32.GL_DEPTH_CLAMP);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        glDisable(GL32.GL_DEPTH_CLAMP);
    }

    public static void drawBoundingBoxFixed(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {

        // Normalize Colors
        red     /= 255.0f;
        green   /= 255.0f;
        blue    /= 255.0f;
        alpha   /= 255.0f;

        glLineWidth(width);
        glEnable(GL32.GL_DEPTH_CLAMP);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        // Front Face
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.maxX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        // Back Face
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.maxX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        // Side Connections
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.maxX,bb.minY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.minY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        bufferbuilder.reset();
        bufferbuilder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX,bb.maxY,bb.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        glDisable(GL32.GL_DEPTH_CLAMP);
    }

    public static void drawBoundingBox(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        glLineWidth(width);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB bb, float width, int color) {
        final float alpha = (color >> 24 & 0xFF) / 255.0F;
        final float red = (color >> 16 & 0xFF) / 255.0F;
        final float green = (color >> 8 & 0xFF) / 255.0F;
        final float blue = (color & 0xFF) / 255.0F;
        drawBoundingBox(bb, width, red, green, blue, alpha);
    }

    public static void drawFilledBox(double x, double y, double z, AxisAlignedBB bb, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        drawFilledBox(bb, color);
        GL11.glPopMatrix();
    }

    public static void drawBoundingBox(double x, double y, double z, AxisAlignedBB bb, float width, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        drawBoundingBox(bb, width, color);
        GL11.glPopMatrix();
    }

    public static void drawCrosses(double x, double y, double z, AxisAlignedBB bb, float width, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        drawCrosses(bb, width, color);
        GL11.glPopMatrix();
    }

    public static void drawPlane(double x, double y, double z, AxisAlignedBB bb, float width, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        drawPlane(bb, width, color);
        GL11.glPopMatrix();
    }

    public static void drawPlane(AxisAlignedBB axisalignedbb, float width, int color) {
        GlStateManager.glLineWidth(width);
        drawPlane(axisalignedbb, color);
    }

    public static void drawPlane(AxisAlignedBB boundingBox, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        double minX = boundingBox.minX;
        double minY = boundingBox.minY;
        double minZ = boundingBox.minZ;

        double maxX = boundingBox.maxX;
        double maxY = boundingBox.maxY;
        double maxZ = boundingBox.maxZ;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(minX, minY, maxZ).color(red, green, blue, 0).endVertex();
        bufferbuilder.pos(maxZ, minY, minZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();
    }

    public static void drawFilledBox(AxisAlignedBB bb, int color) {
        final float alpha = (color >> 24 & 0xFF) / 255.0F;
        final float red = (color >> 16 & 0xFF) / 255.0F;
        final float green = (color >> 8 & 0xFF) / 255.0F;
        final float blue = (color & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();

        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();

        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();

        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void drawCrosses(AxisAlignedBB bb, float width, int color) {
        GlStateManager.glLineWidth(width);
        glColor(color);
        GL11.glBegin(GL_LINES);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glEnd();
    }

    public static void drawCircle(float x, float y, int radius, int color)
    {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        glColor(color);

        GL11.glBegin(GL11.GL_POLYGON);
        int SIDES = 32; // 32 sides

        for (int i = 0; i <= SIDES; i++) // 32 sides
        {
            GL11.glVertex2d(x + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius));
        }
        GL11.glEnd();

        glColor(0xFFFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawCircleOutline(float x, float y, int radius, int thick, int color, int outlineColor){
        drawCircle(x, y, radius, outlineColor);
        drawCircle(x + thick, y + thick, radius - 2*thick, color);
    }

    public static void drawEmptyCircle(float x, float y, int radius, float thickness, int color)
    {
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = (color >> 24 & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL_SMOOTH);
        glLineWidth(thickness);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        int SIDES = 32; // 32 sides

        for (int i = 0; i <= SIDES; i++) // 32 sides
        {
            bufferbuilder.pos(x + (Math.sin((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), y + (Math.cos((i * (360.f / (float)SIDES) * 3.141526D / 180)) * radius), 0).color(red, green, blue, alpha);
        }

        tessellator.draw();
        GlStateManager.shadeModel(GL_FLAT);
        glDisable(GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void glScissor(float x, float y, float x1, float y1, final ScaledResolution sr) {
        GL11.glScissor((int) (x * sr.getScaleFactor()), (int) (Minecraft.getMinecraft().displayHeight - (y1 * sr.getScaleFactor())), (int) ((x1 - x) * sr.getScaleFactor()), (int) ((y1 - y) * sr.getScaleFactor()));
    }

    public static void glBillboard(float x, float y, float z) {
        float scale = 0.016666668f * 1.6f;
        GlStateManager.translate(x - Minecraft.getMinecraft().getRenderManager().renderPosX, y - Minecraft.getMinecraft().getRenderManager().renderPosY, z - Minecraft.getMinecraft().getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Minecraft.getMinecraft().player.rotationPitch, Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
        glBillboard(x, y, z);
        int distance = (int) player.getDistance(x, y, z);
        float scaleDistance = (distance / 2.0f) / (2.0f + (2.0f - scale));
        if (scaleDistance < 1f)
            scaleDistance = 1;
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }

    public static void drawTexture(float x, float y, float textureX, float textureY, float width, float height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, (y + height), 0.0D).tex((textureX * f), ((textureY + height) * f1)).endVertex();
        bufferbuilder.pos((x + width), (y + height), 0.0D).tex(((textureX + width) * f), ((textureY + height) * f1)).endVertex();
        bufferbuilder.pos((x + width), y, 0.0D).tex(((textureX + width) * f), (textureY * f1)).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex((textureX * f), (textureY * f1)).endVertex();
        tessellator.draw();
    }

    public static void drawTexture(float x, float y, float width, float height, float u, float v, float t, float s) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x + width, y, 0F).tex(t, v).endVertex();
        bufferbuilder.pos(x, y, 0F).tex(u, v).endVertex();
        bufferbuilder.pos(x, y + height, 0F).tex(u, s).endVertex();
        bufferbuilder.pos(x, y + height, 0F).tex(u, s).endVertex();
        bufferbuilder.pos(x + width, y + height, 0F).tex(t, s).endVertex();
        bufferbuilder.pos(x + width, y, 0F).tex(t, v).endVertex();
        tessellator.draw();
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void glColor(int redRGB, int greenRGB, int blueRGB, int alphaRGB) {
        float red = 0.003921569F * redRGB;
        float green = 0.003921569F * greenRGB;
        float blue = 0.003921569F * blueRGB;
        float alpha = 0.003921569F * alphaRGB;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void begin2D() {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    }

    public static void end2D() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    public static void begin3D() {
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GL11.glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
    }

    public static void end3D() {
        GL11.glDisable(GL_LINE_SMOOTH);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
    }

    public static void renderOne(float width) {
        checkSetupFBO();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glClearStencil(0xF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public static void renderTwo() {
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }

    public static void renderThree() {
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    public static void renderFour() {
        setColor(new Color(255, 255, 255));
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(1.0F, -2000000F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive() {
        GL11.glPolygonOffset(1.0F, 2000000F);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();
    }

    public static void setColor(Color c) {
        GL11.glColor4d(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public static void checkSetupFBO() {
        // Gets the FBO of Minecraft
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();

        // Check if FBO isn't null
        if (fbo != null) {
            // Checks if screen has been resized or new FBO has been created
            if (fbo.depthBuffer > -1) {
                // Sets up the FBO with depth and stencil extensions (24/8 bit)
                setupFBO(fbo);
                // Reset the ID to prevent multiple FBO's
                fbo.depthBuffer = -1;
            }
        }
    }

    /**
     * Sets up the FBO with depth and stencil
     *
     * @param fbo Framebuffer
     */
    public static void setupFBO(Framebuffer fbo) {
        // Deletes old render buffer extensions such as depth
        // Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        // Generates a new render buffer ID for the depth and stencil extension
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        // Binds new render buffer by ID
        // Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        // Adds the depth and stencil extension
        // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
        // Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        // Adds the stencil attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
        // Adds the depth attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID);
    }
}
