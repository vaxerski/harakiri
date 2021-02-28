package me.vaxry.harakiri.impl.patch;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.gui.EventRenderTooltip;
import me.vaxry.harakiri.framework.patch.ClassPatch;
import me.vaxry.harakiri.framework.patch.MethodPatch;
import me.vaxry.harakiri.impl.management.PatchManager;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.stiff.pomelo.EventManager;

import static org.objectweb.asm.Opcodes.*;

/**
 * created by noil on 11/4/19 at 2:03 PM
 */
public final class GuiScreenPatch extends ClassPatch {

    public GuiScreenPatch() {
        super("net.minecraft.client.gui.GuiScreen", "blk");
    }

    @MethodPatch(
            mcpName = "renderToolTip",
            notchName = "a",
            mcpDesc = "(Lnet/minecraft/item/ItemStack;II)V",
            notchDesc = "(Laip;II)V")
    public void renderToolTip(MethodNode methodNode, PatchManager.Environment env) {
        final InsnList list = new InsnList();
        list.add(new TypeInsnNode(NEW, Type.getInternalName(EventRenderTooltip.class)));
        list.add(new InsnNode(DUP));
        list.add(new VarInsnNode(ALOAD, 1));
        list.add(new VarInsnNode(ILOAD, 2));
        list.add(new VarInsnNode(ILOAD, 3));
        list.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(EventRenderTooltip.class), "<init>", env == PatchManager.Environment.IDE ? "(Lnet/minecraft/item/ItemStack;II)V" : "(Laip;II)V", false));
        list.add(new VarInsnNode(ASTORE, 7));
        list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(Harakiri.class), "INSTANCE", "Lme/vaxry/harakiri/Harakiri;"));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(Harakiri.class), "getEventManager", "()Lteam/stiff/pomelo/EventManager;", false));
        list.add(new VarInsnNode(ALOAD, 7));
        list.add(new MethodInsnNode(INVOKEINTERFACE, Type.getInternalName(EventManager.class), "dispatchEvent", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        list.add(new InsnNode(POP));
        list.add(new VarInsnNode(ALOAD, 7));
        list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(EventRenderTooltip.class), "isCanceled", "()Z", false));
        final LabelNode jmp = new LabelNode();
        list.add(new JumpInsnNode(IFEQ, jmp));
        list.add(new InsnNode(RETURN));
        list.add(jmp);
        methodNode.instructions.insert(list);
    }
}
