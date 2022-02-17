package me.vaxry.harakiri.impl.gui.hud.component;

import com.mojang.realmsclient.gui.ChatFormatting;

import org.lwjgl.opengl.GL11;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.impl.module.world.FillLavaModule;
import me.vaxry.harakiri.impl.module.world.NukerModule;
import me.vaxry.harakiri.impl.module.world.ScaffoldModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Chat;

public final class HighwayInfoComponent extends DraggableHudComponent {

    public HighwayInfoComponent() {
        super("HighwayInfo");
        this.setH(Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
    }

    private double distance(Vec3d a, Vec3d b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        String infoToPrint = ChatFormatting.GREEN + "Hello, Vaxry.\n" + ChatFormatting.GRAY + "Nuker status: " + ChatFormatting.WHITE;
        final boolean nukerActive = Harakiri.get().getModuleManager().find(NukerModule.class).isEnabled();

        infoToPrint += (nukerActive ? ((NukerModule)Harakiri.get().getModuleManager().find(NukerModule.class)).nukerStatus : "off") + "\n" + ChatFormatting.GRAY;

        final float seconds = nukerActive ? ((System.currentTimeMillis() - ((NukerModule)Harakiri.get().getModuleManager().find(NukerModule.class)).nukerStartTimer.getTime()) / 1000.0f) : 0;
        final double distance = nukerActive ? distance(((NukerModule)Harakiri.get().getModuleManager().find(NukerModule.class)).startVec, Minecraft.getMinecraft().player.getPositionVector()) : 0;
        final double speed = nukerActive ? (distance / 1000.f) / (seconds / 3600.f) : 0; // km/h

        infoToPrint += "Average speed: " + ChatFormatting.WHITE + Float.toString(((float)((int)(speed * 10.f))) / 10.f) + "km/h\n" + ChatFormatting.GRAY;

        infoToPrint += "Scaffold status: " + ChatFormatting.WHITE + ((ScaffoldModule)Harakiri.get().getModuleManager().find(ScaffoldModule.class)).scaffoldStatus + "\n" + ChatFormatting.GRAY;
        
        infoToPrint += "FillLava status: " + ChatFormatting.WHITE + ((FillLavaModule)Harakiri.get().getModuleManager().find(FillLavaModule.class)).fillStatus + "\n" + ChatFormatting.GRAY;

        long maxCoord = (long)Math.max(Minecraft.getMinecraft().player.getPositionVector().x, Minecraft.getMinecraft().player.getPositionVector().z);
        if (maxCoord < 3750000) {
            // highway to 3.75M
            double completedPerc = maxCoord / 3750000.D * 100.D;
            infoToPrint += "Completion: " + ChatFormatting.WHITE + Double.toString(((long)(completedPerc * 1000.D)) / 1000.D) + "%" + "\n" + ChatFormatting.GRAY;
        } else {
            // highway to 30M
            double completedPerc = (maxCoord - 3750000.D) / 26250000.D * 100.D;
            infoToPrint += "Completion: " + ChatFormatting.WHITE + Double.toString(((long)(completedPerc * 1000.D)) / 1000.D) + "%" + "\n" + ChatFormatting.GRAY;
        }

        infoToPrint += "Elapsed: " + ChatFormatting.WHITE + (seconds > 3600 ? Integer.toString((int)(seconds / 3600)) + "h " : "") + (seconds > 60 ? Integer.toString((int)(seconds % 3600 / 60)) + "m " : "") + Integer.toString((int)seconds % 60) + "s" + "\n" + ChatFormatting.GRAY;
        infoToPrint += "Dug: " + ChatFormatting.WHITE + Float.toString((float)((int)(distance / 1000.f * 10.f) / 10.f)) + "km" + "\n" + ChatFormatting.GRAY;

        int completedMillions = (int)(maxCoord / 1000000F);
        float nextMilestonePerc = (maxCoord - (completedMillions * 1000000)) / 1000000F * 100F;

        infoToPrint += "Next milestone: " + ChatFormatting.WHITE + Float.toString((float)((int)(nextMilestonePerc * 10.f) / 10.f)) + "%";

        float w = 0;
        float lines = 0;
        for (String s : infoToPrint.split("\n")) {
            if (Harakiri.get().getTTFFontUtil().getStringWidth(s) > w){
                w = Harakiri.get().getTTFFontUtil().getStringWidth(s);
            }

            lines++;
        }

        this.setW(w);
        this.setH(lines * Harakiri.get().getTTFFontUtil().FONT_HEIGHT);
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(infoToPrint, this.getX(), this.getY(), -1);

        GL11.glEnable(GL11.GL_ALPHA_TEST);

    }
}
