package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.world.EventChunk;
import me.vaxry.harakiri.api.patch.ClassPatch;
import me.vaxry.harakiri.api.patch.MethodPatch;
import me.vaxry.harakiri.api.util.ASMUtil;
import me.vaxry.harakiri.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * created by noil on 11/3/19 at 5:59 PM
 */
public final class ChunkPatch extends ClassPatch {

    public ChunkPatch() {
        super("net.minecraft.world.chunk.Chunk", "axw");
    }

    @MethodPatch(
            mcpName = "onUnload",
            notchName = "d",
            mcpDesc = "()V",
            notchDesc = "()V")
    public void onUnload(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList insnList = new InsnList();

        insnList.add(new TypeInsnNode(NEW, Type.getInternalName(EventChunk.class)));
        insnList.add(new InsnNode(DUP));
        insnList.add(new FieldInsnNode(GETSTATIC, "me/vaxry/harakiri/api/event/world/EventChunk$ChunkType", "UNLOAD", "Lme/vaxry/harakiri/api/event/world/EventChunk$ChunkType;"));
        insnList.add(new VarInsnNode(ALOAD, 0));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventChunk.class), "<init>", env == PatchManager.Environment.IDE ? "(Lme/vaxry/harakiri/api/event/world/EventChunk$ChunkType;Lnet/minecraft/world/chunk/Chunk;)V" : "(Lme/vaxry/harakiri/api/event/world/EventChunk$ChunkType;Laxw;)V", false));
        insnList.add(new VarInsnNode(ASTORE, 7));
        insnList.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Harakiri.class), "INSTANCE", "Lme/vaxry/harakiri/Harakiri;"));
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Harakiri.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        insnList.add(new VarInsnNode(ALOAD, 7));
        insnList.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        insnList.add(new InsnNode(POP));

        methodNode.instructions.insertBefore(ASMUtil.bottom(methodNode), insnList);
    }

    /**
     *     // access flags 0x9
     *     public static handleChunkDataHook(Lnet/minecraft/world/chunk/Chunk;)V
     *     NEW me/vaxry/harakiri/api/event/world/EventChunk
     *     DUP
     *     GETSTATIC me/vaxry/harakiri/api/event/world/EventChunk$ChunkType.LOAD : Lme/vaxry/harakiri/api/event/world/EventChunk$ChunkType;
     *     ALOAD 0
     *     INVOKESPECIAL me/vaxry/harakiri/api/event/world/EventChunk.<init> (Lme/vaxry/harakiri/api/event/world/EventChunk$ChunkType;Lnet/minecraft/world/chunk/Chunk;)V
     *     ASTORE 1
     *
     *     GETSTATIC me/vaxry/harakiri/Harakiri.INSTANCE : Lme/vaxry/harakiri/Harakiri;
     *     INVOKEVIRTUAL me/vaxry/harakiri/Harakiri.getEventManager ()Lteam/stiff/pomelo/EventManager;
     *     ALOAD 1
     *     INVOKEINTERFACE team/stiff/pomelo/EventManager.dispatchEvent (Ljava/lang/Object;)Ljava/lang/Object; (itf)
     *     POP
     *
     *     LOCALVARIABLE chunk Lnet/minecraft/world/chunk/Chunk; L0 L3 0
     *     LOCALVARIABLE event Lme/vaxry/harakiri/api/event/world/EventChunk; L1 L3 1
     *     MAXSTACK = 4
     *     MAXLOCALS = 2
     */
}
