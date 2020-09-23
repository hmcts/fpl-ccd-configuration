package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

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
    static final String ALLOCATED_JUDGE_EMAIL_ADDRESS = "judge@gmail.com";
    static final String COURT_CODE = "11";
    static final String CTSC_INBOX = "Ctsc+test@gmail.com";
    static final String PARTY_ADDED_TO_CASE_BY_EMAIL_ADDRESS = "barney@rubble.com";
    static final String PARTY_ADDED_TO_CASE_THROUGH_DIGITAL_SERVICE_EMAIL = "fred@flinstone.com";
    static final byte[] DOCUMENT_CONTENTS = {1, 2, 3, 4, 5};

    private NotificationEventHandlerTestData() {
    }

    public static AllocatedJudgeTemplate getExpectedAllocatedJudgeNotificationParameters() {
        AllocatedJudgeTemplate allocatedJudgeTemplate = new AllocatedJudgeTemplate();
        allocatedJudgeTemplate.setJudgeTitle("Her Honour Judge");
        allocatedJudgeTemplate.setJudgeName("Moley");
        allocatedJudgeTemplate.setCaseName("test");
        allocatedJudgeTemplate.setCaseUrl("http://fake-url/cases/case-details/12345");

        return allocatedJudgeTemplate;
    }

    public static Map<String, Object> expectedCommonCMONotificationParameters() {
        String subjectLine = "Lastname, SACCCCCCCC5676576567";
        return ImmutableMap.of("subjectLineWithHearingDate", subjectLine,
            "reference", "12345",
            "caseUrl", String.format("null/case/%s/%s/12345", JURISDICTION, CASE_TYPE));
    }

    public static ImmutableMap<String, Object> getCMOIssuedCaseLinkNotificationParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", LOCAL_AUTHORITY_NAME)
            .putAll(expectedCommonCMONotificationParameters())
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

    public static List<Representative> expectedRepresentatives() {
        return ImmutableList.of(Representative.builder()
            .email("abc@example.com")
            .fullName("Jon Snow")
            .servingPreferences(DIGITAL_SERVICE)
            .build());
    }

    public static List<Representative> getExpectedEmailRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("barney@rubble.com")
                .fullName("Barney Rubble")
                .servingPreferences(EMAIL)
                .build());
    }

    public static List<Representative> getExpectedDigitalRepresentativesForAddingPartiesToCase() {
        return ImmutableList.of(
            Representative.builder()
                .email("fred@flinstone.com")
                .fullName("Fred Flinstone")
                .servingPreferences(DIGITAL_SERVICE)
                .build());
    }
}
