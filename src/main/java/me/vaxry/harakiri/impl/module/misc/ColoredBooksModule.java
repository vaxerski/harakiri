package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.event.gui.EventBookPage;
import me.vaxry.harakiri.framework.event.gui.EventBookTitle;
import me.vaxry.harakiri.framework.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/16/2019 @ 8:07 AM.
 */
public final class ColoredBooksModule extends Module {

    public ColoredBooksModule() {
        super("BookColor", new String[]{"BookColor", "BookColors", "cbooks", "cbook"}, "Allows you to use the & character in books.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void addPage(EventBookPage event) {
        event.setPage(event.getPage().replace("&", "\247"));
    }

    @Listener
    public void editTitle(EventBookTitle event) {
        event.setTitle(event.getTitle().replace("&", "\247"));
    }

}
