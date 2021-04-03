package me.vaxry.harakiri.framework;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.util.MathUtil;
import me.vaxry.harakiri.framework.util.Timer;
import me.vaxry.harakiri.impl.gui.hud.component.NotificationsComponent;

public final class Notification {

    private final String title;

    private String text;

    private float x = 0, y = 0, width = 0, height = 0;

    private final Type type;

    private int duration; // milliseconds

    private final int maxDuration;

    private float transitionX = 0, transitionY = 0;

    public float alpha = 0;

    public float percFrac = 0;
    public float percFracBack = 0;

    private final Timer timer = new Timer();
    private final Timer jittertimer = new Timer();

    public Notification(String title, String text, Type type, int duration) {
        this.title = title;
        this.text = text;
        this.type = type;
        this.duration = duration;
        this.maxDuration = duration;

        final NotificationsComponent notificationsComponent = (NotificationsComponent) Harakiri.get().getHudManager().findComponent(NotificationsComponent.class);
        if (notificationsComponent != null) {
            this.transitionX = 0;
            this.transitionY = 0;
            this.setX(notificationsComponent.getX());
            this.setY(notificationsComponent.getY());
            this.alpha = 0;
        }

        this.timer.reset();
        this.jittertimer.reset();
    }

    public Notification(String title, String text) {
        this(title, text, Type.INFO, 3000);
    }

    private float getJitter() {
        final float seconds = ((System.currentTimeMillis() - this.jittertimer.getTime()) / 1000.0f) % 60.0f;

        final float desiredTimePerSecond = 1;

        this.jittertimer.reset();
        return 1 / Math.min(desiredTimePerSecond * seconds, 1.0f);
    }

    public void update() {
        int incline = 8;
        //this.transitionY = (float) MathUtil.parabolic(this.transitionY, this.y, incline);

        //if(this.timer.passed(this.duration - 1000)){
        //    this.alpha = Math.min(255.f, this.alpha -4);
        //}
        final float jitter = 1;//getJitter();

        if(this.timer.passed(this.duration - 1000)){
            this.percFrac = (float) MathUtil.parabolic(this.percFrac, 0, incline * jitter);
            this.percFracBack = (float) MathUtil.parabolic(this.percFracBack, 0, incline * jitter * 1.3f);
            this.percFrac = Math.max(this.percFrac, 0);
            this.percFracBack = Math.max(this.percFracBack, 0);
        }else if(this.percFrac < 1){
            this.percFrac = (float) MathUtil.parabolic(this.percFrac, 1.0f, incline * jitter * 1.3f);
            this.percFracBack = (float) MathUtil.parabolic(this.percFracBack, 1.015f, incline * jitter);
            this.percFracBack = Math.min(this.percFracBack, 1.03f);
            this.percFrac = Math.min(this.percFrac, 1);
        }else if(this.percFrac >= 1){
            this.percFrac = 1;
            this.percFracBack = 1.03f;
        }

        if (this.timer.passed((this.duration))) {
            Harakiri.get().getNotificationManager().removeNotification(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Type getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public float getTransitionX() {
        return transitionX;
    }

    public float getTransitionY() {
        return transitionY;
    }

    public enum Type {
        INFO(0xFF909090), SUCCESS(0xFF10FF10), WARNING(0xFFFFFF10), ERROR(0xFFFF1010), QUESTION(0xFF10FFFF), MISC(0xFFFFFFFF);

        private int color;

        Type(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }
}
