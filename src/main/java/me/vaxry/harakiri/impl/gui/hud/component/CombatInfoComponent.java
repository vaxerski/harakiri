package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.hud.component.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.module.combat.*;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;
import java.text.DecimalFormat;


public final class CombatInfoComponent extends DraggableHudComponent {

    private final int MODULES_DISPLAYED = 5;

    public CombatInfoComponent() {
        super("Speed");
        this.setH(MODULES_DISPLAYED * Harakiri.get().getTTFFontUtil().FONT_HEIGHT + MODULES_DISPLAYED);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        String finalDrawString = "";

        finalDrawString += ChatFormatting.GRAY + "CA: ";
        finalDrawString += Harakiri.get().getModuleManager().find(CrystalAuraModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.GRAY + "\nKA: ";
        finalDrawString += Harakiri.get().getModuleManager().find(KillAuraModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.GRAY + "\nObsRepl: ";
        finalDrawString += Harakiri.get().getModuleManager().find(ObsidianReplaceModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.GRAY + "\nNoCrys: ";
        finalDrawString += Harakiri.get().getModuleManager().find(NoCrystalModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.GRAY + "\nAT: ";
        finalDrawString += ((AutoTotemModule)Harakiri.get().getModuleManager().find(AutoTotemModule.class)).getOverrideStatus() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        this.setW(Harakiri.get().getTTFFontUtil().getStringWidth(finalDrawString));
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(finalDrawString, this.getX(), this.getY(), -1);
    }

}