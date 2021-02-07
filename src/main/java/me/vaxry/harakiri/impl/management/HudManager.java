package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.event.render.EventRender2D;
import me.vaxry.harakiri.framework.gui.hud.component.HudComponent;
import me.vaxry.harakiri.framework.module.Module;
import me.vaxry.harakiri.framework.util.ReflectionUtil;
import me.vaxry.harakiri.framework.value.Value;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.gui.hud.anchor.AnchorPoint;
import me.vaxry.harakiri.impl.gui.hud.component.*;
import me.vaxry.harakiri.impl.gui.hud.component.module.ModuleListComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.MinecraftForge;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Author Seth
 * 7/25/2019 @ 6:20 AM.
 */
public final class HudManager {

    private List<HudComponent> componentList = new CopyOnWriteArrayList<>();
    private List<AnchorPoint> anchorPoints = new ArrayList<>();

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

        RireworksFemovedComponent rfc = new RireworksFemovedComponent();

        add(new PlexusComponent());
        add(new WatermarkComponent());
        add(new ArrayListComponent(TOP_RIGHT)); // creates the enabled mods component & by default anchors in the top right (to aid new users)
        add(new TpsComponent());
        add(new PotionEffectsComponent());
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
        add(new EnemyPotionsComponent());
        add(new HubComponent());
        add(new SwitchViewComponent());
        add(new InventoryComponent());
        add(new TotemCountComponent());
        add(new PlayerCountComponent());
        add(new EntityListComponent());

        MinecraftForge.EVENT_BUS.register(rfc);
        //MinecraftForge.EVENT_BUS.register(new ThreatCamComponent());

        NotificationsComponent notificationsComponent = new NotificationsComponent();
        notificationsComponent.setAnchorPoint(TOP_CENTER);
        add(notificationsComponent);

        this.loadExternalHudComponents();

        // Organize alphabetically
        this.componentList = this.componentList.stream().sorted((obj1, obj2) -> obj1.getName().compareTo(obj2.getName())).collect(Collectors.toList());

        Harakiri.INSTANCE.getEventManager().addEventListener(this);
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
    }

    public void loadExternalHudComponents() {
        try {
            final File dir = new File("harakiri/Hud");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (Class clazz : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (clazz != null) {
                    if (HudComponent.class.isAssignableFrom(clazz)) {
                        final HudComponent component = (HudComponent) clazz.newInstance();
                        this.componentList.add(component);
                        Harakiri.INSTANCE.getLogger().log(Level.INFO, "Found external hud component " + component.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        Harakiri.INSTANCE.getEventManager().removeEventListener(this);
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

    public void setComponentList(List<HudComponent> componentList) {
        this.componentList = componentList;
    }
}
