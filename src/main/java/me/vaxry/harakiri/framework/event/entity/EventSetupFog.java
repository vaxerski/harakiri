package me.vaxry.harakiri.framework.event.entity;

import me.vaxry.harakiri.framework.event.EventCancellable;
import me.vaxry.harakiri.framework.event.EventStageable;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class EventSetupFog extends EventStageable {
    public World world;
    public Entity entity;
    public float partialTicks;

    public EventSetupFog(World w, Entity e, float pt){
        this.world = w;
        this.entity = e;
        this.partialTicks = pt;
    }
}
