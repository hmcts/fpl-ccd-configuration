package uk.gov.hmcts.reform.fpl.utils;

import java.util.List;

public class RespondentNameFormatter {
    private RespondentNameFormatter() {}

    public static String formatRespondentNames(List<String> names) {
        if (names.size() == 1) {
            return names.get(0) + " is";
        }
        if (names.size() == 2) {
            return names.get(0) + " and " + names.get(1) + " are";
        }
        return String.join(", ", names.subList(0, names.size() - 1) + " and "
            + names.get(names.size() - 1) + " are");
    }
}
