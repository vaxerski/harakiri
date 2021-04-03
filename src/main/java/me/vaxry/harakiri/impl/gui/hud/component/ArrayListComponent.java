package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.client.EventSaveConfig;
import me.vaxry.harakiri.framework.event.world.EventLoadWorld;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.ColorUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.framework.gui.anchor.AnchorPoint;
import me.vaxry.harakiri.impl.module.combat.VelocityModule;
import me.vaxry.harakiri.impl.module.hidden.ArrayListModule;
import me.vaxry.harakiri.impl.module.render.HudModule;
import net.minecraft.client.gui.ScaledResolution;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArrayListComponent extends DraggableHudComponent {

    private ArrayListModule.Mode SORTING_MODE = ArrayListModule.Mode.LENGTH;
    private boolean SHOW_METADATA = true;
    private boolean LOWERCASE = false;

    private boolean RAINBOW = true;
    private float RAINBOW_HUE_DIFFERENCE = 2.5f;
    private float RAINBOW_HUE_SPEED = 50.f;
    private float RAINBOW_SATURATION = 50.f;
    private float RAINBOW_BRIGHTNESS = 50.f;

    private Timer timer = new Timer();
    private float curHue = 0;

    public ArrayListComponent(AnchorPoint anchorPoint) {
        super("ArrayList");
        //this.setAnchorPoint(anchorPoint); // dont plox
        this.setAnchorPoint(null);
        this.setVisible(true);

        Harakiri.get().getEventManager().addEventListener(this); // subscribe to the event manager
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = RAINBOW_HUE_SPEED;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.updateValues();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final List<Module> mods = new ArrayList<>();
        final ScaledResolution res = new ScaledResolution(mc);
        boolean isInHudEditor = mc.currentScreen instanceof GuiHudEditor;

        float xOffset = 0;
        float yOffset = 0;
        float maxWidth = 0;
        int hueDifference = 0;

        final HudModule hm = (HudModule) Harakiri.get().getModuleManager().find(HudModule.class);
        boolean useRainbow;
        if(hm.rainbow.getValue())
            useRainbow = true;
        else
            useRainbow = false;

        for (Module mod : Harakiri.get().getModuleManager().getModuleList()) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                mods.add(mod);
            }
        }

        if (mods.size() > 0) {
            if (SORTING_MODE.equals(ArrayListModule.Mode.LENGTH)) {
                final Comparator<Module> lengthComparator = (first, second) -> {
                    String firstName = first.getDisplayName() + (SHOW_METADATA ? (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    String secondName = second.getDisplayName() + (SHOW_METADATA ? (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if (LOWERCASE) {
                        firstName = firstName.toLowerCase();
                        secondName = secondName.toLowerCase();
                    }
                    final float dif = Harakiri.get().getTTFFontUtil().getStringWidth(secondName) - Harakiri.get().getTTFFontUtil().getStringWidth(firstName);
                    return dif != 0 ? (int) dif : secondName.compareTo(firstName);
                };
                mods.sort(lengthComparator);
            } else if (SORTING_MODE.equals(ArrayListModule.Mode.ALPHABET)) {
                final Comparator<Module> alphabeticalComparator = (first, second) -> {
                    String firstName = first.getDisplayName() + (SHOW_METADATA ? (first.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + first.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    String secondName = second.getDisplayName() + (SHOW_METADATA ? (second.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + second.getMetaData().toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if (LOWERCASE) {
                        firstName = firstName.toLowerCase();
                        secondName = secondName.toLowerCase();
                    }
                    return firstName.compareToIgnoreCase(secondName);
                };
                mods.sort(alphabeticalComparator);
            } else if (SORTING_MODE.equals(ArrayListModule.Mode.UNSORTED)) {

            }

            float jitter = getJitter();
            curHue += jitter;

            if(curHue > 10000)
                curHue -= 10000;

            for (Module mod : mods) {
                if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && (mod.isEnabled() || Harakiri.get().getTTFFontUtil().getStringWidth(mod.getDisplayName()) > mod.activexOffset) && !mod.isHidden()) {
                    String name = mod.getDisplayName() + (SHOW_METADATA ? (mod.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + mod.getMetaData().charAt(0) + mod.getMetaData().substring(1, mod.getMetaData().length()).toLowerCase() + ChatFormatting.GRAY + "]" : "") : "");
                    if(mod instanceof VelocityModule)
                        name = mod.getDisplayName() + (SHOW_METADATA ? (mod.getMetaData() != null ? " " + ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + mod.getMetaData() + ChatFormatting.GRAY + "]" : "") : "");

                    if (LOWERCASE)
                        name = name.toLowerCase();

                    final float width = Harakiri.get().getTTFFontUtil().getStringWidth(name);

                    int color;
                    if (useRainbow) {
                        Color rainbow = new Color(Color.HSBtoRGB((float) ((curHue * 7.5f) / (100.0D - RAINBOW_HUE_SPEED) + Math.sin(hueDifference / (100.0D - RAINBOW_HUE_SPEED * Math.PI / 2.0D))) % 1.0F, RAINBOW_SATURATION, RAINBOW_BRIGHTNESS));
                        color = ColorUtil.changeAlpha((new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue())).getRGB(), 0xFF);
                    } else {
                        color = mod.getColorByGroup();
                    }

                    if (width >= maxWidth) {
                        maxWidth = width;
                    }

                    if(mod.activexOffset > 0 || (!mod.isEnabled() && Harakiri.get().getTTFFontUtil().getStringWidth(mod.getDisplayName()) > mod.activexOffset)) {
                        if((!mod.isEnabled() && Harakiri.get().getTTFFontUtil().getStringWidth(mod.getDisplayName()) > mod.activexOffset)) {
                            mod.activexOffset += 1.3f;
                        }else if(mod.isEnabled()) {
                            mod.activexOffset -= 1.3f;
                        }
                    }
                    else
                        mod.activexOffset = 0;

                    if (this.getAnchorPoint() != null) {
                        if (this.getAnchorPoint().getPoint() != null) {
                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                case BOTTOM_CENTER:
                                    xOffset = (this.getW() - Harakiri.get().getTTFFontUtil().getStringWidth(name)) / 2;
                                    break;
                                case TOP_LEFT:
                                case BOTTOM_LEFT:
                                    xOffset = 0;
                                    break;
                                case TOP_RIGHT:
                                case BOTTOM_RIGHT:
                                    xOffset = this.getW() - Harakiri.get().getTTFFontUtil().getStringWidth(name);
                                    break;
                            }

                            switch (this.getAnchorPoint().getPoint()) {
                                case TOP_CENTER:
                                case TOP_LEFT:
                                case TOP_RIGHT:
                                    Harakiri.get().getTTFFontUtil().drawStringWithShadow(name, this.getX() + xOffset + mod.activexOffset, this.getY() + yOffset, color);
                                    if(mod.activexOffset != 0){
                                        final float perc = mod.activexOffset / Harakiri.get().getTTFFontUtil().getStringWidth(mod.getDisplayName());
                                        yOffset += (Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1) * (1 - perc);
                                    }else
                                        yOffset += (Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1);
                                    break;
                                case BOTTOM_CENTER:
                                case BOTTOM_LEFT:
                                case BOTTOM_RIGHT:
                                    Harakiri.get().getTTFFontUtil().drawStringWithShadow(name, this.getX() + xOffset + mod.activexOffset, this.getY() + (this.getH() - Harakiri.get().getTTFFontUtil().FONT_HEIGHT) + yOffset, color);
                                    yOffset -= (Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1);
                                    break;
                            }
                        }
                    } else {
                        Harakiri.get().getTTFFontUtil().drawStringWithShadow(name, this.getX() + xOffset + mod.activexOffset, this.getY() + yOffset, color);
                        yOffset += (Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1);
                    }

                    hueDifference = (int) (hueDifference + RAINBOW_HUE_DIFFERENCE);
                }
            }
        }

        if (isInHudEditor) {
            if (maxWidth == 0) { // no mods
                final String arraylist = "(enabled modules)";
                Harakiri.get().getTTFFontUtil().drawStringWithShadow(arraylist, this.getX(), this.getY(), 0xFFAAAAAA);
                maxWidth = Harakiri.get().getTTFFontUtil().getStringWidth(arraylist) + 1 /* right side gap */;
                yOffset = Harakiri.get().getTTFFontUtil().FONT_HEIGHT + 1 /* right side gap */;
            }
        }

        this.setW(maxWidth);
        this.setH(Math.abs(yOffset));

        if (this.getH() > res.getScaledHeight()) {
            this.setH(res.getScaledHeight() - 4);
        }
    }

    @Listener
    public void onLoadWorld(EventLoadWorld eventLoadWorld) {
        this.updateValues();
    }

    @Listener
    public void onConfigSave(EventSaveConfig eventSaveConfig) {
        this.updateValues();
    }

    private void updateValues() {
        final HudModule hudModule = (HudModule) Harakiri.get().getModuleManager().find(HudModule.class);
        if (hudModule != null && hudModule.isEnabled()) {
            this.RAINBOW = hudModule.rainbow.getValue();
            this.RAINBOW_HUE_DIFFERENCE = hudModule.rainbowHueDifference.getValue();
            this.RAINBOW_HUE_SPEED = hudModule.rainbowHueSpeed.getValue();
            this.RAINBOW_SATURATION = hudModule.rainbowSaturation.getValue();
            this.RAINBOW_BRIGHTNESS = hudModule.rainbowBrightness.getValue();
        }

        final ArrayListModule arrayListModule = (ArrayListModule) Harakiri.get().getModuleManager().find(ArrayListModule.class);
        if (arrayListModule != null) {
            this.SORTING_MODE = arrayListModule.mode.getValue();
            this.LOWERCASE = arrayListModule.lowercase.getValue();
            this.SHOW_METADATA = arrayListModule.showMetadata.getValue();
        }
    }
}
