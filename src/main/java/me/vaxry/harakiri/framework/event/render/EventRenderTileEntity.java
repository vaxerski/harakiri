package me.vaxry.harakiri.framework.event.render;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.tileentity.TileEntity;

public class EventRenderTileEntity extends EventCancellable {
    public TileEntity te;
    public float partialticks;
    public float destroystage;

    public EventRenderTileEntity(TileEntity t, float pt, float ds){
        this.te = t;
        this.partialticks = pt;
        this.destroystage = ds;
    }
}
