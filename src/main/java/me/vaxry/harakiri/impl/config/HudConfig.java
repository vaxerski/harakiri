package me.vaxry.harakiri.impl.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.Configurable;
import me.vaxry.harakiri.framework.gui.DraggableHudComponent;
import me.vaxry.harakiri.framework.gui.HudComponent;
import me.vaxry.harakiri.framework.util.FileUtil;
import me.vaxry.harakiri.framework.gui.anchor.AnchorPoint;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;

public final class HudConfig extends Configurable {

    class HudConfigJSON{
        public HudConfigJSON(String name, float x, float y, float w, float h, boolean vis, boolean lock, String anch, String stick, String stickside){
            this.name = name;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.vis = vis;
            this.lock = lock;
            this.anch = anch;
            this.stick = stick;
            this.stickside = stickside;
        }
        public String name;
        public float x;
        public float y;
        public float w;
        public float h;
        public boolean vis;
        public boolean lock;
        public String anch;
        public String stick;
        public String stickside;
    }

    public HudConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "hud"), "hud.json");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        String rawdata = loadRawFile();
        if(rawdata.equalsIgnoreCase(""))
            return;

        Gson gson = new Gson();

        //JsonObject obj = gson.fromJson(rawdata, JsonObject.class);
        JsonReader reader = new JsonReader(new StringReader(rawdata));
        reader.setLenient(true);

        // Retrieve array
        HudConfigJSON[] hudConfigJSONS = gson.fromJson(reader, HudConfigJSON[].class);

        for(HudConfigJSON settings : hudConfigJSONS) {

            if(settings == null)
                continue;

            HudComponent component = Harakiri.get().getHudManager().findComponent(settings.name);

            if(component == null)
                continue;

            component.setX(settings.x);
            component.setY(settings.y);
            component.setW(settings.w);
            component.setH(settings.h);
            component.setVisible(settings.vis);

            if(component instanceof DraggableHudComponent){
                // its dragebl xdxd
                DraggableHudComponent dhc = (DraggableHudComponent)component;

                // Anchor
                dhc.setAnchorPoint(null);
                try {
                    if (!settings.anch.equalsIgnoreCase("na")) {
                        for (AnchorPoint anchorPoint : Harakiri.get().getHudManager().getAnchorPoints()) {
                            if (anchorPoint.getPoint().equals(AnchorPoint.Point.valueOf(settings.anch))) {
                                dhc.setAnchorPoint(anchorPoint);
                            }
                        }
                    }
                }catch (Throwable t){ }

                // Stick
                try {
                    dhc.setGlued(null);
                    if (!settings.stick.equalsIgnoreCase("na")) {
                        dhc.setGlued((DraggableHudComponent) Harakiri.get().getHudManager().findComponent(settings.stick));
                    }
                }catch (Throwable t){ }

                // StickSide
                try {
                    dhc.setGlueSide(null);
                    if (!settings.stickside.equalsIgnoreCase("na")) {
                        dhc.setGlueSide(DraggableHudComponent.GlueSide.valueOf(settings.stickside));
                    }
                }catch (Throwable t){ }
            }
        }
    }

    @Override
    public void onSave() {
        JsonObject componentsListJsonObject = new JsonObject();

        String strObj = "";

        ArrayList<HudConfigJSON> hudConfigJSONS = new ArrayList<>();

        for(HudComponent component : Harakiri.get().getHudManager().getComponentList()) {
            if(component instanceof DraggableHudComponent) {
                DraggableHudComponent draggableHudComponent = (DraggableHudComponent)component;
                hudConfigJSONS.add(new HudConfigJSON(
                        draggableHudComponent.getName(),
                        draggableHudComponent.getX(),
                        draggableHudComponent.getY(),
                        draggableHudComponent.getW(),
                        draggableHudComponent.getH(),
                        draggableHudComponent.isVisible(),
                        draggableHudComponent.isLocked(),
                        draggableHudComponent.getAnchorPoint() == null ? "na" : draggableHudComponent.getAnchorPoint().getPoint().name(),
                        draggableHudComponent.getGlued() == null ? "na" : draggableHudComponent.getGlued().getName(),
                        draggableHudComponent.getGlueSide() == null ? "na" : draggableHudComponent.getGlueSide().name()
                ));
            }else{
                hudConfigJSONS.add(new HudConfigJSON(
                        component.getName(),
                        component.getX(),
                        component.getY(),
                        component.getW(),
                        component.getH(),
                        component.isVisible(),
                        false,
                        "na",
                        "na",
                        "na"
                ));
            }
        }

        strObj = new Gson().toJson(hudConfigJSONS);

        this.saveStringToFile(strObj);
        //this.saveJsonObjectToFile(componentsListJsonObject);
    }
}
