package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.framework.patch.ClassPatch;
import me.vaxry.harakiri.framework.patch.MethodPatch;
import me.vaxry.harakiri.impl.management.PatchManager;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IRETURN;

/**
 * Author Seth
 * 4/10/2019 @ 3:27 AM.
 */
public final class KeyBindingPatch extends ClassPatch {

    public KeyBindingPatch() {
        super("net.minecraft.client.settings.KeyBinding", "bhy");
    }

    /**
     * This is where minecraft checks if a key is down
     * We need to patch it because forge adds a key conflicting
     * problem and doesn't allow us to keep keys pressed while
     * gui's are open
     *
     * @param methodNode
     * @param env
     */
    @MethodPatch(
            mcpName = "isKeyDown",
            notchName = "e",
            mcpDesc = "()Z")
    public void isKeyDown(MethodNode methodNode, PatchManager.Environment env) {
        AbstractInsnNode target = null;

        for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
            if (insn.getOpcode() == ALOAD) {
                target = insn;
                break;
            }
        }

        if (target != null) {
            AbstractInsnNode next = target.getNext().getNext(); //If
            while (next.getOpcode() != IRETURN) {
                next = next.getNext();
                methodNode.instructions.remove(next.getPrevious());
            }
        }
    }

}

