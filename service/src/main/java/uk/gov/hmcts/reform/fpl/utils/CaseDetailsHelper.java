package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Splitter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;

import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.RETURNED;

public class CaseDetailsHelper {
    private CaseDetailsHelper() {
        // NO OP
    }

    public static String formatCCDCaseNumber(Long caseNumber) {
        String ccdCaseNumber = String.valueOf(caseNumber);

        if (ccdCaseNumber.length() != 16) {
            throw new IllegalArgumentException("CCD Case number must be 16 digits long");
        }

        return String.join("-", Splitter.fixedLength(4).splitToList(ccdCaseNumber));
    }

    public static void removeTemporaryFields(CaseDetails caseDetails, String... fields) {
        for (String field : fields) {
            caseDetails.getData().remove(field);
        }
    }

    public static void removeTemporaryFields(CaseDetailsMap caseDetailsMap, String... fields) {
        for (String field : fields) {
            caseDetailsMap.remove(field);
        }
    }

    public static boolean isInOpenState(CaseDetails caseDetails) {
        return isInState(caseDetails, OPEN);
    }

    public static boolean isInReturnedState(CaseDetails caseDetails) {
        return isInState(caseDetails, RETURNED);
    }

    public static boolean isInGatekeepingState(CaseDetails caseDetails) {
        return isInState(caseDetails, GATEKEEPING);
    }

    private static boolean isInState(CaseDetails caseDetails, State state) {
        return state.getValue().equals(caseDetails.getState());
    }
}
