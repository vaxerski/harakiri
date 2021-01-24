package me.vaxry.harakiri.impl.module.combat;

import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/30/2019 @ 3:37 AM.
 */
public final class AutoTotemModule extends Module {

    public final Value<Float> health = new Value("Health", new String[]{"Hp"}, "The amount of health needed to auto-put a totem.", 16.0f, 0.0f, 20.0f, 0.5f);

    public AutoTotemModule() {
        super("AutoTotem", new String[]{"Totem"}, "Automatically puts a totem into your offhand.", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return "" + this.getTotemCount();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory) {
                if (mc.player.getHealth() <= this.health.getValue()) {
                    final ItemStack offHand = mc.player.getHeldItemOffhand();

                    if (offHand.getItem() == Items.TOTEM_OF_UNDYING) {
                        return;
                    }

                    final int slot = this.getTotemSlot();

                    if (slot != -1) {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.updateController();
                    }
                }
            }
        }
    }

    private int getTotemSlot() {
        for (int i = 0; i < 36; i++) {
            final Item item = Minecraft.getMinecraft().player.inventory.getStackInSlot(i).getItem();
            if (item == Items.TOTEM_OF_UNDYING) {
                if (i < 9) {
                    i += 36;
                }
                return i;
            }
        }
        return -1;
    }

    private int getTotemCount() {
        int totems = 0;

        if (Minecraft.getMinecraft().player == null)
            return totems;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                totems += stack.getCount();
            }
        }

        return totems;
    }

}
