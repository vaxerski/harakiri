package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.framework.patch.ClassPatch;

/**
 * Author Seth
 * 5/24/2019 @ 3:42 AM.
 */
public final class EntityPatch extends ClassPatch {

    public EntityPatch() {
        super("net.minecraft.entity.Entity", "vg");
    }
//
//    @MethodPatch(
//            mcpName = "move",
//            notchName = "a",
//            mcpDesc = "(Lnet/minecraft/entity/MoverType;DDD)V",
//            notchDesc = "(Lvv;DDD)V")
//    public void move(MethodNode methodNode, PatchManager.Environment env) {
//        final AbstractInsnNode insnNode = ASMUtil.findPatternInsn(methodNode, new int[] {Opcodes.ALOAD, Opcodes.GETFIELD, Opcodes.ICONST_0, Opcodes.FCMPL, Opcodes.IFLE});
//        if(insnNode != null) {
//            methodNode.instructions.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(this.getClass()), "moveHook", "()V", false));
//        }
//    }
//
//    public static void moveHook() {
//        Harakiri.get().getEventManager().dispatchEvent(new EventStep());
//    }

}
