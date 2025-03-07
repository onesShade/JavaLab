package javalab.utility;

public class Tools {

    private Tools() {
        throw new IllegalStateException("Utility class");
    }

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return (long) -1;
        }
    }
}