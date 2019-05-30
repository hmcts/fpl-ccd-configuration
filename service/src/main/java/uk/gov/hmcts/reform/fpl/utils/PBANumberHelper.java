package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PBANumberHelper {

    private static final Pattern PBA_NUMBER_REGEX = Pattern.compile("[0-9]{7}");

    private static final Pattern PBA_AT_START_REGEX = Pattern.compile("^PBA");

    private static final String PBA_NUMBER_CCD_KEY = "pbaNumber";

    private static final String PBA_NUMBER_FIELD_ERROR = "Payment by account (PBA) number must include 7 numbers";

    private PBANumberHelper () {
        // NO-OP
    }

    public static String updatePBANumber(CaseDetails caseDetails) {
        String pbaNumberData = (String) caseDetails.getData().get(PBA_NUMBER_CCD_KEY);

        Matcher startsWithPBA = PBA_AT_START_REGEX.matcher(pbaNumberData);
        if (!startsWithPBA.matches()) {
            return "PBA" + pbaNumberData;
        } else {
            return pbaNumberData;
        }
    }

    public static boolean validatePBANumber(){
        return true;

    }
}
