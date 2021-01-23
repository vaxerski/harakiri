package me.vaxry.harakiri.impl.module.render;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.event.EventStageable;
import me.vaxry.harakiri.api.event.network.EventReceivePacket;
import me.vaxry.harakiri.api.event.render.EventRender2D;
import me.vaxry.harakiri.api.event.render.EventRenderName;
import me.vaxry.harakiri.api.friend.Friend;
import me.vaxry.harakiri.api.module.Module;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.util.*;
import me.vaxry.harakiri.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.*;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

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


    public ESPModule() {
        super("ESP", new String[]{"ESP"}, "Highlights entities", "NONE", -1, ModuleType.RENDER);

    }

    @SubscribeEvent
    public void onRenderLivingBase(RenderLivingEvent.Specials.Post<EntityLivingBase> ent){
        if(!(ent.getEntity() instanceof EntityPlayer))
            return;

        EntityPlayer e = (EntityPlayer)ent.getEntity();
    }

}
