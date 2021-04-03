package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class FullbrightModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The fullbright mode to use.", Mode.GAMMA);
    public final Value<Boolean> disablePotion = new Value<Boolean>("DisablePotion", new String[]{"AutoDisablePotion", "dp", "adp"}, "Automatically remove the night vision effect if using a different mode.", false);

    private enum Mode {
        GAMMA, POTION, TABLE
    }

    private float lastGamma;

    private World world;

    public FullbrightModule() {
        super("Fullbright", new String[]{"FullBright", "Bright"}, "Makes the world brighter", "NONE", -1, ModuleType.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (this.mode.getValue() == Mode.GAMMA) {
            this.lastGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.mode.getValue() == Mode.GAMMA) {
            Minecraft.getMinecraft().gameSettings.gammaSetting = this.lastGamma;
        }

        if (this.mode.getValue() == Mode.POTION && Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.removePotionEffect(MobEffects.NIGHT_VISION);
        }

        if (this.mode.getValue() == Mode.TABLE) {
            if (Minecraft.getMinecraft().world != null) {
                float f = 0.0F;

                for (int i = 0; i <= 15; ++i) {
                    float f1 = 1.0F - (float) i / 15.0F;
                    Minecraft.getMinecraft().world.provider.getLightBrightnessTable()[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) + 0.0F;
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (!this.mode.getValue().equals(Mode.POTION) && this.disablePotion.getValue())
                Minecraft.getMinecraft().player.removePotionEffect(MobEffects.NIGHT_VISION);

            switch (this.mode.getValue()) {
                case GAMMA:
                    Minecraft.getMinecraft().gameSettings.gammaSetting = 1000;
                    break;
                case POTION:
                    Minecraft.getMinecraft().player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5210));
                    break;
                case TABLE:
                    if (this.world != Minecraft.getMinecraft().world) {
                        if (Minecraft.getMinecraft().world != null) {
                            for (int i = 0; i <= 15; ++i) {
                                Minecraft.getMinecraft().world.provider.getLightBrightnessTable()[i] = 1.0f;
                            }
                        }
                        this.world = Minecraft.getMinecraft().world;
                    }
                    break;
            }
        }
    }
}
