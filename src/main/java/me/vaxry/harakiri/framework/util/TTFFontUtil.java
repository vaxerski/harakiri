package me.vaxry.harakiri.framework.util;

/// Credit: https://github.com/HyperiumClient/Hyperium/blob/mcgradle/src/main/java/cc/hyperium/utils/HyperiumFontRenderer.java

import me.vaxry.harakiri.Harakiri;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class TTFFontUtil
{
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("\247[0123456789abcdefklmnor]");
    public final int FONT_HEIGHT = 9;
    private final int[] colorCodes =
            { 0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF };
    private final Map<String, Float> cachedStringWidth = new HashMap<>();
    private float antiAliasingFactor;
    private UnicodeFont unicodeFont;
    private int prevScaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    private String name;
    private float size;
    public boolean isTTF = false;

    private Timer cleanupTimer = new Timer();
    final private ArrayList<CachedTextString> cachedStringsArray = new ArrayList<>();

    private boolean isTTF(){
        return isTTF;
    }

    public TTFFontUtil(String fontName, float fontSize)
    {
        name = fontName;
        size = fontSize;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        try
        {
            prevScaleFactor = resolution.getScaleFactor();
            unicodeFont = new UnicodeFont(getFontByName(fontName).deriveFont(fontSize * prevScaleFactor / 2));
            unicodeFont.addAsciiGlyphs();
            unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
            unicodeFont.loadGlyphs();
        }
        catch (FontFormatException | IOException | SlickException e)
        {
            e.printStackTrace();

            prevScaleFactor = resolution.getScaleFactor();
            try
            {
                unicodeFont = new UnicodeFont(getFontByName("gravity").deriveFont(fontSize * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            } catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        this.cleanupTimer.reset();
        this.antiAliasingFactor = resolution.getScaleFactor();
    }

    public TTFFontUtil(Font font)
    {
        this(font.getFontName(), font.getSize());
    }

    public TTFFontUtil(String fontName, int fontType, int size)
    {
        this(new Font(fontName, fontType, size));
    }

    public static Font getFontByName(String name) throws IOException, FontFormatException
    {
        if (name.equalsIgnoreCase("gravity"))
            return getFontFromInput("/assets/harakirimod/fonts/GravityRegular.ttf");

        // Attempt to find custom fonts
        return null;
    }

    static Font font = null;

    public static Font getFontFromInput(String path) throws IOException, FontFormatException
    {
        Font newFont = Font.createFont(Font.TRUETYPE_FONT, TTFFontUtil.class.getResourceAsStream(path));

        if (newFont != null)
            font = newFont;

        return font;
    }

    public void drawStringScaled(String text, int givenX, int givenY, int color, double givenScale)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public int drawString(String text, float x, float y, int color)
    {
        if (text == null)
            return 0;

        if(!isTTF())
            return Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, false);

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        // idk fix shit
        y -= 1.5f;

        try
        {
            if (resolution.getScaleFactor() != prevScaleFactor)
            {
                prevScaleFactor = resolution.getScaleFactor();
                unicodeFont = new UnicodeFont(getFontByName(name).deriveFont(size * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            }
        }
        catch (FontFormatException | IOException | SlickException e)
        {
            e.printStackTrace();
        }

        this.antiAliasingFactor = resolution.getScaleFactor();

        GL11.glPushMatrix();
        GlStateManager.scale(1 / antiAliasingFactor, 1 / antiAliasingFactor, 1 / antiAliasingFactor);
        x *= antiAliasingFactor;
        y *= antiAliasingFactor;
        float originalX = x;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);

        int currentColor = color;

        char[] characters = text.toCharArray();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        String[] parts = COLOR_CODE_PATTERN.split(text);
        int index = 0;
        for (String s : parts)
        {
            for (String s2 : s.split("\n"))
            {
                for (String s3 : s2.split("\r"))
                {

                    unicodeFont.drawString(x, y, s3, new org.newdawn.slick.Color(currentColor));
                    x += unicodeFont.getWidth(s3);

                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r')
                    {
                        x = originalX;
                        index++;
                    }
                }
                if (index < characters.length && characters[index] == '\n')
                {
                    x = originalX;
                    y += getHeight(s2) * 2;
                    index++;
                }
            }
            if (index < characters.length)
            {
                char colorCode = characters[index];
                if (colorCode == '\247')
                {
                    char colorChar = characters[index + 1];
                    int codeIndex = ("0123456789" + "abcdef").indexOf(colorChar);
                    if (codeIndex < 0)
                    {
                        if (colorChar == 'r')
                        {
                            currentColor = color;
                        }
                    }
                    else
                    {
                        currentColor = colorCodes[codeIndex];
                    }
                    index += 2;
                }
            }
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.bindTexture(0);
        GlStateManager.popMatrix();
        return (int) getWidth(text);
    }

    public int drawStringCached(String text, float x, float y, int color)
    {
        if (text == null)
            return 0;

        if(!isTTF())
            return Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, false);

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        // check for cache
        final ScaledResolution res = resolution;

        CachedTextString cachedTextString = getCachedString(text);
        if(cachedTextString != null){
            final Framebuffer framebuffer = cachedTextString.framebuffer;

            GlStateManager.pushMatrix();

            GlStateManager.enableAlpha();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableBlend();

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            framebuffer.bindFramebufferTexture();

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderUtil.drawTexture(cachedTextString.x,
                    cachedTextString.y,
                    cachedTextString.w,
                    cachedTextString.h,
                    0,0,1,1);

            GlStateManager.disableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();

            GlStateManager.bindTexture(0);

            cachedTextString.lastUsed = System.currentTimeMillis();

            GlStateManager.popMatrix();

            return (int) getWidth(text);
        }

        // idk fix shit
        y -= 1.5f;

        try
        {
            if (resolution.getScaleFactor() != prevScaleFactor)
            {
                prevScaleFactor = resolution.getScaleFactor();
                unicodeFont = new UnicodeFont(getFontByName(name).deriveFont(size * prevScaleFactor / 2));
                unicodeFont.addAsciiGlyphs();
                unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
                unicodeFont.loadGlyphs();
            }
        }
        catch (FontFormatException | IOException | SlickException e)
        {
            e.printStackTrace();
        }

        this.antiAliasingFactor = resolution.getScaleFactor();

        float width = getWidth(text);

        Framebuffer framebuffer = new Framebuffer((int)width, FONT_HEIGHT, false);
        framebuffer.createFramebuffer((int)width, FONT_HEIGHT);

        //Framebuffer framebuffer = new Framebuffer((int)res.getScaledWidth() * res.getScaleFactor(), (int)res.getScaledHeight() * res.getScaleFactor(), false);
        //framebuffer.createFramebuffer((int)res.getScaledWidth() * res.getScaleFactor(), (int)res.getScaledHeight() * res.getScaleFactor());

        GL11.glPushMatrix();
        GlStateManager.scale(1 / antiAliasingFactor, 1 / antiAliasingFactor, 1 / antiAliasingFactor);
        x *= antiAliasingFactor;
        y *= antiAliasingFactor;
        float originalX = x;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.color(1,1,1,1);

        int currentColor = color;

        // Save buffer 1
        CachedTextString cachedTextStringNew = new CachedTextString(text, x, y, width, FONT_HEIGHT, false);

        char[] characters = text.toCharArray();

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        framebuffer.bindFramebuffer(false);
        String[] parts = COLOR_CODE_PATTERN.split(text);

        // Cuz we drawing to a buffer
        originalX = 0;
        x = 0;
        y = 0;

        int index = 0;
        for (String s : parts)
        {
            for (String s2 : s.split("\n"))
            {
                for (String s3 : s2.split("\r"))
                {

                    unicodeFont.drawString(x, y, s3, new org.newdawn.slick.Color(currentColor));
                    x += unicodeFont.getWidth(s3);

                    index += s3.length();
                    if (index < characters.length && characters[index] == '\r')
                    {
                        x = originalX;
                        index++;
                    }
                }
                if (index < characters.length && characters[index] == '\n')
                {
                    x = originalX;
                    y += getHeight(s2) * 2;
                    index++;
                }
            }
            if (index < characters.length)
            {
                char colorCode = characters[index];
                if (colorCode == '\247')
                {
                    char colorChar = characters[index + 1];
                    int codeIndex = ("0123456789" + "abcdef").indexOf(colorChar);
                    if (codeIndex < 0)
                    {
                        if (colorChar == 'r')
                        {
                            currentColor = color;
                        }
                    }
                    else
                    {
                        currentColor = colorCodes[codeIndex];
                    }
                    index += 2;
                }
            }
        }
        framebuffer.unbindFramebuffer();

        //save 2
        cachedTextStringNew.framebuffer = framebuffer;
        cachedStringsArray.add(cachedTextStringNew);

        // Buffer cleanup every 5s
        if(this.cleanupTimer.passed(5000)){
            for(int i = 0; i < cachedStringsArray.size() / 2; ++i){
                // Will remove all even elements.
                cachedStringsArray.remove(i);
            }
        }

        // Render the framebuffer :)

        cachedTextString = cachedTextStringNew;

        // render texture
        GlStateManager.enableAlpha();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        framebuffer.bindFramebufferTexture();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderUtil.drawTexture(cachedTextString.x,
                cachedTextString.y,
                cachedTextString.w,
                cachedTextString.h,
                0,0,1,1);
       /* RenderUtil.drawTexture(0,
                0,
                res.getScaledWidth(),
                res.getScaledHeight(),
                0,0,1,1);*/

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();

        cachedTextString.lastUsed = System.currentTimeMillis();

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.bindTexture(0);

        GlStateManager.popMatrix();

        return (int) getWidth(text);
    }

    public String shadowStr(String t){
        String result = "" + t.charAt(0);
        for(int i = 1; i < t.length(); i++){
            if(t.charAt(i-1) == '\247'){
                result += "0";
            }else {
                result += t.charAt(i);
            }
        }
        return result;
    }

    public int drawStringWithShadow(String text, float x, float y, int color)
    {
        if (text == null || text == "")
            return 0;

        if(!isTTF())
            return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);

        //StringUtils.stripControlCodes(text)
        drawString(shadowStr(text), x + 0.4F, y + 0.4F, 0x000000);
        return drawString(text, x, y, color);
    }

    public void drawCenteredString(String text, float x, float y, int color)
    {
        drawString(text, x - ((int) getWidth(text) >> 1), y, color);
    }

    /**
     * Draw Centered Text Scaled
     *
     * @param text       - Given Text String
     * @param givenX     - Given X Position
     * @param givenY     - Given Y Position
     * @param color      - Given Color (HEX)
     * @param givenScale - Given Scale
     */
    public void drawCenteredTextScaled(String text, int givenX, int givenY, int color, double givenScale)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        drawCenteredString(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, int color)
    {
        drawCenteredString(StringUtils.stripControlCodes(text), x + 0.5F, y + 0.5F, color);
        drawCenteredString(text, x, y, color);
    }

    public static String stripColor(final String input)
    {
        return input == null ? null : Pattern.compile("(?i)" + '\247' + "[0-9A-FK-OR]").matcher(input).replaceAll("");
    }

    public float getWidth(String text)
    {
        if (cachedStringWidth.size() > 1000)
            cachedStringWidth.clear();
        return cachedStringWidth.computeIfAbsent(text, e -> unicodeFont.getWidth(stripColor(text)) / antiAliasingFactor);
    }

    public float getCharWidth(char c)
    {
        return unicodeFont.getWidth(String.valueOf(c));
    }

    public float getHeight(String s)
    {
        return unicodeFont.getHeight(s) / 2.0F;
    }

    public UnicodeFont getFont()
    {
        return unicodeFont;
    }

    public void drawSplitString(ArrayList<String> lines, int x, int y, int color)
    {
        drawString(String.join("\n\r", lines), x, y, color);
    }

    public List<String> splitString(String text, int wrapWidth)
    {
        List<String> lines = new ArrayList<>();

        String[] splitText = text.split(" ");
        StringBuilder currentString = new StringBuilder();

        for (String word : splitText)
        {
            String potential = currentString + " " + word;

            if (getWidth(potential) >= wrapWidth)
            {
                lines.add(currentString.toString());
                currentString = new StringBuilder();
            }

            currentString.append(word).append(" ");
        }

        lines.add(currentString.toString());
        return lines;
    }

    public float getStringWidth(String p_Name)
    {
        if(!isTTF())
            return Minecraft.getMinecraft().fontRenderer.getStringWidth(p_Name);
        return unicodeFont.getWidth(stripColor(p_Name)) / 2;
    }

    public float getStringHeight(String p_Name)
    {
        return getHeight(p_Name);
    }

    /**
     * Trims a string to fit a specified Width.
     */
    public String trimStringToWidth(String text, int width)
    {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse)
    {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k)
        {
            char c0 = text.charAt(l);
            float i1 = this.getWidth(text);

            if (flag)
            {
                flag = false;

                if (c0 != 'l' && c0 != 'L')
                {
                    if (c0 == 'r' || c0 == 'R')
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (i1 < 0)
            {
                flag = true;
            }
            else
            {
                i += i1;

                if (flag1)
                {
                    ++i;
                }
            }

            if (i > width)
            {
                break;
            }

            if (reverse)
            {
                stringbuilder.insert(0, c0);
            }
            else
            {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private CachedTextString getCachedString(String text){
        for(CachedTextString entry : cachedStringsArray){
            if(entry.text.equalsIgnoreCase(text))
                return entry;
        }
        return null;
    }

    private class CachedTextString{
        public CachedTextString(String text, float x, float y, float w, float h, boolean shadow){
            this.text = text;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.shadow = shadow;
        }
        public String text;
        public float x;
        public float y;
        public float w;
        public float h;
        public boolean shadow;
        public Framebuffer framebuffer;
        public float lastUsed = 0;
    }
}
