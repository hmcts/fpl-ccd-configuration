package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Splitter;

public class CaseDetailsHelper {
    private CaseDetailsHelper() {
        // NO OP
    }

    public static String formatCCDCaseNumber(Long caseNumber) {
        return String.join("-", Splitter.fixedLength(4).splitToList(String.valueOf(caseNumber)));
    }
}
