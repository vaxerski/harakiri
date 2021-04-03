package me.vaxry.harakiri.framework.event.module;

import me.vaxry.harakiri.framework.Module;

/**
 * Author Seth
 * 6/10/2019 @ 2:36 PM.
 */
public class EventModuleLoad {

    private Module mod;

    public EventModuleLoad(Module mod) {
        this.mod = mod;
    }

    public Module getMod() {
        return mod;
    }

    public void setMod(Module mod) {
        this.mod = mod;
    }
}
