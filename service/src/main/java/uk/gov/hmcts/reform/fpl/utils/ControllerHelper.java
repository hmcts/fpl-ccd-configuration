package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public class ControllerHelper {

    private ControllerHelper() {
    }

    public static void removeTemporaryFields(CaseDetails caseDetails, String... fields) {
        for (String field : fields) {
            caseDetails.getData().remove(field);
        }
    }
}
