package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class CoordLoggerModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Change between various coordlogger modes.", Mode.VANILLA);

    private enum Mode {
        VANILLA, SPIGOT
    }

    public final Value<Boolean> thunder = new Value<Boolean>("Thunder", new String[]{"thund"}, "Thunder/lightning sounds.", true);
    public final Value<Boolean> endPortal = new Value<Boolean>("EndPortal", new String[]{"portal"}, "End portal creation sound.", true);
    public final Value<Boolean> endDragon = new Value<Boolean>("EndDragon", new String[]{"dragon"}, "End dragon sounds.", true);
    public final Value<Boolean> wither = new Value<Boolean>("Wither", new String[]{"with"}, "Wither sounds.", true);

    public CoordLoggerModule() {
        super("CoordLogger", new String[]{"CoordLog", "CLogger", "CLog"}, "Logs useful coordinates", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {

            if (event.getPacket() instanceof SPacketSpawnMob) {
                final SPacketSpawnMob packet = (SPacketSpawnMob) event.getPacket();
            }

            if (event.getPacket() instanceof SPacketSoundEffect) {
                final SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (this.thunder.getValue()) {
                    if (packet.getCategory() == SoundCategory.WEATHER && packet.getSound() == SoundEvents.ENTITY_LIGHTNING_THUNDER) {
                        float yaw = 0;
                        final double difX = packet.getX() - Minecraft.getMinecraft().player.posX;
                        final double difZ = packet.getZ() - Minecraft.getMinecraft().player.posZ;

                        yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                        Harakiri.get().logChat("Lightning spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                    }
                }
            }
            if (event.getPacket() instanceof SPacketEffect) {
                final SPacketEffect packet = (SPacketEffect) event.getPacket();
                if (this.endPortal.getValue()) {
                    if (packet.getSoundType() == 1038) {
                        Harakiri.get().logChat("End Portal activated X:" + packet.getSoundPos().getX() + " Y:" + packet.getSoundPos().getY() + " Z:" + packet.getSoundPos().getZ());
                    }
                }
                if (this.wither.getValue()) {
                    if (packet.getSoundType() == 1023) {
                        switch (this.mode.getValue()) {
                            case VANILLA:
                                Harakiri.get().logChat("Wither spawned X:" + packet.getSoundPos().getX() + " Y:" + packet.getSoundPos().getY() + " Z:" + packet.getSoundPos().getZ());
                                break;
                            case SPIGOT:
                                float yaw = 0;
                                final double difX = packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX;
                                final double difZ = packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ;

                                yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                                Harakiri.get().logChat("Wither spawned X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                                break;
                        }
                    }
                }
                if (this.endDragon.getValue()) {
                    if (packet.getSoundType() == 1028) {
                        float yaw = 0;
                        final double difX = packet.getSoundPos().getX() - Minecraft.getMinecraft().player.posX;
                        final double difZ = packet.getSoundPos().getZ() - Minecraft.getMinecraft().player.posZ;

                        yaw += MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0f) - yaw);

                        Harakiri.get().logChat("Ender Dragon killed X:" + Minecraft.getMinecraft().player.posX + " Z:" + Minecraft.getMinecraft().player.posZ + " Angle:" + yaw);
                    }
                }
            }
        }
    }
}
