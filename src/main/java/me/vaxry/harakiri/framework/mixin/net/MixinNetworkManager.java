package me.vaxry.harakiri.framework.mixin.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.EventStageable;
import me.vaxry.harakiri.framework.event.network.EventReceivePacket;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler<Packet<?>> {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacketPre(Packet<?> packetIn, CallbackInfo ci) {
        final EventSendPacket event = new EventSendPacket(EventStageable.EventStage.PRE, packetIn);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void onSendPacket(Packet<?> packetIn, CallbackInfo ci) {
        final EventSendPacket event = new EventSendPacket(EventStageable.EventStage.POST, packetIn);
        Harakiri.get().getEventManager().dispatchEvent(event);
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead0Pre(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        final EventReceivePacket event = new EventReceivePacket(EventStageable.EventStage.PRE, p_channelRead0_2_);
        Harakiri.get().getEventManager().dispatchEvent(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("RETURN"))
    private void onChannelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        final EventReceivePacket event = new EventReceivePacket(EventStageable.EventStage.POST, p_channelRead0_2_);
        Harakiri.get().getEventManager().dispatchEvent(event);
    }
}
