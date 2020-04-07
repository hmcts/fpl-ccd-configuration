package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Representative;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

public class NotificationEventHandlerTestData {
    static final String LOCAL_AUTHORITY_CODE = "example";
    static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";
    static final String COURT_EMAIL_ADDRESS = "admin@family-court.com";
    static final String COURT_NAME = "Family Court";
    static final String AUTH_TOKEN = "Bearer token";
    static final String CAFCASS_EMAIL_ADDRESS = "FamilyPublicLaw+cafcass@gmail.com";
    static final String CAFCASS_NAME = "cafcass";
    static final String GATEKEEPER_EMAIL_ADDRESS = "FamilyPublicLaw+gatekeeper@gmail.com";
    static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+sa@gmail.com";
    static final String COURT_CODE = "11";
    static final String CTSC_INBOX = "Ctsc+test@gmail.com";
    static final String PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS = "barney@rubble.com";
    static final String PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL = "fred@flinstone.com";
    static final byte[] DOCUMENT_CONTENTS = {1, 2, 3, 4, 5};
    static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusMonths(3);

    private NotificationEventHandlerTestData() {
    }

    public static CallbackRequest appendSendToCtscOnCallback() {
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

    public static CallbackRequest buildCallbackRequest() {
        return CallbackRequest.builder()
            .caseDetails(buildCaseDetailsWithRepresentatives())
            .build();
    }

    public static CaseDetails buildCaseDetailsWithRepresentatives() {
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

    public static List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
    }

    public static Map<String, Object> getExpectedOrderNotificationParameters() {
        String fileContent = new String(Base64.encodeBase64(DOCUMENT_CONTENTS), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        String subjectLine = "Jones, SACCCCCCCC5676576567";

        return Map.of("callOut",
            (subjectLine + ", hearing " + formatLocalDateToString(FUTURE_DATE.toLocalDate(), FormatStyle.MEDIUM)),
            "courtName", COURT_NAME,
            "orderType", "Blank order (C21)",
            "reference", "12345",
            "caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345",
            "attachedDocumentLink", jsonFileObject);
    }
}
