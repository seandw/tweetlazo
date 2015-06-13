package org.cognoseed.tweetlazo;

import java.text.Normalizer;

/**
 * A collection of String processing utilities.
 */
public class StringUtils {

    /**
     * This class isn't meant to be constructed.
     */
    private StringUtils() {}

    /**
     * Strips out adjacent duplicate characters.
     */
    public static String stripDuplicateAdjacents(String srcStr) {
        if (srcStr == null || srcStr.length() == 0) return "";
        String str = srcStr.toLowerCase();

        StringBuilder buf = new StringBuilder();
        char last = str.charAt(0);
        buf.append(last);
        for (int idx = 1; idx < str.length(); ++idx) {
            char curr = str.charAt(idx);
            if (last != curr) {
                last = curr;
                buf.append(last);
            }
        }
        return buf.toString();
    }

    /**
     * Strips punctuation and accents.
     */
    public static String stripPunctuation(String srcStr) {
        if (srcStr == null) return "";
        String normalized = Normalizer.normalize(srcStr, Normalizer.Form.NFD);
        return normalized.replaceAll("(?U)[\\p{Punct}\\p{InCombiningDiacriticalMarks}]", "");
    }
}
