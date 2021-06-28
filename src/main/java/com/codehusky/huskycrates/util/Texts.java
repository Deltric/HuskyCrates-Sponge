package com.codehusky.huskycrates.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Texts {

    private static final Pattern pattern;
    private static final Pattern patternUnformatted;
    private static final String HEX_PATTERN = (char) 167 + "#([A-Fa-f0-9]{6})";
    private static final String HEX_PATTERN_UNFORMATTED = "#([A-Fa-f0-9]{6})";

    static {
        pattern = Pattern.compile(HEX_PATTERN);
        patternUnformatted = Pattern.compile(HEX_PATTERN_UNFORMATTED);
    }

    /**
     * Translates a message into a colored message
     * @param message - Message to color
     * @return Colored message
     */
    public static Text of(String message) {
        return TextSerializers.FORMATTING_CODE.deserialize(formatHex(message));
    }

    /**
     * Is this String a hexadecimal color code.
     * @param text - Text to check
     * @return Hex color code or not
     */
    public static boolean isHex(String text) {
        Matcher matcher = pattern.matcher((char) 167 + text);
        return matcher.matches();
    }

    /**
     * Strips hex colors from the text
     * @param text - Text to strip
     * @return Stripped text
     */
    public static String stripHex(String text) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()) {
            matcher.appendReplacement(buf, "");
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    /**
     * Looks for hex colors and formats with color symbol
     * @param text - Text to format
     * @return Formatted string
     */
    public static String formatHex(String text) {
        StringBuffer buf = new StringBuffer();
        Matcher matcher = patternUnformatted.matcher(text);
        while(matcher.find()) {
            if(matcher.start(1) != -1) {
                matcher.appendReplacement(buf, (char) 167 + "#" + matcher.group(1));
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

}