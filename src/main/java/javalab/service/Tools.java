package javalab.service;

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
}
