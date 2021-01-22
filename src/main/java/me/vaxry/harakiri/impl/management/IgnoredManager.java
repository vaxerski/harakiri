package me.vaxry.harakiri.impl.management;

import me.vaxry.harakiri.api.ignore.Ignored;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author Seth
 * 6/29/2019 @ 4:52 AM.
 */
public final class IgnoredManager {

    private List<Ignored> ignoredList = new CopyOnWriteArrayList<>();

    public void add(String name) {
        this.ignoredList.add(new Ignored(name));
        //harakiri.INSTANCE.getConfigManager().saveAll();
    }

    public Ignored find(String name) {
        for (Ignored ignored : this.ignoredList) {
            if (ignored.getName().equalsIgnoreCase(name)) {
                return ignored;
            }
        }
        return null;
    }

    public void unload() {
        this.ignoredList.clear();
    }

    public List<Ignored> getIgnoredList() {
        return ignoredList;
    }

    public void setIgnoredList(List<Ignored> ignoredList) {
        this.ignoredList = ignoredList;
    }
}
