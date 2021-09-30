package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.BooleanUtils;

public class BooleanHelper {
    private BooleanHelper() {
        // NO-OP
    }

    public static String booleanToYesNo(boolean value) {
        return BooleanUtils.toString(value, "Yes", "No");
    }
}
