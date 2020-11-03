package uk.gov.hmcts.reform.fpl.util;

public class StringUtils {

    private StringUtils() {
    }

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String BLUE = "\033[0;34m";

    public static String blue(String message) {
        return color(BLUE, message);
    }

    public static String red(String message) {
        return color(RED, message);
    }

    private static String color(String color, String message) {
        return String.format("%s%s%s", color, message, RESET);
    }

}
