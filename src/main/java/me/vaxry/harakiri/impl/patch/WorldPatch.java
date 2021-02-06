package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.world.*;
import me.vaxry.harakiri.framework.patch.ClassPatch;
import me.vaxry.harakiri.framework.patch.MethodPatch;
import me.vaxry.harakiri.impl.management.PatchManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/6/2019 @ 1:25 PM.
 */
public final class WorldPatch extends ClassPatch {

    public WorldPatch() {
        super("net.minecraft.world.World", "amu");
    }

    /**
     * This function is used to update light for blocks
     * It is VERY unoptimized and in some cases it's
     * better off to disable
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "checkLightFor",
            notchName = "c",
            mcpDesc = "(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)Z",
            notchDesc = "(Lana;Let;)Z")
    public void checkLightFor(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList list = new InsnList();
        //call our hook function
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "checkLightForHook", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals" and pass in the label
        list.add(new JumpInsnNode(IFEQ, jmp));
        //add 0 or false
        list.add(new InsnNode(ICONST_0));
        //return 0 or false
        list.add(new InsnNode(IRETURN));
        //add our label
        list.add(jmp);
        //insert the instructions at the top of the function
        methodNode.instructions.insert(list);
    }

    public static boolean checkLightForHook() {
        final EventLightUpdate event = new EventLightUpdate();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

        if (Minecraft.getMinecraft().isSingleplayer()) {
            return false;
        }

        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "getRainStrength",
            notchName = "j",
            mcpDesc = "(F)F",
            notchDesc = "(F)F")
    public void getRainStrength(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "getRainStrengthHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        list.add(new JumpInsnNode(IFEQ, jmp));
        list.add(new InsnNode(FCONST_0));
        list.add(new InsnNode(FRETURN));
        list.add(jmp);
        methodNode.instructions.insert(list);
    }

    public static boolean getRainStrengthHook() {
        final EventRainStrength event = new EventRainStrength();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "onEntityAdded",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void onEntityAdded(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityAddedHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)V" : "(Lvg;)V", false));
        methodNode.instructions.insert(list);
    }

    public static void onEntityAddedHook(Entity entity) {
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventAddEntity(entity));
    }

    @MethodPatch(
            mcpName = "onEntityRemoved",
            notchName = "c",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void onEntityRemoved(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "onEntityRemovedHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)V" : "(Lvg;)V", false));
        methodNode.instructions.insert(list);
    }

    public static void onEntityRemovedHook(Entity entity) {
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventRemoveEntity(entity));
    }

    @MethodPatch(
            mcpName = "spawnParticle",
            notchName = "a",
            mcpDesc = "(IZDDDDDD[I)V",
            notchDesc = "(IZDDDDDD[I)V")
    public void spawnParticle(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "spawnParticleHook", "()Z", false));
        final LabelNode jmp = new LabelNode();
        list.add(new JumpInsnNode(IFEQ, jmp));
        list.add(new InsnNode(RETURN));
        list.add(jmp);
        methodNode.instructions.insert(list);
    }

    public static boolean spawnParticleHook() {
        final EventSpawnParticle event = new EventSpawnParticle();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);
        return event.isCanceled();
    }

    @MethodPatch(
            mcpName = "spawnEntity",
            notchName = "b",
            mcpDesc = "(Lnet/minecraft/entity/Entity;)V",
            notchDesc = "(Lvg;)V")
    public void spawnEntity(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "spawnEntityHook", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/entity/Entity;)V" : "(Lvg;)V", false));
        methodNode.instructions.insert(list);
    }

    public static void spawnEntityHook(Entity entity) {
        Harakiri.INSTANCE.getEventManager().dispatchEvent(new EventSpawnEntity(entity));
        if (entity instanceof EntityFireworkRocket) {
            entity.setDead();
            return;
        }
    }
}
