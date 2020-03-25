package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

public class NotificationEventHandlerTestData {
    static final String LOCAL_AUTHORITY_CODE = "example";
    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
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
    static final byte[] DOCUMENT_CONTENTS = {1, 2, 3};

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

    public static ImmutableMap<String, Object> getCMOIssuedCaseLinkNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", LOCAL_AUTHORITY_NAME)
            .putAll(expectedCommonCMONotificationParameters())
            .build();
    }

    public static CallbackRequest buildCallbackRequest() throws IOException {
        return CallbackRequest.builder()
            .caseDetails(buildCaseDetailsWithRepresentatives())
            .build();
    }

    public static CaseDetails buildCaseDetailsWithRepresentatives() throws IOException {
        CaseDetails caseDetails = callbackRequest().getCaseDetails();
        Map<String, Object> caseData = caseDetails.getData();

        caseData.put("representatives", createRepresentatives(DIGITAL_SERVICE));
        return caseDetails.toBuilder()
            .data(caseData)
            .build();
    }

    public static CaseData buildCaseDataWithRepresentatives() {
        return CaseData.builder()
            .representatives(createRepresentatives(DIGITAL_SERVICE))
            .build();
    }

    public static List<Representative> expectedRepresentatives() {
        return ImmutableList.of(Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(DIGITAL_SERVICE)
            .build());
    }

    public static Map<String, Object> getExpectedCMOIssuedCaseLinkNotificationParametersForRepresentative() {
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", "Jon Snow")
            .putAll(expectedCommonCMONotificationParameters())
            .build();
    }
}
