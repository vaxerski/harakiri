package me.vaxry.harakiri.impl.module.hidden;

import me.vaxry.harakiri.framework.Module;
import me.vaxry.harakiri.framework.Value;

public class ArrayListModule extends Module {

    public final Value<ArrayListModule.Mode> mode = new Value<ArrayListModule.Mode>("Sorting", new String[]{"Sorting", "sort"}, "Changes arraylist sorting method.", ArrayListModule.Mode.LENGTH);
    public final Value<Boolean> lowercase = new Value<Boolean>("Lowercase", new String[]{"Lower", "case", "undercase", "nocap"}, "NO CAP.", false);
    public final Value<Boolean> showMetadata = new Value<Boolean>("Metadata", new String[]{"ShowMetadata", "suffix", "showsuffix"}, "Shows the metadata of the module if it exists.", true);

    public ArrayListModule() {
        super("ArrayList", new String[]{"ArrayList", "arraylist", "modulelist", "modlist", "array-list", "alist"}, "Optional values for the ArrayList hud component.", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
    }

    public enum Mode {
        LENGTH, ALPHABET, UNSORTED;
    }
}
