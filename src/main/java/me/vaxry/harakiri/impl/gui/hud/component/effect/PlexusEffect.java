package me.vaxry.harakiri.impl.gui.hud.component.effect;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.RenderUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.gui.hud.GuiHudEditor;
import me.vaxry.harakiri.impl.gui.hud.component.PlexusComponent;
import me.vaxry.harakiri.impl.module.hidden.PlexusModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import javax.vecmath.Vector2f;
import java.util.ArrayList;

class PlexusParticle {

    // ---SETTINGS--- //
    private float PARTICLE_SPEED_MIN = 20.f; // px/s
    private float PARTICLE_SPEED_MAX = 60.f; // px/s
    private int PARTICLE_MAX_OFFSCREEN = 0;
    private int PARTICLE_SIZE_MIN = 2;
    private int PARTICLE_SIZE_MAX = 3;
    private float PARTICLE_SIZE_SCALE = 0.5f;

    public PlexusParticle(){ }

    public float speed;
    public float x;
    public float y;
    public float size;
    public Vector2f speedVec;

    public void calculateMove(float jitter) {
        this.x += jitter * speedVec.x * speed;
        this.y += jitter * speedVec.y * speed;
    }

    public void calcNewRandomParams(ScaledResolution res) {
        int x = (int)(-PARTICLE_MAX_OFFSCREEN + (float)Math.random() * (res.getScaledWidth() + 2*PARTICLE_MAX_OFFSCREEN));
        int y = (int)(-PARTICLE_MAX_OFFSCREEN + (float)Math.random() * (res.getScaledHeight() + 2*PARTICLE_MAX_OFFSCREEN));
        float speed = PARTICLE_SPEED_MIN + (float)Math.random() * (PARTICLE_SPEED_MAX + PARTICLE_SPEED_MIN);
        int size = (int)(PARTICLE_SIZE_MIN + (float)Math.random() * (PARTICLE_SIZE_MAX - PARTICLE_SIZE_MIN));

        float vec1 = (float)Math.random() * 2 - 1;
        float vec2 = (float)Math.random() * 2 - 1;

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
        this.speedVec = new Vector2f(vec1, vec2);
    }

    public void updateSettings(float PARTICLE_SPEED_MINv, float PARTICLE_SPEED_MAXv, int PARTICLE_MAX_OFFSCREENv, float PARTICLE_SIZE_SCALEv) {
        PARTICLE_SPEED_MIN = PARTICLE_SPEED_MINv;
        PARTICLE_SPEED_MAX = PARTICLE_SPEED_MAXv;
        PARTICLE_MAX_OFFSCREEN = PARTICLE_MAX_OFFSCREENv;
        PARTICLE_SIZE_SCALE = PARTICLE_SIZE_SCALEv;
    }

    public void calcNewRandomOffscreenParams(ScaledResolution res) {
        int x, y;

        if(Math.random() > 0.5f){
            x = (int)(PARTICLE_MAX_OFFSCREEN * Math.random());
            x += Math.random() > 0.66f ? -PARTICLE_MAX_OFFSCREEN : res.getScaledWidth();
            y = (int)(-PARTICLE_MAX_OFFSCREEN + (float)Math.random() * (res.getScaledHeight() + 2*PARTICLE_MAX_OFFSCREEN));
        }else{
            y = (int)(PARTICLE_MAX_OFFSCREEN * Math.random());
            y += Math.random() > 0.66f ? -PARTICLE_MAX_OFFSCREEN : res.getScaledHeight();
            x = (int)(-PARTICLE_MAX_OFFSCREEN + (float)Math.random() * (res.getScaledWidth() + 2*PARTICLE_MAX_OFFSCREEN));
        }

        float speed = PARTICLE_SPEED_MIN + (float)Math.random() * (PARTICLE_SPEED_MAX + PARTICLE_SPEED_MIN);
        int size = (int)(PARTICLE_SIZE_MIN + (float)Math.random() * (PARTICLE_SIZE_MAX - PARTICLE_SIZE_MIN));

        float vec1 = x < 0 ? (float)Math.random() : (float)Math.random() - 1;
        float vec2 = y < 0 ? (float)Math.random() : (float)Math.random() - 1;

        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
        this.speedVec = new Vector2f(vec1, vec2);
    }

    public boolean isOutOfBounds(ScaledResolution res) {
        return (this.x < -PARTICLE_MAX_OFFSCREEN || this.x > res.getScaledWidth() + PARTICLE_MAX_OFFSCREEN) || (this.y < -PARTICLE_MAX_OFFSCREEN || this.y > res.getScaledHeight() + PARTICLE_MAX_OFFSCREEN);
    }

    public void drawParticle(int color) {
        RenderUtil.drawCircle(this.x, this.y, (int)(this.size * this.PARTICLE_SIZE_SCALE), color);
    }

    public void processClick(int mx, int my, float force, int maxdist, float dist) {

        Vector2f spV = scaleVec(new Vector2f(this.x - mx, this.y - my), (maxdist - dist) * 0.1f);

        this.speedVec.x += spV.x * force;
        this.speedVec.y += spV.y * force;

        this.speedVec = normalizeVec(this.speedVec);

        this.speed += force;
    }

    private Vector2f scaleVec(Vector2f vec, float scale) {
        vec.x *= scale;
        vec.y *= scale;
        return vec;
    }

    private Vector2f normalizeVec(Vector2f in) {
        if(in.x > in.y) {
            in.y /= Math.abs(in.x);
            in.x /= Math.abs(in.x);
        } else {
            in.x /= Math.abs(in.y);
            in.y /= Math.abs(in.y);
        }
        return in;
    }
}

public class PlexusEffect {

    // ---SETTINGS--- //
    private int PARTICLE_NUM = 100;
    private int LINE_MAX_DIST = 200;
    private int LINE_MAX_ALPHA = 75;
    private int RAINBOW_SIZE = 200;
    private int CLICK_MAX_RANGE = 50;
    private float CLICK_FORCE = 0.5f;

    private ArrayList<PlexusParticle> particles = new ArrayList<>();
    private Timer timer = new Timer();
    private boolean init = true;

    private int rainbowColor = 0xFF000000;
    private boolean mouseClick = false;

    private PlexusComponent plexusComponent;

    public PlexusEffect(PlexusComponent pc) {
        plexusComponent = pc;
    }

    public void render(int mouseX, int mouseY) {
        // No super.render because I don't want default bg and stuff here.

        // redundant, but i like security.
        if(!(Minecraft.getMinecraft().currentScreen instanceof GuiHudEditor))
            return;
        if(!plexusComponent.isVisible()) {
            mouseClick = false;
            return;
        }

        // -------------------------- //

        final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());

        // Particles

        //    INIT    //
        if(init){
            for(int i = 0; i < PARTICLE_NUM; ++i){
                particles.add(new PlexusParticle());
                particles.get(i).calcNewRandomParams(res);
            }
            init = false;
        }

        // CHECK SETTINGS //
        final PlexusModule plexusModule = (PlexusModule) Harakiri.get().getModuleManager().find(PlexusModule.class);
        if(plexusModule != null){
            if(PARTICLE_NUM != plexusModule.PARTICLE_NUM.getValue() || LINE_MAX_DIST != plexusModule.LINE_MAX_DIST.getValue() || LINE_MAX_ALPHA != plexusModule.LINE_MAX_ALPHA.getValue() || RAINBOW_SIZE != plexusModule.RAINBOW_SIZE.getValue()) {
                LINE_MAX_DIST = plexusModule.LINE_MAX_DIST.getValue();
                LINE_MAX_ALPHA = plexusModule.LINE_MAX_ALPHA.getValue();
                RAINBOW_SIZE = plexusModule.RAINBOW_SIZE.getValue();
                PARTICLE_NUM = plexusModule.PARTICLE_NUM.getValue();

                init = true;
                particles.clear();
                return;
            }
        }

        rainbowColor = Harakiri.get().getHudManager().rainbowColor;

        //     CALCULATE MOVEMENT     //

        final float jitter = (((System.currentTimeMillis() - this.timer.getTime()) / 1000.0f) % 60.0f);
        timer.reset();

        for(PlexusParticle p : particles) {
            p.calculateMove(jitter);
            if(p.isOutOfBounds(res))
                p.calcNewRandomOffscreenParams(res);

            p.updateSettings(plexusModule.PARTICLE_SPEED_MIN.getValue(), plexusModule.PARTICLE_SPEED_MAX.getValue(), plexusModule.PARTICLE_MAX_OFFSCREEN.getValue(), plexusModule.PARTICLE_SIZE_SCALE.getValue());

            // update movement if click.
            if(mouseClick){
                // doesnt work well
                //final float dist = get2DDistance(mouseX, mouseY, p.x, p.y);
                //if(dist < CLICK_MAX_RANGE)
                    //p.processClick(mouseX, mouseY, Math.min((CLICK_MAX_RANGE - dist) * 0.1f, 3) * CLICK_FORCE, CLICK_MAX_RANGE, dist);
            }
        }

        //     DRAW     //

        for(PlexusParticle p : particles) {
            // first, draw the particle.
            final float distToMouse = get2DDistance(p.x, p.y, mouseX, mouseY);
            final boolean isPinRangeOfRainbow = distToMouse < RAINBOW_SIZE;
            if(!isPinRangeOfRainbow)
                p.drawParticle(0x88FFFFFF);
            else
                p.drawParticle(rainbowColor);

            // then, draw the lines.
            for(PlexusParticle p2 : particles) {
                if(p == p2) continue;

                if(Math.abs(p2.x - p.x) > LINE_MAX_DIST || Math.abs(p2.y - p.y) > LINE_MAX_DIST) continue;

                // line is drawn, lets check for rainbow.

                final boolean isP2inRangeOfRainbow = get2DDistance(p2.x, p2.y, mouseX, mouseY) < RAINBOW_SIZE;

                final int alpha = getAlphaFromDist(get2DDistance(p.x, p.y, p2.x, p2.y));

                int colorP  = ((int)((float)0xFF * ((float)alpha / 255.f)) * 0x1000000) + 0xFFFFFF;
                int colorP2 = ((int)((float)0xFF * ((float)alpha / 255.f)) * 0x1000000) + 0xFFFFFF;

                if(isPinRangeOfRainbow)
                    colorP = ((int)((float)0xFF * ((float)alpha / 255.f)) * 0x1000000) + (rainbowColor - 0xFF000000);

                if(isP2inRangeOfRainbow)
                    colorP2 = ((int)((float)0xFF * ((float)alpha / 255.f)) * 0x1000000) + (rainbowColor - 0xFF000000);


                //Harakiri.get().logChat("Alpha: " + String.format("0x%08X", colore));

                RenderUtil.drawLine2(p.x, p.y, p2.x, p2.y, 1.0f, colorP, colorP2);//(int)((float)0xFF000000 * ((float)alpha / 255.f)) + 0xFFFFFF);
            }
        }

        mouseClick = false;
    }

    private float get2DDistance(float x, float y, float x1, float y1) {
        return (float)(Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2)));
    }

    private int getAlphaFromDist(float distance) {
        if(distance > LINE_MAX_DIST) return 0;
        return Math.min((int)(LINE_MAX_ALPHA * (LINE_MAX_DIST - distance)/(float)LINE_MAX_DIST), LINE_MAX_ALPHA);
    }

    public void onMouseClicked() {
        mouseClick = true;
    }
}
