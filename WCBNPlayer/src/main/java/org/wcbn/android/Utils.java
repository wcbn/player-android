package org.wcbn.android;

import org.wcbn.android.station.WCBNStation;

/**
 * Misc. Utilities
 */
public class Utils {

    private static final Class<? extends Station> STATION_CLASS = WCBNStation.class;

    /**
     * Get the Station
     */
    public static Class<? extends Station> getStation() {
        return STATION_CLASS;
    }

    /**
     * Capitalizes the first letter of every word in a String, as in a title.
     *
     * @param string string to process
     * @return string with beginning of every word capitalized
     */
    public static String capitalizeTitle(String string) {
        if(string == null)
            return null;

        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
