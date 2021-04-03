package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.minecraft.EventDisplayGui;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.gui.HudComponent;
import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.framework.Value;
import me.vaxry.harakiri.framework.gui.anchor.AnchorPoint;
import me.vaxry.harakiri.impl.gui.hud.component.*;
import me.vaxry.harakiri.impl.gui.hud.component.special.HubComponent;
import me.vaxry.harakiri.impl.gui.hud.component.special.ModuleListComponent;
import me.vaxry.harakiri.impl.gui.hud.component.special.ModuleSearchComponent;
import me.vaxry.harakiri.impl.gui.screen.HaraGuiChat;
import me.vaxry.harakiri.impl.module.ui.HudEditorModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final class HudManager {

    private List<HudComponent> componentList = new CopyOnWriteArrayList<>();
    private List<AnchorPoint> anchorPoints = new ArrayList<>();

    private Timer timer = new Timer();
    float rainSpeed = 0.1f; // +0.1 default
    public int rainbowColor = 0x00000000;
    private float hue = 0;

    public HudManager() {
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        final AnchorPoint TOP_LEFT = new AnchorPoint(AnchorPoint.Point.TOP_LEFT);
        final AnchorPoint TOP_RIGHT = new AnchorPoint(AnchorPoint.Point.TOP_RIGHT);
        final AnchorPoint BOTTOM_LEFT = new AnchorPoint(AnchorPoint.Point.BOTTOM_LEFT);
        final AnchorPoint BOTTOM_RIGHT = new AnchorPoint(AnchorPoint.Point.BOTTOM_RIGHT);
        final AnchorPoint TOP_CENTER = new AnchorPoint(AnchorPoint.Point.TOP_CENTER);
        final AnchorPoint BOTTOM_CENTER = new AnchorPoint(AnchorPoint.Point.BOTTOM_CENTER);
        this.anchorPoints.add(TOP_LEFT);
        this.anchorPoints.add(TOP_RIGHT);
        this.anchorPoints.add(BOTTOM_LEFT);
        this.anchorPoints.add(BOTTOM_RIGHT);
        this.anchorPoints.add(TOP_CENTER);
        this.anchorPoints.add(BOTTOM_CENTER);

        for (AnchorPoint anchorPoint : this.anchorPoints)
            anchorPoint.updatePosition(sr);

        int moduleListXOffset = 0;
        int moduleListYOffset = 0;
        for (Module.ModuleType type : Module.ModuleType.values()) {
            if (type.equals(Module.ModuleType.HIDDEN) || type.equals(Module.ModuleType.UI))
                continue;

            final ModuleListComponent moduleList = new ModuleListComponent(type);
            if ((moduleList.getX() + moduleListXOffset) > sr.getScaledWidth()) {
                moduleListXOffset = 0;
                moduleListYOffset += moduleList.getH() + 4 /* gap above and below each column */;
            }

            moduleList.setX(moduleList.getX() + moduleListXOffset);
            if (moduleListYOffset != 0) {
                moduleList.setY(moduleList.getY() + moduleListYOffset);
            }

            add(moduleList);

            moduleListXOffset += moduleList.getW() + 4 /* gap between each list */;
        }

        final ModuleSearchComponent moduleSearchComponent = new ModuleSearchComponent();
        add(moduleSearchComponent);

        RireworksFemovedComponent rfc = new RireworksFemovedComponent();

        add(new PlexusComponent());
        add(new WatermarkComponent());
        add(new TpsComponent());
        add(new FpsComponent());
        add(new CoordsComponent());
        add(new NetherCoordsComponent());
        add(new SpeedComponent());
        add(new ThreatComponent());
        add(new ThreatCamComponent());
        add(new WaifuComponent());
        add(new ArmorComponent());
        add(new PingComponent());
        add(new ServerBrandComponent());
        add(new DirectionComponent());
        add(new PacketTimeComponent());
        add(rfc);
        add(new TimeComponent());
        add(new HubComponent());
        add(new SwitchViewComponent());
        add(new InventoryComponent());
        add(new TotemCountComponent());
        add(new PlayerCountComponent());
        add(new EntityListComponent());
        add(new WarningsComponent());
        add(new BPSComponent());
        add(new CombatInfoComponent());

        MinecraftForge.EVENT_BUS.register(rfc);
        //MinecraftForge.EVENT_BUS.register(new ThreatCamComponent());

        ArrayListComponent arrayListComponent = new ArrayListComponent(TOP_RIGHT);
        arrayListComponent.setAnchorPoint(TOP_RIGHT);
        add(arrayListComponent);
        NotificationsComponent notificationsComponent = new NotificationsComponent();
        notificationsComponent.setAnchorPoint(TOP_LEFT);
        add(notificationsComponent);

        //this.loadExternalHudComponents();

        // Organize alphabetically
        this.componentList = this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());

        Harakiri.get().getEventManager().addEventListener(this);
    }

    /**
     * Find all fields within the hud component that are values
     * and add them to the list of values inside of the hud component
     *
     * @param component the HudComponent to add
     */
    public void add(HudComponent component) {
        try {
            for (Field field : component.getClass().getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    final Value val = (Value) field.get(component);
                    component.getValueList().add(val);
                }
            }
            this.componentList.add(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update our anchor point positions when we render
     *
     * @param event
     */
    @Listener
    public void onRender(EventRender2D event) {
        final Minecraft mc = Minecraft.getMinecraft();

        final int chatHeight = (mc.currentScreen instanceof GuiChat) ? 14 : 0;

        for (AnchorPoint point : this.anchorPoints) {
            if (point.getPoint() == AnchorPoint.Point.TOP_LEFT) {
                point.setX(2);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.TOP_RIGHT) {
                point.setX(event.getScaledResolution().getScaledWidth() - 2);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_LEFT) {
                point.setX(2);
                point.setY(event.getScaledResolution().getScaledHeight() - chatHeight - 2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_RIGHT) {
                point.setX(event.getScaledResolution().getScaledWidth() - 2);
                point.setY(event.getScaledResolution().getScaledHeight() - chatHeight - 2);
            }
            if (point.getPoint() == AnchorPoint.Point.TOP_CENTER) {
                point.setX(event.getScaledResolution().getScaledWidth() / 2.0f);
                point.setY(2);
            }
            if (point.getPoint() == AnchorPoint.Point.BOTTOM_CENTER) {
                point.setX(event.getScaledResolution().getScaledWidth() / 2.0f);
                point.setY(event.getScaledResolution().getScaledHeight() - 2);
            }
        }

        HudEditorModule hudmodule = (HudEditorModule) Harakiri.get().getModuleManager().find(HudEditorModule.class);
        rainSpeed = hudmodule.rainspeed.getValue();

        // Shift RGB

        final float jitter = getJitter();

        hue += jitter;
        if(hue > 1)
            hue -= 1;

        Color rainbowColorC = Color.getHSBColor(hue, 1, 1);
        rainbowColor = 0xFF000000 + rainbowColorC.getRed() * 0x10000 + rainbowColorC.getGreen() * 0x100 + rainbowColorC.getBlue();
    }

    @Listener
    public void onGuiScreenOpen(EventDisplayGui eventDisplayGui){
        if(eventDisplayGui.getScreen() instanceof GuiChat && !(eventDisplayGui.getScreen() instanceof HaraGuiChat) && !(eventDisplayGui.getScreen() instanceof GuiSleepMP)) {
            eventDisplayGui.setCanceled(true);
            eventDisplayGui.getScreen().onGuiClosed();
            Minecraft.getMinecraft().displayGuiScreen(new HaraGuiChat());
        }
    }

    public void moveToTop(HudComponent component) {
        for (HudComponent comp : this.componentList) {
            if (comp != null && comp == component) {
                this.componentList.remove(comp);
                this.componentList.add(comp);
                break;
            }
        }
    }

    public void unload() {
        this.anchorPoints.clear();
        this.componentList.clear();
        Harakiri.get().getEventManager().removeEventListener(this);
    }

    public AnchorPoint findPoint(AnchorPoint.Point point) {
        for (AnchorPoint anchorPoint : this.anchorPoints) {
            if (anchorPoint.getPoint() == point) {
                return anchorPoint;
            }
        }
        return null;
    }

    public HudComponent findComponent(String componentName) {
        for (HudComponent component : this.componentList) {
            if(componentName == null || component.getName() == null)
                continue;
            if (componentName.equalsIgnoreCase(component.getName())) {
                return component;
            }
        }
        return null;
    }

    public HudComponent findComponent(Class componentClass) {
        for (HudComponent component : this.componentList) {
            if (component.getClass() == componentClass) {
                return component;
            }
        }
        return null;
    }

    public List<AnchorPoint> getAnchorPoints() {
        return anchorPoints;
    }

    public void setAnchorPoints(List<AnchorPoint> anchorPoints) {
        this.anchorPoints = anchorPoints;
    }

    public List<HudComponent> getComponentList() {
        return this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = rainSpeed;

        this.timer.reset();
        return Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    public void setComponentList(List<HudComponent> componentList) {
        this.componentList = componentList;
    }
}
