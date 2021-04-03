package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.player.EventPlayerUpdate;
import me.vaxry.harakiri.framework.Friend;
import me.vaxry.harakiri.framework.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class MiddleClickFriendsModule extends Module {

    private boolean clicked;

    public MiddleClickFriendsModule() {
        super("MiddleClickFriends", new String[]{"MCF", "MiddleClickFriends", "MClick"}, "Allows you to middle click players to add them as a friend.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.currentScreen == null) {
                if (Mouse.isButtonDown(2)) {
                    if (!this.clicked) {
                        final RayTraceResult result = mc.objectMouseOver;
                        if (result != null && result.typeOfHit == RayTraceResult.Type.ENTITY) {
                            final Entity entity = result.entityHit;
                            if (entity != null && entity instanceof EntityPlayer) {
                                final Friend friend = Harakiri.get().getFriendManager().isFriend(entity);

                                if (friend != null) {
                                    Harakiri.get().getFriendManager().getFriendList().remove(friend);
                                    Harakiri.get().logChat("Removed \247c" + friend.getAlias() + " \247f");
                                } else {
                                    Harakiri.get().getFriendManager().add(entity.getName(), entity.getName(), true);
                                    Harakiri.get().logChat("Added \247c" + entity.getName() + " \247f");
                                }
                            }
                        }
                    }
                    this.clicked = true;
                } else {
                    this.clicked = false;
                }
            }
        }
    }

}
