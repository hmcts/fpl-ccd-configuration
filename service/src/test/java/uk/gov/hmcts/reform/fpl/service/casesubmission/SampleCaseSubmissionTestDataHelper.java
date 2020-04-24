package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.generateCourtSealEncodedString;

public class SampleCaseSubmissionTestDataHelper {
    static final String BASE_64 = "image:base64:%1$s";

    static final String APPLICANT_ORGANISATIONS_KEY = "applicantOrganisations";
    static final String RESPONDENT_NAMES_KEY = "respondentNames";
    static final String ORDERS_NEEDED_KEY = "ordersNeeded";
    static final String DIRECTIONS_NEEDED_KEY = "directionsNeeded";
    static final String HEARING_KEY = "hearing";
    static final String HEARING_TYPE_AND_REASON_KEY = "typeAndReason";
    static final String HEARING_TIME_FRAME_KEY = "timeFrame";
    static final String HEARING_WITHOUT_NOTICE_DETAILS_KEY = "withoutNoticeDetails";
    static final String HEARING_REDUCED_NOTICED_DETAILS_KEY = "reducedNoticeDetails";
    static final String HEARING_RESPONDENTS_AWARE_KEY = "respondentsAware";
    static final String HEARING_RESPONDENTS_AWARE_REASON_KEY = "respondentsAwareReason";
    static final String ALLOCATION_KEY = "allocation";
    static final String PROPOSAL_KEY = "proposal";
    static final String PROPOSAL_REASON_KEY = "proposalReason";
    static final String ALLOCATION_PROPOSAL_PRESENT_KEY = "allocationProposalPresent";
    static final String JUDGE_LEVEL_RADIO_KEY = "judgeLevelRadio";
    static final String HEARING_DATE_PREFERENCES_KEY = "hearingPreferences";
    static final String INTERPRETER_KEY = "interpreter";
    static final String WELSH_DETAILS_KEY = "welshDetails";
    static final String INTERMEDIARY_KEY = "intermediary";
    static final String DISABILITY_ASSISTANCE_KEY = "disabilityAssistance";
    static final String EXTRA_SECURITY_MEASURES_KEY = "extraSecurityMeasures";
    static final String SOMETHING_ELSE_KEY = "somethingElse";
    static final String INTERNATIONAL_ELEMENT_KEY = "internationalElement";
    static final String POSSIBLE_CARER_KEY = "possibleCarer";
    static final String SIGNIFICANT_EVENTS_KEY = "significantEvents";
    static final String PROCEEDINGS_KEY = "proceedings";
    static final String INTERNATIONAL_AUTHORITY_INVOLVEMENT_KEY = "internationalAuthorityInvolvement";
    static final String ISSUES_KEY = "issues";
    static final String COURT_SEAL_KEY = "courtseal";
    static final String USER_FULL_NAME_KEY = "userFullName";
    static final String SUBMITTED_DATE_KEY = "submittedDate";
    static final String GROUNDS_FOR_EPO_REASON_KEY = "groundsForEPOReason";
    static final String GROUNDS_THRESHOLD_REASON_KEY = "groundsThresholdReason";
    static final String THRESHOLD_DETAILS_KEY = "thresholdDetails";
    static final String RISKS_KEY = "risks";
    static final String EMOTIONAL_HARM_DETAILS_KEY = "emotionalHarmDetails";
    static final String PHYSICAL_HARM_DETAILS_KEY = "physicalHarmDetails";
    static final String SEXUAL_ABUSE_DETAILS_KEY = "sexualAbuseDetails";
    static final String NEGLECT_DETAILS_KEY = "neglectDetails";
    static final String FACTORS_PARENTING_KEY = "factorsParenting";
    static final String ALCOHOL_DRUG_ABUSE_DETAILS_KEY = "alcoholDrugAbuseDetails";
    static final String DOMESTIC_VIOLENCE_DETAILS_KEY = "domesticViolenceDetails";
    static final String ANYTHING_ELSE_KEY = "anythingElse";
    static final String RESPONDENTS_KEY = "respondents";
    static final String APPLICANTS_KEY = "applicants";
    static final String CHILDREN_KEY = "children";
    static final String OTHERS_KEY = "others";
    static final String PROCEEDING_KEY = "proceeding";
    static final String ANNEX_DOCUMENTS_KEY = "annexDocuments";
    static final String SAME_GUARDIAN_DETAILS_KEY = "sameGuardianDetails";
    static final String SAME_GUARDIAN_KEY = "sameGuardianNeeded";
    static final String GUARDIAN_KEY = "guardian";
    static final String JUDGE_KEY = "judge";
    static final String ORDERS_MADE_KEY = "ordersMade";
    static final String ENDED_KEY = "ended";
    static final String STARTED_KEY = "started";
    static final String CASE_NUMBER_KEY = "caseNumber";
    static final String PROCEEDING_STATUS_KEY = "proceedingStatus";
    static final String ON_GOING_PROCEEDING_KEY = "onGoingProceeding";
    static final String OTHER_SOCIAL_WORK_DOCUMENTS_KEY = "otherSocialWorkDocuments";
    static final String CHECKLIST_DOCUMENT_KEY = "checklistDocument";
    static final String THRESHOLD_DOCUMENT_KEY = "thresholdDocument";
    static final String SOCIAL_WORK_EVIDENCE_TEMPLATE_KEY = "socialWorkEvidenceTemplate";
    static final String SOCIAL_WORK_CARE_PLAN_KEY = "socialWorkCarePlan";
    static final String SOCIAL_WORK_ASSESSMENT_KEY = "socialWorkAssessment";
    static final String SOCIAL_WORK_STATEMENT_KEY = "socialWorkStatement";
    static final String SOCIAL_WORK_CHRONOLOGY_KEY = "socialWorkChronology";
    static final String DRAFT_WATERMARK_KEY = "draftWaterMark";
    static final String ORGANISATION_NAME = "organisationName";
    static final String CONTACT_NAME = "contactName";
    static final String JOB_TITLE = "jobTitle";
    static final String ADDRESS = "address";
    static final String EMAIL = "email";
    static final String MOBILE_NUMBER = "mobileNumber";
    static final String TELEPHONE_NUMBER = "telephoneNumber";
    static final String PBA_NUMBER = "pbaNumber";
    static final String SOLICITOR_NAME = "solicitorName";
    static final String SOLICITOR_MOBILE = "solicitorMobile";
    static final String SOLICITOR_TELEPHONE = "solicitorTelephone";
    static final String SOLICITOR_EMAIL = "solicitorEmail";
    static final String SOLICITOR_DX = "solicitorDx";
    static final String SOLICITOR_REFERENCE = "solicitorReference";
    static final String NAME = "name";
    static final String AGE = "age";
    static final String GENDER = "gender";
    static final String DATE_OF_BIRTH = "dateOfBirth";

    private SampleCaseSubmissionTestDataHelper() {
    }

    public static Map<String, Object> expectedDCaseSubmissionTemplateData() throws IOException {
        Map<String, Object> templateData = Maps.newHashMap();

        templateData.put(RESPONDENTS_KEY, expectedRespondentsTemplateData());
        templateData.put(APPLICANTS_KEY, expectedApplicantsTemplateData());
        templateData.put(CHILDREN_KEY, expectedChildrenTemplateData());
        templateData.put(OTHERS_KEY, expectedOthersTemplateData());
        templateData.put(APPLICANT_ORGANISATIONS_KEY, "London Borough of Southwark");
        templateData.put(RESPONDENT_NAMES_KEY, "Paul Smith\nJames Smith\nAn Other");
        templateData.put(ORDERS_NEEDED_KEY, "Emergency protection order");
        templateData.put(DIRECTIONS_NEEDED_KEY, "Contact with any named personYes\n");
        templateData.put(HEARING_KEY, expectedHearingTemplateData());
        templateData.put(ALLOCATION_KEY, expectedAllocationTemplateData());
        templateData.put(HEARING_DATE_PREFERENCES_KEY, expectedHearingDatePreferenceTemplateData());
        templateData.put(INTERNATIONAL_ELEMENT_KEY, expectedInternationElementTemplateData());
        templateData.put(COURT_SEAL_KEY, format(BASE_64, generateCourtSealEncodedString()));
        templateData.put(DRAFT_WATERMARK_KEY, null);
        templateData.put(USER_FULL_NAME_KEY, "Professor");
        templateData.put(SUBMITTED_DATE_KEY, now().format(ofPattern("d MMMM yyyy")));
        templateData.put(GROUNDS_FOR_EPO_REASON_KEY, "There’s reasonable cause to believe the child is likely to suffer"
            + " significant harm if they’re not moved to accommodation provided by you, or on your behalf\n"
            + "There’s reasonable cause to believe the child is likely to suffer significant harm if they don’t "
            + "stay in their current accommodation");
        templateData.put(GROUNDS_THRESHOLD_REASON_KEY, "Not receiving care that would be reasonably expected "
            + "from a parent.\nBeyond parental control.\n");
        templateData.put(THRESHOLD_DETAILS_KEY, "grounds for application criteria");
        templateData.put(RISKS_KEY, expectedRisksTemplateData());
        templateData.put(FACTORS_PARENTING_KEY, expectedFactorsParentingTemplateData());
        templateData.put(PROCEEDING_KEY, expectedProceedingTemplateData());
        templateData.put(ANNEX_DOCUMENTS_KEY, expectedAnnexDocumentsTemplateData());

        return templateData;
    }

    private static List<Map<String, String>> expectedRespondentsTemplateData() {
        return List.of(Map.ofEntries(entry(NAME, "Paul Smith"),
            entry(AGE, "75 years 11 months 22 days old"),
            entry(GENDER, "75 years 11 months 22 days old"),
            entry(DATE_OF_BIRTH, "75 years 11 months 22 days old")));
    }

    private static List<Map<String, String>> expectedApplicantsTemplateData() {
        return List.of(Map.ofEntries(entry(ORGANISATION_NAME, "London Borough of Southwark"),
            entry(CONTACT_NAME, "Jane Smith"),
            entry(JOB_TITLE, "Legal adviser"),
            entry(ADDRESS, "160 Tooley St\nTooley road\nTooley\nLimerick\nGalway\nSE1 2QH\nIreland"),
            entry(EMAIL, "jane@smith.com"),
            entry(MOBILE_NUMBER, "2020202020"),
            entry(TELEPHONE_NUMBER, "02120202020"),
            entry(PBA_NUMBER, "PBA1234567"),
            entry(SOLICITOR_NAME, "Brian Banks"),
            entry(SOLICITOR_MOBILE, "7665545327"),
            entry(SOLICITOR_TELEPHONE, "020 2772 5772"),
            entry(SOLICITOR_EMAIL, "brian@banks.com"),
            entry(SOLICITOR_DX, "106 Southwark &"),
            entry(SOLICITOR_REFERENCE, "12345")));
    }

    private static Map<String, String> expectedChildrenTemplateData() {
        return null;
    }

    private static Map<String, String> expectedOthersTemplateData() {
        return null;
    }

    public static Map<String, String> expectedHearingTemplateData() {
        return Map.of(HEARING_TYPE_AND_REASON_KEY, "Contested interim care order\nStandard case management hearing",
            HEARING_TIME_FRAME_KEY, "Same day\nBaby will be discharged from hospital on 21 June 2018",
            HEARING_WITHOUT_NOTICE_DETAILS_KEY, "Yes\nNotice without hearing needed",
            HEARING_REDUCED_NOTICED_DETAILS_KEY, "Yes\nBaby needs to be discharged from the hospital",
            HEARING_RESPONDENTS_AWARE_KEY, "Yes",
            HEARING_RESPONDENTS_AWARE_REASON_KEY, "They seek to care for baby in mother and baby unit");
    }

    public static Map<String, String> expectedAllocationTemplateData() {
        return new HashMap<>() {{
                put(PROPOSAL_KEY, "Section 9 circuit judge");
                put(PROPOSAL_REASON_KEY, "allocation proposal reason");
                put(ALLOCATION_PROPOSAL_PRESENT_KEY, null);
                put(JUDGE_LEVEL_RADIO_KEY, null);
            }};
    }

    public static Map<String, String> expectedHearingDatePreferenceTemplateData() {
        return Map.of(INTERPRETER_KEY, "Yes\ninterpreter required",
            WELSH_DETAILS_KEY, "Yes\nwelsh proceedings",
            INTERMEDIARY_KEY, "Yes\nintermediary hearing required",
            DISABILITY_ASSISTANCE_KEY, "Yes\nlearning disability",
            EXTRA_SECURITY_MEASURES_KEY, "Yes\nsecurity needed",
            SOMETHING_ELSE_KEY, "Yes\nI need this from someone");
    }

    public static Map<String, String> expectedInternationElementTemplateData() {
        return ImmutableMap.<String, String>builder()
            .put(POSSIBLE_CARER_KEY, "Yes\nAunt outside UK")
            .put(SIGNIFICANT_EVENTS_KEY, "Yes\nSomething happened in France")
            .put(PROCEEDINGS_KEY, "Yes\nOutside of the UK")
            .put(INTERNATIONAL_AUTHORITY_INVOLVEMENT_KEY, "Yes\nFrench authorities were involved")
            .put(ISSUES_KEY, "Yes\nBrussels 2 regulation")
            .build();
    }

    public static Map<String, String> expectedRisksTemplateData() {
        return ImmutableMap.<String, String>builder()
            .put(NEGLECT_DETAILS_KEY, "Yes\nPast harm\nFuture risk of harm")
            .put(SEXUAL_ABUSE_DETAILS_KEY, "Yes\nPast harm\nFuture risk of harm")
            .put(PHYSICAL_HARM_DETAILS_KEY, "Yes\nPast harm\nFuture risk of harm")
            .put(EMOTIONAL_HARM_DETAILS_KEY, "Yes\nPast harm\nFuture risk of harm")
            .build();
    }

    private static Map<String, String> expectedFactorsParentingTemplateData() {
        return ImmutableMap.<String, String>builder()
            .put(ANYTHING_ELSE_KEY, "No")
            .put(ALCOHOL_DRUG_ABUSE_DETAILS_KEY, "Yes\nhistory of drug abuse")
            .put(DOMESTIC_VIOLENCE_DETAILS_KEY, "Yes\nhistory of domestic violence")
            .build();
    }

    private static Map<String, String> expectedProceedingTemplateData() {
        return Map.ofEntries(entry(ON_GOING_PROCEEDING_KEY, "Yes"),
            entry(PROCEEDING_STATUS_KEY, "Ongoing"),
            entry(CASE_NUMBER_KEY, "12345"),
            entry(STARTED_KEY, "02-02-2002"),
            entry(ENDED_KEY, "02-02-2006"),
            entry(ORDERS_MADE_KEY, "Supervision order"),
            entry(JUDGE_KEY, "William Peters"),
            entry(CHILDREN_KEY, "children subject to proceedings"),
            entry(GUARDIAN_KEY, "Mark Watson"),
            entry(SAME_GUARDIAN_KEY, "No"),
            entry(SAME_GUARDIAN_DETAILS_KEY, "Some guardian not needed"));
    }

    private static Map<String, Object> expectedAnnexDocumentsTemplateData() {
        return Map.of(SOCIAL_WORK_CHRONOLOGY_KEY, "To follow\nSocial work chronology text",
            SOCIAL_WORK_STATEMENT_KEY, "To follow\nSocial work statement and genogram text",
            SOCIAL_WORK_ASSESSMENT_KEY, "To follow\nSocial work assessment text",
            SOCIAL_WORK_CARE_PLAN_KEY, "To follow\nsome text",
            SOCIAL_WORK_EVIDENCE_TEMPLATE_KEY, "Attached",
            THRESHOLD_DOCUMENT_KEY, "Attached",
            CHECKLIST_DOCUMENT_KEY, "Attached",
            OTHER_SOCIAL_WORK_DOCUMENTS_KEY, asList("document_one", "document_two"));
    }
}
