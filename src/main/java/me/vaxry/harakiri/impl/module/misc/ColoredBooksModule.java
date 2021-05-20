package me.vaxry.harakiri.impl.module.misc;

import io.github.vialdevelopment.attendance.attender.Attender;
import me.vaxry.harakiri.framework.event.gui.EventBookPage;
import me.vaxry.harakiri.framework.event.gui.EventBookTitle;
import me.vaxry.harakiri.framework.Module;


public final class ColoredBooksModule extends Module {

    public ColoredBooksModule() {
        super("BookColor", new String[]{"BookColor", "BookColors", "cbooks", "cbook"}, "Allows you to use the & character in books.", "NONE", -1, ModuleType.MISC);
    }

    Attender<EventBookPage> onAddPage = new Attender<>(EventBookPage.class, event -> event.setPage(event.getPage().replace("&", "\247")));
    Attender<EventBookTitle> onEditTitle = new Attender<>(EventBookTitle.class, event -> event.setTitle(event.getTitle().replace("&", "\247")));

}
