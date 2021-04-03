package me.vaxry.harakiri.impl.module.misc;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class ExtraTabModule extends Module {

    public final Value<Boolean> detailsV = new Value<Boolean>("HighlightPlayers", new String[]{"Highlight", "HighlightPlayers"}, "Highlights you and friends.", false);

    public ExtraTabModule(){
        super("ExtraTab", new String[]{"ExtraTab"}, "Changes the tab menu behavior.", "NONE", -1, ModuleType.MISC);
    }
}
