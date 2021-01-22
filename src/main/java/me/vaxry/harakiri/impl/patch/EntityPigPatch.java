package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.entity.EventPigTravel;
import me.vaxry.harakiri.api.event.entity.EventSteerEntity;
import me.vaxry.harakiri.api.patch.ClassPatch;
import me.vaxry.harakiri.api.patch.MethodPatch;
import me.vaxry.harakiri.api.util.ASMUtil;
import me.vaxry.harakiri.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Author Seth
 * 4/9/2019 @ 11:15 AM.
 */
public final class EntityPigPatch extends ClassPatch {

    public EntityPigPatch() {
        super("net.minecraft.entity.passive.EntityPig", "aad");
    }

    /**
     * This is where minecraft checks if you can steer pigs
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "canBeSteered",
            notchName = "cV",
            mcpDesc = "()Z")
    public void canBeSteered(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions and add the needed instructions to call our hook function
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "canBeSteeredHook", "()Z", false));
        //add a label to jump to
        final LabelNode jmp = new LabelNode();
        //add if equals and pass the label
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //add 1 or true
        insnList.add(new InsnNode(ICONST_1));
        //add return so the rest of the function doesn't get called
        insnList.add(new InsnNode(IRETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructions at the top of the function
        methodNode.instructions.insert(insnList);
    }

    @MethodPatch(
            mcpName = "travel",
            notchName = "a",
            mcpDesc = "(FFF)V")
    public void travel(MethodNode methodNode, PatchManager.Environment env) {
        final AbstractInsnNode target = ASMUtil.findMethodInsn(methodNode, INVOKEVIRTUAL, env == PatchManager.Environment.IDE ? "net/minecraft/entity/passive/EntityPig" : "aad", env == PatchManager.Environment.IDE ? "setAIMoveSpeed" : "k", "(F)V");
        if (target != null) {
            //create a list of instructions and add the needed instructions to call our hook function
            final InsnList insnList = new InsnList();
            //call our hook function
            insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "travelHook", "()Z", false));
            //add a label to jump to
            final LabelNode jmp = new LabelNode();
            //add if equals and pass the label
            insnList.add(new JumpInsnNode(IFEQ, jmp));
            //add return so the rest of the function doesn't get called
            insnList.add(new InsnNode(RETURN));
            //add our label
            insnList.add(jmp);
            //insert the list of instructions at the top of the function
            methodNode.instructions.insert(target, insnList);
        }
    }

    public static boolean travelHook() {
        final EventPigTravel event = new EventPigTravel();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

    /**
     * Our canBeSteered hook
     * Used to allow us to steer and control pigs
     * without a "carrot on a stick"
     *
     * @return
     */
    public static boolean canBeSteeredHook() {
        //dispatch our event
        final EventSteerEntity event = new EventSteerEntity();
        Harakiri.INSTANCE.getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }

}
