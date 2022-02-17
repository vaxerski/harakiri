package me.vaxry.harakiri.impl.module.player;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class AutoEatModule extends Module {

    public boolean eating = false;

    public final Value<Float> hunger = new Value<Float>("Hunger", new String[]{"food", "h"}, "The amount of hunger needed to acquire some food.", 9.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Float> health = new Value<Float>("Health", new String[]{"health", "he"}, "The amount of health needed to acquire some food.", 9.0f, 0.0f, 20.0f, 0.5f);
    public final Value<Integer> forcedSlot = new Value<Integer>("Slot", new String[]{"s"}, "The hot-bar slot to put the food into. (45 for offhand)", 43, 0, 43, 1);

    private int previousHeldItem = -1;
    private int foodSlot = -1;

    public AutoEatModule() {
        super("AutoEat", new String[]{"Eat", "AutoFeed"}, "Automatically eats food when hunger is below the set threshold.", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public String getMetaData() {
        return "" + this.getFoodCount();
    }

    @Listener
    public void onPlayerUpdate(EventPlayerUpdate event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        eating = false;

        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        if (mc.player.getFoodStats().getFoodLevel() < this.hunger.getValue() || mc.player.getHealth() < this.health.getValue()) {
            this.foodSlot = this.findFood();
        }

        if (this.foodSlot != -1) {
            eating = true;
            if (this.forcedSlot.getValue() != 45) { // we aren't trying to put it in the offhand
                if (this.previousHeldItem == -1) {
                    this.previousHeldItem = mc.player.inventory.currentItem;
                }

                if (this.foodSlot < 36) {
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.QUICK_MOVE, mc.player); // last hot-bar slot
                    mc.playerController.windowClick(0, this.foodSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, this.forcedSlot.getValue(), 0, ClickType.PICKUP, mc.player);
                    mc.player.inventory.currentItem = this.forcedSlot.getValue() - 36;
                } else {
                    mc.player.inventory.currentItem = this.foodSlot - 36; // in the hot-bar, so remove the inventory offset
                }
            } else { // we need this notch apple in the offhand
                if (!(mc.player.getHeldItemOffhand().getItem() instanceof ItemFood)) {
                    mc.playerController.windowClick(0, 45, 0, ClickType.QUICK_MOVE, mc.player); // offhand slot
                    mc.playerController.windowClick(0, this.foodSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                }
            }

            // TODO: make this better
            if (mc.player.getFoodStats().getFoodLevel() >= this.hunger.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() >= this.health.getValue()) {
                mc.gameSettings.keyBindUseItem.pressed = false;
                if (this.previousHeldItem != -1) {
                    mc.player.inventory.currentItem = this.previousHeldItem;
                }
                this.foodSlot = -1;
                this.previousHeldItem = -1;
                eating = false;
            } else {
                mc.displayGuiScreen(null);
                mc.gameSettings.keyBindUseItem.pressed = true;
            }
        }
    }

    private int findFood() {
        float bestSaturation = -1;
        int bestFoodSlot = -1;
        for (int slot = 44; slot > 8; slot--) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventoryContainer.getSlot(slot).getStack();
            if (itemStack.isEmpty())
                continue;

            if (this.isFoodItem(itemStack.getItem())) {
                float saturation = ((ItemFood) itemStack.getItem()).getSaturationModifier(itemStack);
                if (saturation > bestSaturation) {
                    bestSaturation = saturation;
                    bestFoodSlot = slot;
                }
            }
        }
        return bestFoodSlot;
    }

    public int getFoodCount() {
        int food = 0;

        if (Minecraft.getMinecraft().player == null)
            return food;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && this.isFoodItem(stack.getItem())) {
                food += stack.getCount();
            }
        }

        return food;
    }

    private boolean isFoodItem(Item item) {
        if (!(item instanceof ItemFood))
            return false; // is not of ItemFood class

        if (item == Items.CHORUS_FRUIT || item == Items.ROTTEN_FLESH || item == Items.POISONOUS_POTATO || item == Items.SPIDER_EYE)
            return false;

        return true;
    }
}
