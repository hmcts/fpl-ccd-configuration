package uk.gov.hmcts.reform.fpl.handlers;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.CmoNotifyData;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;

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

    public static AllocatedJudgeTemplateForCMO getCMOReadyForJudgeNotificationParameters() {
        CmoNotifyData commonCMONotificationParameters = expectedCommonCMONotificationParameters();

        return AllocatedJudgeTemplateForCMO.builder()
            .subjectLineWithHearingDate(commonCMONotificationParameters.getSubjectLineWithHearingDate())
            .caseUrl(commonCMONotificationParameters.getCaseUrl())
            .reference(commonCMONotificationParameters.getReference())
            .respondentLastName("Smith")
            .judgeTitle("Her Honour Judge")
            .judgeName("Moley")
            .build();
    }

    public static AllocatedJudgeTemplate getExpectedAllocatedJudgeNotificationParameters() {
        return AllocatedJudgeTemplate.builder()
            .judgeTitle("Her Honour Judge")
            .judgeName("Moley")
            .caseName("test")
            .caseUrl("http://fake-url/cases/case-details/12345")
            .build();
    }

    public static CmoNotifyData expectedCommonCMONotificationParameters() {
        return CmoNotifyData.builder()
            .subjectLineWithHearingDate("Lastname, SACCCCCCCC5676576567")
            .reference("12345")
            .caseUrl(String.format("null/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .build();
    }

    public static CmoNotifyData getCMOIssuedCaseLinkNotificationParameters() {
        return CmoNotifyData.builder()
            .subjectLineWithHearingDate("Lastname, SACCCCCCCC5676576567")
            .reference("12345")
            .caseUrl(String.format("null/case/%s/%s/12345", JURISDICTION, CASE_TYPE))
            .localAuthorityNameOrRepresentativeFullName(LOCAL_AUTHORITY_NAME)
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
