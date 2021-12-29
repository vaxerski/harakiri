package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.impl.manager.APIManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import me.vaxry.harakiri.framework.Module;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;

import java.util.*;

import akka.actor.FSM.Event;

public final class MobOwnerModule extends Module {

    private HashMap<String, String> alreadyResolvedUUIDs = new HashMap<>();

    public MobOwnerModule() {
        super("MobOwner", new String[]{"MobOwner"}, "Shows the mob owner.", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void onRender2D(EventRender2D event) {

        final Minecraft mc = Minecraft.getMinecraft();
        
        for (Entity e : mc.world.getLoadedEntityList()) {
            if ((e instanceof EntityTameable)){
                handleTameable((EntityTameable)e, event);
            } else if (e instanceof AbstractHorse) {
                handleHorse((AbstractHorse)e, event);
            }
        }
    }

    public void handleHorse(AbstractHorse ent, EventRender2D event) {
        if (ent.getOwnerUniqueId() == null)
            return;

        if (!alreadyResolvedUUIDs.containsKey(ent.getOwnerUniqueId().toString())) {
            alreadyResolvedUUIDs.put(ent.getOwnerUniqueId().toString(), ent.getOwnerUniqueId().toString());
            new Thread(() -> {
                final String name = Harakiri.get().getApiManager().resolveName(ent.getOwnerUniqueId().toString());
                if (name != null) {
                    alreadyResolvedUUIDs.put(ent.getOwnerUniqueId().toString(), name);
                }
            }).start();
        }

        // draw
        final String toDrawName = alreadyResolvedUUIDs.get(ent.getOwnerUniqueId().toString());

        ((NametagsModule) Harakiri.get().getModuleManager().find(NametagsModule.class)).drawNametag(toDrawName,
                0xFFFFFFFF, ent, event, true);
    }

    public void handleTameable(EntityTameable ent, EventRender2D event) {
        if (!ent.isTamed())
            return;

        if (ent.getOwner() == null)
            return;

        if (ent.getOwnerId() == null)
            return;

        if (!alreadyResolvedUUIDs.containsKey(ent.getOwnerId().toString())) {
            alreadyResolvedUUIDs.put(ent.getOwnerId().toString(), ent.getOwnerId().toString());
            new Thread(() -> {
                final String name = Harakiri.get().getApiManager().resolveName(ent.getOwnerId().toString());
                if (name != null) {
                    alreadyResolvedUUIDs.put(ent.getOwnerId().toString(), name);
                }
            }).start();
        }

        // draw
        final String toDrawName = alreadyResolvedUUIDs.get(ent.getOwnerId().toString());

        ((NametagsModule) Harakiri.get().getModuleManager().find(NametagsModule.class)).drawNametag(toDrawName,
                0xFFFFFFFF, ent, event, true);
    }

}
