package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.network.EventSendPacket;
import me.vaxry.harakiri.api.patch.ClassPatch;
import me.vaxry.harakiri.api.patch.MethodPatch;
import me.vaxry.harakiri.api.util.ASMUtil;
import me.vaxry.harakiri.impl.management.PatchManager;
import net.minecraft.network.Packet;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/6/2019 @ 1:46 PM.
 */
public final class NetworkManagerPatch extends ClassPatch {

    public NetworkManagerPatch() {
        super("net.minecraft.network.NetworkManager", "gw");
    }

    /**
     * This is where minecraft sends packets before they are compressed and encrypted
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "sendPacket",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/network/Packet;)V",
            notchDesc = "(Lht;)V")
    public void sendPacket(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList preInsn = new InsnList();
        //add ALOAD to pass the Packet into our hook function
        preInsn.add(new VarInsnNode(ALOAD, 1));
        //PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "PRE", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //call our hook function
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "sendPacketHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/network/Packet;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z" : "(Lht;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        preInsn.add(new InsnNode(RETURN));
        //add our label
        preInsn.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(preInsn);

        //do the same thing as above but insert the list at the bottom of the method
        //we dont need to cancel post
        final InsnList postInsn = new InsnList();
        //add ALOAD to pass the Packet into our hook function
        postInsn.add(new VarInsnNode(ALOAD, 1));
        //POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "POST", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //call our hook function
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "sendPacketHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/network/Packet;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z" : "(Lht;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * This is our sendPacket hook
     * It allows us to intercept outgoing unencrypted packets and modify the data
     * It also allows us to cancel sending certain packets
     *
     * @param packet
     * @param stage
     * @return
     */
    public static boolean sendPacketHook(Packet packet, EventStageable.EventStage stage) {
        final EventSendPacket event = new EventSendPacket(stage, packet);
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * This is where minecraft handles received packets
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "channelRead0",
            notchName = "a",
            mcpDesc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            notchDesc = "(Lio/netty/channel/ChannelHandlerContext;Lht;)V")
    public void channelRead0(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList preInsn = new InsnList();
        //add ALOAD to pass the Packet into our hook function
        preInsn.add(new VarInsnNode(ALOAD, 2));
        //PRE
        preInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "PRE", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //call our hook function
        preInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "channelRead0Hook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/network/Packet;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z" : "(Lht;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        preInsn.add(new JumpInsnNode(IFEQ, jmp));
        //add return so the rest of the function doesn't get called
        preInsn.add(new InsnNode(RETURN));
        //add our label
        preInsn.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(preInsn);

        //do the same thing as above but insert the list at the bottom of the method
        //we dont need to cancel post
        final InsnList postInsn = new InsnList();
        //add ALOAD to pass the Packet into our hook function
        postInsn.add(new VarInsnNode(ALOAD, 2));
        //POST
        postInsn.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/EventStageable$EventStage", "POST", "Lme/vaxry/harakiri/api/event/EventStageable$EventStage;"));
        //call our hook function
        postInsn.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "channelRead0Hook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/network/Packet;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z" : "(Lht;Lme/vaxry/harakiri/api/event/EventStageable$EventStage;)Z", false));
        //insert the list of instructions at the bottom of the function
        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), postInsn);
    }

    /**
     * This is our channelRead0 hook
     * It allows us to cancel processing incoming packets or modify the unencrypted data
     *
     * @param packet
     * @param stage
     * @return
     */
    public static boolean channelRead0Hook(Packet packet, EventStageable.EventStage stage) {
        if (packet != null) {
            final EventReceivePacket event = new EventReceivePacket(stage, packet);
            Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

            return event.isCanceled();
        }

        return false;
    }


}
