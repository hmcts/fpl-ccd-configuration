package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public class SubmittedFormFilenameHelper {

    private SubmittedFormFilenameHelper() {
        // NO-OP
    }

    public static String buildFileName(CaseDetails caseDetails) {
        String caseName = Strings.nullToEmpty((String) caseDetails.getData().get("caseName")).trim();

        if (!Strings.isNullOrEmpty(caseName)) {
            return caseName.replaceAll("\\s", "_") + ".pdf";
        }

        return caseDetails.getId() + ".pdf";
    }
}
