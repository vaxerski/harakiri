package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRenderBossHealth;
import me.vaxry.harakiri.framework.patch.ClassPatch;
import me.vaxry.harakiri.framework.patch.MethodPatch;
import me.vaxry.harakiri.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * created by noil on 10/4/2019 at 2:40 PM
 */
public final class GuiBossOverlayPatch extends ClassPatch {

    public GuiBossOverlayPatch() {
        super("net.minecraft.client.gui.GuiBossOverlay", "biz");
    }

    @MethodPatch(
            mcpName = "renderBossHealth",
            notchName = "a",
            mcpDesc = "()V",
            notchDesc = "()V")
    public void renderBossHealth(MethodNode methodNode, PatchManager.Environment env) {
        //create a list of instructions
        final InsnList insnList = new InsnList();
        //call our hook function
        insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(this.getClass()), "renderBossHealthHook", "()Z", false));
        //create a label to jump to
        final LabelNode jmp = new LabelNode();
        //add "if equals"
        insnList.add(new JumpInsnNode(IFEQ, jmp));
        //return so the rest of the function doesnt get called
        insnList.add(new InsnNode(RETURN));
        //add our label
        insnList.add(jmp);
        //insert the list of instructs at the top of the function
        methodNode.instructions.insert(insnList);
    }

    public static boolean renderBossHealthHook() {
        final EventRenderBossHealth event = new EventRenderBossHealth();
        Harakiri.get().getEventManager().dispatchEvent(event);

        return event.isCanceled();
    }
}
