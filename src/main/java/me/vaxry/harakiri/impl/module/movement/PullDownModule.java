package me.vaxry.harakiri.impl.module.movement;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public class PullDownModule extends Module {
    private static final float VELOCITY_MAX = 10.0f;
    public final Value<Boolean> jumpDisables =
            new Value<Boolean>("JumpDisables", new String[]{"jump"}, "When enabled, holding the jump key will disable any speed boosts.", true);
    public final Value<Float> speed =
            new Value<Float>("Speed", new String[]{"velocity"}, "Speed multiplier at which the player will be falling.", 4.0f,
                    0f, VELOCITY_MAX, 1f);
    public final Value<Float> minhe =
            new Value<Float>("MinHeight", new String[]{"minheight"}, "Minimum height to activate.", 3.0f,
                    1f, 10F, 1f);

    public PullDownModule() {
        super("PullDown", new String[]{"FastFall"}, "Increase your falling velocity.",
                "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if (this.jumpDisables.getValue() && mc.gameSettings.keyBindJump.isKeyDown())
                return;

            if (mc.player.isElytraFlying() || mc.player.capabilities.isFlying ||
                    mc.player.onGround || mc.player.fallDistance <= 0.0f)
                return;

            final Vec3d playerPosition = mc.player.getPositionVector();
            if (!hullCollidesWithBlock(mc.player, playerPosition.subtract(0.0d,
                    this.minhe.getValue(), 0.0d)))
                mc.player.motionY = -this.speed.getValue();
        }
    }

    private boolean hullCollidesWithBlock(final Entity entity,
                                          final Vec3d nextPosition) {
        final AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
        final Vec3d[] boundingBoxCorners = {
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ)
        };

        final Vec3d entityPosition = entity.getPositionVector();
        for (final Vec3d entityBoxCorner : boundingBoxCorners) {
            final Vec3d nextBoxCorner = entityBoxCorner.subtract(entityPosition).add(nextPosition);
            final RayTraceResult rayTraceResult = entity.world.rayTraceBlocks(entityBoxCorner,
                    nextBoxCorner, true, false, true);
            if (rayTraceResult == null)
                continue;

            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK)
                return true;
        }

        return false;
    }
}
