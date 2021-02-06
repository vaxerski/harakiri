package me.vaxry.harakiri.framework.event.gui.hud;

/**
 * @author noil
 */
public class EventHubComponentClick {

    public String hubComponentName;
    public boolean hubComponentVisible;

    public EventHubComponentClick(String hubComponentName, boolean hubComponentVisible) {
        this.hubComponentName = hubComponentName;
        this.hubComponentVisible = hubComponentVisible;
    }
}
