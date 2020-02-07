package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Splitter;

public class CaseDetailsHelper {
    private CaseDetailsHelper() {
        // NO OP
    }

    public static String formatCCDCaseNumber(Long caseNumber) {
        String ccdCaseNumber = String.valueOf(caseNumber)
            
        if (ccdCaseNumber.length() != 16) {
            throw new IllegalArgumentException("CCD Case number must be 16 digits long");
        }
        
        return String.join("-", Splitter.fixedLength(4).splitToList(ccdCaseNumber));
    }
}
