package me.vaxry.harakiri.framework.util;

import java.util.regex.Pattern;

/**
 * @author noil
 */
public class RegexUtil {

    // thanks @wine for the regex pattern
    public static final Pattern BY_CAPITAL = Pattern.compile("(?=\\p{Lu})");
}
