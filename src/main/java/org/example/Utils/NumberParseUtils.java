package org.example.Utils;

/**
 * Parse số an toàn cho tầng Service (không phụ thuộc Swing).
 */
public final class NumberParseUtils {

    private NumberParseUtils() {
    }

    public static int parseInt(String s, int defaultValue) {
        if (s == null || s.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float parseFloat(String s, float defaultValue) {
        if (s == null || s.isBlank()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
