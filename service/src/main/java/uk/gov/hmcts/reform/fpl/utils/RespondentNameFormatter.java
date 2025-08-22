package uk.gov.hmcts.reform.fpl.utils;

import java.util.List;

public class RespondentNameFormatter {
    private RespondentNameFormatter() {}

    public static String formatRespondentNames(List<String> names) {
        return switch (names.size()) {
            case 1 -> names.get(0) + " is";
            case 2 -> names.get(0) + " and " + names.get(1) + " are";
            default -> String.join(", ", names.subList(0, names.size() - 1))
                    + " and " + names.get(names.size() - 1) + " are";
        };
    }
}
