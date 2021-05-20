package me.vaxry.harakiri.impl.module.render;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.player.EventPlayerDamageBlock;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.event.render.EventRenderBossHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;


public final class NoBreakAnimModule extends Module {

    public NoBreakAnimModule() {
        super("NoBreakAnim", new String[]{"AntiBreakAnim", "NoBreakAnimation"}, "Prevents the break animation serverside.", "NONE", -1, ModuleType.RENDER);
    }

    Attender<EventPlayerDamageBlock> ondamage = new Attender<>(EventPlayerDamageBlock.class, event -> {
        Minecraft.getMinecraft().player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, event.getPos(), event.getFace()));
    });

}
