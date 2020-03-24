package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

public class NotificationEventHandlerTestData {
    static final String LOCAL_AUTHORITY_CODE = "example";
    static final String COURT_EMAIL_ADDRESS = "admin@family-court.com";
    static final String COURT_NAME = "Test court";
    static final String AUTH_TOKEN = "Bearer token";
    static final String USER_ID = "1";
    static final String CAFCASS_EMAIL_ADDRESS = "FamilyPublicLaw+cafcass@gmail.com";
    static final String CAFCASS_NAME = "cafcass";
    static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    static final String COURT_CODE = "11";
    static final String CTSC_INBOX = "Ctsc+test@gmail.com";

    private NotificationEventHandlerTestData() {
    }

    public static CallbackRequest appendSendToCtscOnCallback() throws IOException {
        CallbackRequest callbackRequest = callbackRequest();
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        Map<String, Object> updatedCaseData = ImmutableMap.<String, Object>builder()
            .putAll(caseDetails.getData())
            .put("sendToCtsc", "Yes")
            .build();

        caseDetails.setData(updatedCaseData);
        callbackRequest.setCaseDetails(caseDetails);

        return callbackRequest;
    }

    public static ImmutableMap<String, Object> getCMOReadyForJudgeNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .putAll(expectedCommonCMONotificationParameters())
            .build();
    }

    public static Map<String, Object> expectedCommonCMONotificationParameters() {
        String subjectLine = "Lastname, SACCCCCCCC5676576567";
        return ImmutableMap.of("subjectLineWithHearingDate", subjectLine,
            "reference", "12345",
            "caseUrl", String.format("null/case/%s/%s/12345", JURISDICTION, CASE_TYPE));
    }

    public static Map<String, Object> getCMORejectedCaseLinkNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("requestedChanges", "Please make these changes XYZ")
            .putAll(expectedCommonCMONotificationParameters())
            .build();
    }
}
