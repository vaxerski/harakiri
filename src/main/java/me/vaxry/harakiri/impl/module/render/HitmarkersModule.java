package me.vaxry.harakiri.impl.module.render;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.network.EventSendPacket;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Texture;
import me.vaxry.harakiri.framework.util.GLUProjection;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.impl.module.combat.CrystalAuraModule;
import me.vaxry.harakiri.impl.module.combat.KillAuraModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.RayTraceResult;
import org.locationtech.jts.geom.Coordinate;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;

public class HitmarkersModule extends Module {

    private Texture hitmarkerCOD;
    private CPacketUseEntity lastPacket = null;

    private enum MODE {
            COD,
            BONK,
            HIT,
            POW
    }

    //public final Value<MODE> mode = new Value<MODE>("Mode", new String[]{"Mode", "Mod"}, "Display mode.", MODE.COD);
    public final Value<Float> life = new Value<Float>("Lifetime", new String[]{"Lifetime", "Life"}, "How long the hitmarker is alive. (in seconds)", 1.f, 0.1f, 5.f, 0.1f);
    public final Value<Float> scale = new Value<Float>("Scale", new String[]{"Scale", "Sc"}, "Scale of the sprite.", 1.f, 0.25f, 2.f, 0.1f);

    ArrayList<HitmarkerData> hitmarkers = new ArrayList<>();

    public HitmarkersModule() {
        super("Hitmarkers", new String[]{"Hitmarkers", "hitmark"}, "Draws hitmarkers.", "NONE", -1, Module.ModuleType.RENDER);

        hitmarkerCOD = new Texture("hitmarker.png");
    }

    private class HitmarkerData {
        private HitmarkersModule parent;

        private final float speed = 0.3f;
        private final float size = 16.f;

        private Coordinate worldCoord;
        private float lifetime;
        private MODE mode;

        private Texture texture;

        private Timer timer = new Timer();

        // Create a hitmarker
        public HitmarkerData(Coordinate worldCoord, float lifetime, MODE mode, Texture texture, HitmarkersModule parent){
            this.worldCoord = worldCoord;
            this.lifetime = lifetime;
            this.mode = mode;
            this.texture = texture;
            this.parent = parent;

            this.timer.reset();
        }

        private float getJitterNormTo1() {
            final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

            final float desiredTimePerSecond = 1;

            this.timer.reset();
            return Math.min(desiredTimePerSecond * seconds, 1.0f);
        }

        private void updateHitmarkerAnimation(){
            if(this.mode != MODE.COD) {
                this.worldCoord.setY(
                        this.worldCoord.y + this.getJitterNormTo1() * this.speed
                );
            }
        }

        private Coordinate conv3Dto2DSpace(double x, double y, double z) {
            final GLUProjection.Projection projection = GLUProjection.getInstance().project(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ, GLUProjection.ClampMode.NONE, false);

            return projection.getType() == GLUProjection.Projection.Type.OUTSIDE || projection.getType() == GLUProjection.Projection.Type.INVERTED ? null : new Coordinate(projection.getX(), projection.getY());
        }

        private void draw3DReticleAt(Coordinate coordinate, float a){
            Coordinate Coord2D = conv3Dto2DSpace(coordinate.x, coordinate.y, coordinate.z);
            if(Coord2D == null)
                return;

            //GlStateManager.enableTexture2D();
            //GlStateManager.color(1.0f, 1.0f, 1.0f, a);

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            this.texture.bind();
            //this.texture.bind();
           // GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, a);
            RenderUtil.drawTexture((float)Coord2D.x - (size * this.parent.scale.getValue())/2.f, (float)Coord2D.y - (size * this.parent.scale.getValue())/2.f, (size * this.parent.scale.getValue()), (size * this.parent.scale.getValue()), 0, 0, 1, 1);
            //RenderUtil.drawTexture((float)Coord2D.x - size/2.f, (float)Coord2D.y - size/2.f, size, size, 0, 0, 1, 1);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();

            //GlStateManager.disableTexture2D();
            //GlStateManager.disableTexture2D();
        }

        public boolean draw(){
            updateHitmarkerAnimation();

            final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

            float alpha = this.timer.passed(this.lifetime * 1000 - 500) ? ((this.lifetime - seconds) / 0.5f) : 1.0F;

            draw3DReticleAt(this.worldCoord, alpha);

            if(this.timer.passed(this.lifetime * 1000))
                return false;
            return true;
        }
    }

    @Listener
    public void onRender2D(EventRender2D event){
        ArrayList<HitmarkerData> toremove = new ArrayList<>();
        for(HitmarkerData hitmarkerData : hitmarkers){
            if(!hitmarkerData.draw())
                toremove.add(hitmarkerData);

        }

        for(HitmarkerData hitmarkerData : toremove)
            hitmarkers.remove(hitmarkerData);
    }

    @Listener
    public void onPacketSend(EventSendPacket event){
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
            if (packet.getAction() == CPacketUseEntity.Action.ATTACK && packet != lastPacket) {

                Minecraft mc = Minecraft.getMinecraft();

                Coordinate hitCoord;

                KillAuraModule killAuraModule = (KillAuraModule)Harakiri.get().getModuleManager().find(KillAuraModule.class);
                CrystalAuraModule crystalAuraModule = (CrystalAuraModule)Harakiri.get().getModuleManager().find(CrystalAuraModule.class);

                if(killAuraModule.killAuraHit || crystalAuraModule.crystalAuraHit){
                    // Kill aura (or ca), arbitrary hitmarker

                    Entity rayEnt = mc.world.getEntityByID(packet.entityId);

                    if(rayEnt == null)
                        return;

                    hitCoord = new Coordinate(rayEnt.posX,
                            rayEnt.posY + rayEnt.getEyeHeight() / 1.6f,
                            rayEnt.posZ);

                }else{
                    // Calculate raytrace hit
                    final RayTraceResult rayTraceResult = mc.objectMouseOver;
                    mc.entityRenderer.getMouseOver(0);
                    if(rayTraceResult == null) {
                        return;
                    }

                    hitCoord = new Coordinate(rayTraceResult.hitVec.x, rayTraceResult.hitVec.y, rayTraceResult.hitVec.z);
                }

                hitmarkers.add(new HitmarkerData(hitCoord, this.life.getValue(), MODE.COD, this.hitmarkerCOD, this));
                lastPacket = packet;
            }
        }
    }
}
