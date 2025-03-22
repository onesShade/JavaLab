package javalab.utility;

import java.util.Optional;

public class Tools {

    private Tools() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<Integer> tryParseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Long> tryParseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}