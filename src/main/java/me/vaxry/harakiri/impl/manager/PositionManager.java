package me.vaxry.harakiri.impl.manager;

import net.minecraft.client.Minecraft;

public final class PositionManager {

    private double x;
    private double y;
    private double z;

    public void updatePosition() {
        this.x = Minecraft.getMinecraft().player.posX;
        this.y = Minecraft.getMinecraft().player.posY;
        this.z = Minecraft.getMinecraft().player.posZ;
    }

    public void restorePosition() {
        Minecraft.getMinecraft().player.posX = this.x;
        Minecraft.getMinecraft().player.posY = this.y;
        Minecraft.getMinecraft().player.posZ = this.z;
    }

    public void setPlayerPosition(double x, double y, double z) {
        Minecraft.getMinecraft().player.posX = x;
        Minecraft.getMinecraft().player.posY = y;
        Minecraft.getMinecraft().player.posZ = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }
}
