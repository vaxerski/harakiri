package me.vaxry.harakiri.impl.gui.hud.component;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.camera.Camera2;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.module.hidden.ThreatCamModule;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3d;

public class ThreatCamComponent extends DraggableHudComponent {

    private final Camera2 threatCamera = new Camera2(640, 360);
    private float distance = 1.2f;

    private boolean isCamCreated = false;
    private Entity lastE = null;
    private boolean firstTime = true;

    public ThreatCamComponent() {
        super("ThreatCam (alpha)");
        Harakiri.get().getCameraManager().addCamera2(threatCamera);
        this.setW(160);
        this.setH(90);

        Harakiri.get().getEventManager().addEventListener(this);
    }

    // todo: fix Future ESP
    // todo: make yourself rendered in ThreatCam

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor) {
            Harakiri.get().getTTFFontUtil().drawStringWithShadow("ThreatCam (alpha)", this.getX() + this.getW() / 2 - Harakiri.get().getTTFFontUtil().getStringWidth("ThreatCam (alpha)") / 2, this.getY() + this.getH() / 2 - Harakiri.get().getTTFFontUtil().FONT_HEIGHT / 2, 0x99FFFFFF);
            return;
        }

        final ThreatCamModule threatcammodule = (ThreatCamModule) Harakiri.get().getModuleManager().find(ThreatCamModule.class);
        this.distance = threatcammodule.distance.getValue();

        if (mc.player == null || mc.world == null) return;
        if (getThreat() == null) {
            if(this.isCamCreated){
                threatCamera.destroyCamEntity();
                isCamCreated = false;
                lastE = getThreat();
            }
            return;
        }

        final EntityPlayer threat = getThreat();

        if(!isCamCreated) {
            threatCamera.createCamEntity(threat);
            isCamCreated = true;
        }

        threatCamera.updateCamEntity(threat);

        RenderUtil.drawRect(this.getX() - 1, this.getY() - 1, this.getX() + this.getW() + 1, this.getY() + this.getH() + 1, 0x99101010);
        RenderUtil.drawRect(this.getX(), this.getY(), this.getX() + this.getW(), this.getY() + this.getH(), 0xFF202020);
        Harakiri.get().getTTFFontUtil().drawStringWithShadow(this.getName(), this.getX() + 2, this.getY() + 2, 0xFFFFFFFF);

        this.threatCamera.setRendering(true);

        this.threatCamera.setOptimized(threatcammodule.optimized.getValue());

        if (this.threatCamera.isValid()) {
            this.threatCamera.setPos(getCameraPos(threat));
            this.threatCamera.setYaw(threat.getPitchYaw().y);
            this.threatCamera.setYawHead(threat.rotationYawHead);
            this.threatCamera.setPitch(threat.rotationPitch);
            this.threatCamera.render(this.getX() + 2, this.getY() + 12, this.getX() + this.getW() - 2, this.getY() + this.getH() - 2);
        }

        if(firstTime) {
            threatCamera.destroyCamEntity();
            firstTime = false;
            isCamCreated = false;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        // not really needed anymoar
    }

    private EntityPlayer getThreat(){

        float bestdist = 999999999.0f;
        EntityPlayer threat = null;

        for(Entity e : mc.world.loadedEntityList){
            if(e instanceof EntityPlayer){
                if(get3DDistance((EntityPlayer)e) < bestdist && e.getUniqueID() != mc.player.getUniqueID() && e.getEntityId() != 420420421 && e.getEntityId() != 420420420 /* FakeLag Fake Ent */){
                    bestdist = get3DDistance((EntityPlayer)e);
                    threat = (EntityPlayer)e;
                }
            }
        }

        return threat;
    }

    private int get3DDistance(EntityPlayer e) {
        return (int)(Math.sqrt(Math.pow((mc.player.posX - e.posX),2) + Math.pow((mc.player.posY - e.posY),2) + Math.pow((mc.player.posZ - e.posZ),2)));
    }

    private Vec3d getCameraPos(EntityPlayer e) {

        Vec3d entityPos = e.getPositionEyes(0);
        Vec3d entityAng = new Vec3d(Math.toRadians(e.rotationPitch), Math.toRadians(e.rotationYaw), 0 /* Roll 0 */);

        Vector3d result = new Vector3d(entityPos.x, entityPos.y, entityPos.z);

        result.x += distance * Math.cos(entityAng.x) * Math.sin(entityAng.y);
        result.y += distance * Math.sin(entityAng.x) /*- e.eyeHeight*/;
        result.z += -distance * Math.cos(entityAng.x) * Math.cos(entityAng.y);

        return new Vec3d(result.x, result.y, result.z);
    }
}
