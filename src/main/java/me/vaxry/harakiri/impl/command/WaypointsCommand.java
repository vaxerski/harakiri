package me.vaxry.harakiri.impl.command;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.command.Command;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.StringUtil;
import me.vaxry.harakiri.impl.config.WaypointsConfig;
import me.vaxry.harakiri.impl.config.WorldConfig;
import me.vaxry.harakiri.impl.module.world.WaypointsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;

import java.text.DecimalFormat;

/**
 * Author Seth
 * 5/8/2019 @ 7:03 AM.
 */
public final class WaypointsCommand extends Command {

    private String[] addAlias = new String[]{"Add", "A"};
    private String[] removeAlias = new String[]{"Remove", "R", "Rem", "Delete", "Del"};
    private String[] listAlias = new String[]{"List", "L"};
    private String[] angleAlias = new String[]{"Angle", "Ang"};
    private String[] clearAlias = new String[]{"Clear", "C"};

    public WaypointsCommand() {
        super("Waypoints", new String[]{"Wp", "Waypoint"}, "Allows you to add waypoints", "Waypoints Add <Name>\n" +
                "Waypoints Add <Name> <X> <Y> <Z>\n" +
                "Waypoints Remove <Name>\n" +
                "Waypoints List\n" +
                "Waypoints Angle <Name>\n" +
                "Waypoints Clear");
    }

    @Override
    public void exec(String input) {
        if (!this.clamp(input, 2)) {
            this.printUsage();
            return;
        }

        final String[] split = input.split(" ");

        final String host = Minecraft.getMinecraft().getCurrentServerData() != null ? Minecraft.getMinecraft().getCurrentServerData().serverIP : "localhost";

        if (equals(addAlias, split[1])) {
            if (!this.clamp(input, 3, 6)) {
                this.printUsage();
                return;
            }

            final String name = split[2];

            final WaypointsModule.WaypointData waypointData = Harakiri.INSTANCE.getWaypointManager().find(host, name);

            if (waypointData != null) {
                Harakiri.INSTANCE.logChat("\247c\"" + name + "\"\247f is already a waypoint");
            } else {
                if (split.length > 3) {
                    if (!this.clamp(input, 6, 6)) {
                        this.printUsage();
                        return;
                    }
                    if (StringUtil.isDouble(split[3])) {
                        if (StringUtil.isDouble(split[4])) {
                            if (StringUtil.isDouble(split[5])) {
                                final DecimalFormat format = new DecimalFormat("#.#");
                                final String x = format.format(Double.parseDouble(split[3]));
                                final String y = format.format(Double.parseDouble(split[4]));
                                final String z = format.format(Double.parseDouble(split[5]));

                                Harakiri.INSTANCE.logChat("Added waypoint " + name + " at x:" + x + " y:" + y + " z:" + z);
                                Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().add(new WaypointsModule.WaypointData(host, name, Minecraft.getMinecraft().player.dimension, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5])));
                                Harakiri.INSTANCE.getConfigManager().save(WaypointsConfig.class);
                            } else {
                                Harakiri.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[5] + "\"");
                            }
                        } else {
                            Harakiri.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[4] + "\"");
                        }
                    } else {
                        Harakiri.INSTANCE.errorChat("Unknown number " + "\247f\"" + split[3] + "\"");
                    }
                } else {
                    final DecimalFormat format = new DecimalFormat("#.#");
                    Harakiri.INSTANCE.logChat("Added waypoint " + name + " at x:" + format.format(Minecraft.getMinecraft().player.posX) + " y:" + format.format(Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight()) + " z:" + format.format(Minecraft.getMinecraft().player.posZ));
                    Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().add(new WaypointsModule.WaypointData(host, name, Minecraft.getMinecraft().player.dimension, Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY + Minecraft.getMinecraft().player.getEyeHeight(), Minecraft.getMinecraft().player.posZ));
                    Harakiri.INSTANCE.getConfigManager().save(WaypointsConfig.class);
                }
            }
        } else if (equals(removeAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }

            final String name = split[2];

            final WaypointsModule.WaypointData waypointData = Harakiri.INSTANCE.getWaypointManager().find(host, name);

            if (waypointData != null) {
                Harakiri.INSTANCE.logChat("Removed waypoint \247c" + waypointData.getName() + " \247f");
                Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().remove(waypointData);
                Harakiri.INSTANCE.getConfigManager().save(WaypointsConfig.class);
            } else {
                Harakiri.INSTANCE.errorChat("Unknown waypoint " + "\247f\"" + name + "\"");
            }
        } else if (equals(listAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            int size = 0;

            for (WaypointsModule.WaypointData waypointData : Harakiri.INSTANCE.getWaypointManager().getWaypointDataList()) {
                if (waypointData.getHost().equals(host)) {
                    size++;
                }
            }

            if (size > 0) {
                final TextComponentString msg = new TextComponentString("\2477Waypoints for " + host + " [" + size + "]\247f ");

                final DecimalFormat format = new DecimalFormat("#.#");

                for (WaypointsModule.WaypointData data : Harakiri.INSTANCE.getWaypointManager().getWaypointDataList()) {
                    if (data != null && data.getHost().equals(host)) {
                        msg.appendSibling(new TextComponentString("\2477[\247a" + data.getName() + "\2477] ")
                                .setStyle(new Style()
                                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("x: " + format.format(data.getX()) + " y: " + format.format(data.getY()) + " z: " + format.format(data.getZ()))))));
                    }
                }

                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any waypoints for " + host);
            }
        } else if (equals(angleAlias, split[1])) {
            if (!this.clamp(input, 3, 3)) {
                this.printUsage();
                return;
            }
            final String name = split[2];
            final WaypointsModule.WaypointData waypointData = Harakiri.INSTANCE.getWaypointManager().find(host, name);

            if (waypointData != null) {
                float[] angle = MathUtil.calcAngle(Minecraft.getMinecraft().player.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks()), new Vec3d(waypointData.getX(), waypointData.getY(), waypointData.getZ()));
                Harakiri.INSTANCE.getRotationManager().setPlayerRotations(angle[0], angle[1]);

                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    Minecraft.getMinecraft().player.getRidingEntity().rotationYaw = angle[0];
                    Minecraft.getMinecraft().player.getRidingEntity().rotationPitch = angle[1];
                }

                final DecimalFormat format = new DecimalFormat("#.#");
                Harakiri.INSTANCE.logChat("Set Angles to " + format.format(angle[0]) + ", " + format.format(angle[1]));

            } else {
                Harakiri.INSTANCE.errorChat("Unknown waypoint " + "\247f\"" + name + "\"");
            }
        } else if (equals(clearAlias, split[1])) {
            if (!this.clamp(input, 2, 2)) {
                this.printUsage();
                return;
            }

            final int waypoints = Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().size();

            if (waypoints > 0) {
                Harakiri.INSTANCE.logChat("Removed \247c" + waypoints + "\247f waypoint" + (waypoints > 1 ? "s" : ""));
                Harakiri.INSTANCE.getWaypointManager().getWaypointDataList().clear();
                Harakiri.INSTANCE.getConfigManager().save(WorldConfig.class);
            } else {
                Harakiri.INSTANCE.logChat("You don't have any waypoints");
            }
        } else {
            Harakiri.INSTANCE.errorChat("Unknown input " + "\247f\"" + input + "\"");
            this.printUsage();
        }

    }
}
