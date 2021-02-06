package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.client.CPacketPlaceRecipe;
import net.minecraft.util.ResourceLocation;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 7/19/2019 @ 8:46 PM.
 */
public final class AutoCraftModule extends Module {

    public final Value<Boolean> drop = new Value<>("Drop", new String[]{"d"}, "Drop the crafted item.", false);
    public final Value<String> recipe = new Value<>("Recipe", new String[]{"Recipes", "Rec", "Rec"}, "The recipe name of what you want to craft.", "");
    public final Value<Float> delay = new Value<>("Delay", new String[]{"Del"}, "The crafting delay (in ms).", 50.0f, 0.0f, 500.0f, 1.0f);

    private final Timer timer = new Timer();

    public AutoCraftModule() {
        super("AutoCraft", new String[]{"AutomaticCraft", "ACraft"}, "Automatically crafts stuff for you.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.recipe.getValue().length() > 0 && this.timer.passed(this.delay.getValue())) {
                if (mc.currentScreen == null || mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiCrafting) {
                    IRecipe recipe = CraftingManager.getRecipe(new ResourceLocation(this.recipe.getValue().toLowerCase()));
                    if (recipe != null) {
                        mc.player.connection.sendPacket(new CPacketPlaceRecipe(mc.player.openContainer.windowId, recipe, true));
                        mc.playerController.windowClick(mc.player.openContainer.windowId, 0, 0, this.drop.getValue() ? ClickType.THROW : ClickType.QUICK_MOVE, mc.player);
                        mc.playerController.updateController();
                    }
                }

                this.timer.reset();
            }
        }
    }

}
