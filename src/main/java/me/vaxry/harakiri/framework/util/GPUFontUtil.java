package me.vaxry.harakiri.framework.util;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.*;
import org.newdawn.slick.TrueTypeFont;

public class GPUFontUtil {

    private TrueTypeFont font;
    private Font awtFont;

    public GPUFontUtil(){
        awtFont = new Font("Segoe UI", Font.PLAIN, 18);
        font = new TrueTypeFont(awtFont, false);
    }
}
