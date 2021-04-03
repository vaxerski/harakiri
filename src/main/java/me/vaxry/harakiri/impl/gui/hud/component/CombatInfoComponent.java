package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.impl.module.combat.*;
import me.vaxry.harakiri.impl.module.world.ScaffoldModule;
import org.lwjgl.opengl.GL11;


public final class CombatInfoComponent extends DraggableHudComponent {

    private final int MODULES_DISPLAYED = 6;

    public CombatInfoComponent() {
        super("CombatInfo");
        this.setH(MODULES_DISPLAYED * Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        String finalDrawString = "";

        finalDrawString += ChatFormatting.GRAY + "CA: ";
        finalDrawString += Harakiri.get().getModuleManager().find(CrystalAuraModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.RESET + " \n";

        finalDrawString += ChatFormatting.GRAY + "KA: ";
        finalDrawString += Harakiri.get().getModuleManager().find(KillAuraModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.RESET + " \n";

        finalDrawString += ChatFormatting.GRAY + "ObsRepl: ";
        finalDrawString += Harakiri.get().getModuleManager().find(ObsidianReplaceModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.RESET + " \n";

        finalDrawString += ChatFormatting.GRAY + "NoCrys: ";
        finalDrawString += Harakiri.get().getModuleManager().find(NoCrystalModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.RESET + " \n";

        finalDrawString += ChatFormatting.GRAY + "AT: ";
        finalDrawString += ((AutoTotemModule)Harakiri.get().getModuleManager().find(AutoTotemModule.class)).getOverrideStatus() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        finalDrawString += ChatFormatting.RESET + " \n";

        finalDrawString += ChatFormatting.GRAY + "S: ";
        finalDrawString += Harakiri.get().getModuleManager().find(ScaffoldModule.class).isEnabled() ?
                ChatFormatting.GREEN + "ON" :
                ChatFormatting.RED + "OFF";

        float w = 0;
        for(String s : finalDrawString.split("\n")){
            if(Harakiri.get().getTTFFontUtil().getStringWidth(s) > w)
                w = Harakiri.get().getTTFFontUtil().getStringWidth(s);
        }

        this.setW(w);
        this.setH(MODULES_DISPLAYED * Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(finalDrawString, this.getX(), this.getY(), -1);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

}