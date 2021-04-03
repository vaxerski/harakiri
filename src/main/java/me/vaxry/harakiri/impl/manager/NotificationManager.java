package me.vaxry.harakiri.impl.manager;

import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.framework.animation.Animation;
import me.vaxry.harakiri.framework.Notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotificationManager implements Animation {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public NotificationManager() {
        Harakiri.get().getAnimationManager().addAnimation(this);
    }

    public void update() {
        for (Notification notification : getNotifications()) {
            notification.update();
        }
    }

    public void unload() {
        this.notifications.clear();
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    public void addNotification(String title, String text, Notification.Type type, int duration) {
        this.notifications.add(new Notification(title, text, type, duration));
    }

    public void addNotification(String title, String text) {
        this.notifications.add(new Notification(title, text));
    }

    public void removeNotification(Notification notification) {
        if (this.notifications.contains(notification)) {
            this.notifications.remove(notification);
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
