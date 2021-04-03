package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.player.EventMove;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.player.FreeCamModule;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class SafeWalkModule extends Module {

    public final Value<Integer> height = new Value<Integer>("Height", new String[]{"Hei", "H"}, "Checks height.", 1, 0, 32, 1);

    public SafeWalkModule() {
        super("SafeWalk", new String[]{"SWalk"}, "Prevents you from walking off certain blocks.", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onMove(EventMove event) {
        final Minecraft mc = Minecraft.getMinecraft();
        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();

        final FreeCamModule freeCam = (FreeCamModule) Harakiri.get().getModuleManager().find(FreeCamModule.class);

        if (freeCam != null && freeCam.isEnabled()) {
            return;
        }

        if (mc.player.onGround && !mc.player.noClip) {
            double increment;
            for (increment = 0.05D; x != 0.0D && isOffsetBBEmpty(x, -this.height.getValue(), 0.0D); ) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
            }
            for (; z != 0.0D && isOffsetBBEmpty(0.0D, -this.height.getValue(), z); ) {
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
            for (; x != 0.0D && z != 0.0D && isOffsetBBEmpty(x, -this.height.getValue(), z); ) {
                if (x < increment && x >= -increment) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= increment;
                } else {
                    x += increment;
                }
                if (z < increment && z >= -increment) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= increment;
                } else {
                    z += increment;
                }
            }
        }
        event.setX(x);
        event.setY(y);
        event.setZ(z);
    }

    private boolean isOffsetBBEmpty(double x, double y, double z) {
        return Minecraft.getMinecraft().world.getCollisionBoxes(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.getEntityBoundingBox().offset(x, y, z)).isEmpty();
    }

}
