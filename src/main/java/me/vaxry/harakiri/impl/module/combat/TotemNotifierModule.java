package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.minecraft.EventRunTick;
import me.vaxry.harakiri.framework.event.world.EventRemoveEntity;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;


public class TotemNotifierModule extends Module {
    final Minecraft mc = Minecraft.getMinecraft();

    public TotemNotifierModule() {
        super("TotemPopNotifier", new String[]{"tm"}, "Notifies you when others around pop totems.", "NONE", -1, ModuleType.COMBAT);
    }

    public final List<Integer> entitiesWithTotems = new ArrayList<>();

    @Listener
    public void runTick(EventRunTick event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.world == null) return;
            for (Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityLivingBase) {
                    final Iterable<ItemStack> stacks = entity.getEquipmentAndArmor();
                    for (ItemStack stack : stacks) {
                        final Item offhandItem = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.OFFHAND).getItem();
                        if (offhandItem == Items.TOTEM_OF_UNDYING) {
                            if (!entitiesWithTotems.contains(entity.getEntityId())) {
                                entitiesWithTotems.add(entity.getEntityId());
                            }
                        } else if (offhandItem == Items.AIR) {
                            if (entitiesWithTotems.contains(entity.getEntityId())) {
                                Harakiri.get().getNotificationManager().addNotification("", entity.getName() + " just popped a totem.");
                                entitiesWithTotems.removeIf(i -> i.equals(entity.getEntityId()));
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onEntityRemove(EventRemoveEntity event) {
        if (entitiesWithTotems.contains(event.getEntity().getEntityId())) {
            entitiesWithTotems.removeIf(i -> i.equals(event.getEntity().getEntityId()));
        }
    }
}
