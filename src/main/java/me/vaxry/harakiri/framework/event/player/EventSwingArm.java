package me.vaxry.harakiri.framework.event.player;

import me.vaxry.harakiri.framework.event.EventCancellable;
import net.minecraft.util.EnumHand;

/**
 * Author Seth
 * 4/8/2019 @ 3:57 AM.
 */
public class EventSwingArm extends EventCancellable {

    private EnumHand hand;

    public EventSwingArm(EnumHand hand) {
        this.hand = hand;
    }

    public EnumHand getHand() {
        return hand;
    }

    public void setHand(EnumHand hand) {
        this.hand = hand;
    }
}
