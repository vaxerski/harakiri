package me.vaxry.harakiri.impl.fml.core;

import me.vaxry.harakiri.impl.management.PatchManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Author Seth
 * 4/5/2019 @ 1:24 AM.
 */
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
}
