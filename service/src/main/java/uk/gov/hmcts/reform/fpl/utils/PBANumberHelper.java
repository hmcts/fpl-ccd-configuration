package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBANumberHelper {

    private static final Pattern PBA_NUMBER_REGEX = Pattern.compile("[0-9]{7}");

    private static final String PBA_NUMBER_FIELD_ERROR = "Payment by account (PBA) number must include 7 numbers";

    private PBANumberHelper() {
        // NO-OP
    }

    public static String updatePBANumber(String pbaNumber) {
        if (pbaNumber.startsWith("PBA")) {
            return pbaNumber;
        } else if (pbaNumber.startsWith("pba")) {
            return pbaNumber.replace("pba", "PBA");
        } else {
            return "PBA" + pbaNumber;
        }
    }

    public static List<String> validatePBANumber(String pbaNumber) {
        ImmutableList.Builder<String> pbaNumberErrors = ImmutableList.builder();

        String remaining = pbaNumber.substring(3);
        Matcher sevenDigits = PBA_NUMBER_REGEX.matcher(remaining);

        if (!sevenDigits.matches()) {
            pbaNumberErrors.add(PBA_NUMBER_FIELD_ERROR);
        }

        return pbaNumberErrors.build();
    }

}
