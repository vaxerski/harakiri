package me.vaxry.harakiri.impl.module.render;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.render.EventRender2D;
import me.vaxry.harakiri.api.event.render.EventRenderName;
import me.vaxry.harakiri.api.extd.ShaderGroupExt;
import me.vaxry.harakiri.api.friend.Friend;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 4/20/2019 @ 10:07 AM.
 */

public final class ESPModule extends Module {

    public final Value<Boolean> items = new Value<Boolean>("Items", new String[]{"Items", "i"}, "Draw Items", false);

    private ResourceLocation shader;
    private boolean toLoadShader = false;

    // scoreboard stuff //
    private Scoreboard board;
    private ScorePlayerTeam green;
    private ScorePlayerTeam red;


    public ESPModule() {
        super("ESP (not yet)", new String[]{"ESP"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);

        shader = new ResourceLocation("harakirimod", "shaders/shader.json");
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        toLoadShader = true;
    }

    @SubscribeEvent
    public void onRenderLivingBasePre(RenderLivingEvent.Specials.Pre<EntityLivingBase> ent){
        Minecraft mc = Minecraft.getMinecraft();
        if(toLoadShader) {

            // Create the shader
            try {
                mc.renderGlobal.entityOutlineShader = new ShaderGroupExt(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
                mc.renderGlobal.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                mc.renderGlobal.entityOutlineFramebuffer = mc.renderGlobal.entityOutlineShader.getFramebufferRaw("final");

                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                this.board = mc.player.getWorldScoreboard();
                this.green = board.createTeam("haraGreen");
                this.red = board.createTeam("haraRed");
                this.green.setPrefix(TextFormatting.GREEN.toString());
                this.red.setPrefix(TextFormatting.RED.toString());

            }catch(Throwable t){
                Harakiri.INSTANCE.logChat("Shader failed: " + t.getMessage());
               // JOptionPane.showMessageDialog(null, t.getMessage(), "Error in ESP shader!", JOptionPane.INFORMATION_MESSAGE);
            }

            toLoadShader = false;
        }

        if(this.isEnabled())
            ent.getEntity().setGlowing(true);
        else
            ent.getEntity().setGlowing(false);


        if(ent.getEntity() instanceof EntitySkeleton)
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), red.getName());
        else
            board.addPlayerToTeam(ent.getEntity().getUniqueID().toString(), green.getName());
    }

    @SubscribeEvent
    public void onRenderLivingBasePost(RenderLivingEvent.Specials.Post<EntityLivingBase> ent){

       // if(ent.getEntity() instanceof EntityPlayer)
       //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), red);
       // else
       //     board.removePlayerFromTeam(ent.getEntity().getUniqueID().toString(), green);

    }
}
