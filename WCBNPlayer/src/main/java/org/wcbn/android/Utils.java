package org.wcbn.android;

import org.wcbn.android.station.Station;
import org.wcbn.android.station.wcbn.WCBNStation;

/**
 * Misc. Utilities
 */
public class Utils {

    // Hard code the Station we're using here.
    private static final Station STATION = new WCBNStation();

    /**
     * Get the Station
     */
    public static Station getStation() {
        return STATION;
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
            }
            else if(Character.isDigit(chars[i])) {
                found = true;
            }
            else if (Character.isWhitespace(chars[i]) || chars[i]=='.') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
