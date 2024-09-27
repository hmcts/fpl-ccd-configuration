package uk.gov.hmcts.reform.fpl.enums.cfv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.cfv.UploadBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.CTSC;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.LA;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.NON_CONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.ADDITIONAL_APPLCIATION_NOTIFICAITON_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.CAFCASS_API_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.CASE_SUMMARY_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.COURT_BUNDLE_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.COURT_CORRESPONDENCE_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.DEFAULT_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.NO_CAFCASS_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.NO_TRANSLATION_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.POSITION_STATEMENT_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration.SKELETON_ARGUMENT_NOTIFICATION_CONFIG;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@AllArgsConstructor
public enum DocumentType {
    COURT_BUNDLE("Court Bundle", courtBundleResolver(),
        false, false, false,
        (bundle) -> {
            Element<CourtBundle> courtBundleElement = element(CourtBundle.builder()
                .document(bundle.getDocument())
                .uploaderType(bundle.getUploaderType())
                .uploaderCaseRoles(bundle.getUploaderCaseRoles())
                .markAsConfidential(YesNo.from(bundle.isConfidential()).getValue())
                .translationRequirements(bundle.getTranslationRequirement())
                .build());
            return HearingCourtBundle.builder()
                .courtBundle(List.of(courtBundleElement))
                .build();
        },
        null, 10, COURT_BUNDLE_NOTIFICATION_CONFIG, "bundle"),
    CASE_SUMMARY("Case Summary", standardResolver("hearingDocuments.caseSummaryList"),
        false, false, false,
        (bundle) -> CaseSummary.builder().document(bundle.getDocument())
            .uploaderType(bundle.getUploaderType())
            .uploaderCaseRoles(bundle.getUploaderCaseRoles())
            .markAsConfidential(YesNo.from(bundle.isConfidential()).getValue())
            .translationRequirements(bundle.getTranslationRequirement())
            .build(),
        null,20, CASE_SUMMARY_NOTIFICATION_CONFIG, "caseSummary"),
    POSITION_STATEMENTS("Position Statements", standardResolver("hearingDocuments.posStmtList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 30, POSITION_STATEMENT_NOTIFICATION_CONFIG, "positionStatements"),
    POSITION_STATEMENTS_CHILD("Position Statements (Child)", standardResolver("hearingDocuments.posStmtChildList"),
        true, true, true,
        null,
        null, 31, null, "positionStatements"),
    POSITION_STATEMENTS_RESPONDENT("Position Statements (Respondent)",
        standardResolver("hearingDocuments.posStmtRespList"),
        true, true, true,
        null,
        null, 32, null, "positionStatements"),
    THRESHOLD("Threshold", standardResolver("thresholdList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 40, DEFAULT_NOTIFICATION_CONFIG, "threshold"),
    SKELETON_ARGUMENTS("Skeleton arguments", standardResolver("hearingDocuments.skeletonArgumentList"),
        false, false, false,
        (bundle) -> SkeletonArgument.builder()
            .document(bundle.getDocument())
            .uploaderType(bundle.getUploaderType())
            .uploaderCaseRoles(bundle.getUploaderCaseRoles())
            .markAsConfidential(YesNo.from(bundle.isConfidential()).getValue())
            .translationRequirements(bundle.getTranslationRequirement())
            .build(),
        null, 50, SKELETON_ARGUMENT_NOTIFICATION_CONFIG, "skeletonArguments"),
    AA_PARENT_ORDERS("Orders", null,
        false, false, false,
        null,
        null, 60, null, "orders"),
    JUDGEMENTS("└─ Judgments/facts and reasons", standardResolver("judgementList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_ORDERS, 70, DEFAULT_NOTIFICATION_CONFIG, "judgementsFactsAndReasons"),
    TRANSCRIPTS("└─ Transcripts", standardResolver("transcriptList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_ORDERS, 80, NO_TRANSLATION_NOTIFICATION_CONFIG, "transcripts"),
    AA_PARENT_APPLICANTS_DOCUMENTS("Applicant's documents", null,
        false, false, false,
        null,
        null, 90, null, "applicantsDocuments"),
    DOCUMENTS_FILED_ON_ISSUE("└─ Documents filed on issue", standardResolver("documentsFiledOnIssueList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 100, DEFAULT_NOTIFICATION_CONFIG, "documentsFiledOnIssue"),
    APPLICANTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("applicantWitnessStmtList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 110, DEFAULT_NOTIFICATION_CONFIG, "applicantWitnessStatements"),
    CARE_PLAN("└─ Care plan", standardResolver("carePlanList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 120, DEFAULT_NOTIFICATION_CONFIG, "carePlan"),
    PARENT_ASSESSMENTS("└─ Parent assessments", standardResolver("parentAssessmentList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 130, DEFAULT_NOTIFICATION_CONFIG, "parentAssessments "),
    FAMILY_AND_VIABILITY_ASSESSMENTS("└─ Family and viability assessments", standardResolver("famAndViabilityList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 140, DEFAULT_NOTIFICATION_CONFIG, "familyAndViabilityAssessments"),
    APPLICANTS_OTHER_DOCUMENTS("└─ Applicant's other documents", standardResolver("applicantOtherDocList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 150, DEFAULT_NOTIFICATION_CONFIG, "applicantsOtherDocuments"),
    MEETING_NOTES("└─ Meeting notes", standardResolver("meetingNoteList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 160, DEFAULT_NOTIFICATION_CONFIG, "meetingNotes"),
    CONTACT_NOTES("└─ Contact notes", standardResolver("contactNoteList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 170, DEFAULT_NOTIFICATION_CONFIG, "contactNotes"),
    AA_PARENT_APPLICATIONS("Applications", null,
        false, false, false,
        null,
        null, 180, null, "applications"),
    C1_APPLICATION_DOCUMENTS("└─ C1 application supporting documents", standardResolver("c1ApplicationDocList"),
        true, true, true,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICATIONS, 190, ADDITIONAL_APPLCIATION_NOTIFICAITON_CONFIG, "c1AndOtherApplications"),
    C2_APPLICATION_DOCUMENTS("└─ C2 application supporting documents", standardResolver("c2ApplicationDocList"),
        true, true, true,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICATIONS, 200, ADDITIONAL_APPLCIATION_NOTIFICAITON_CONFIG, "c2Applications"),
    AA_PARENT_RESPONDENTS_STATEMENTS("Respondent statements", null,
        false, false, false,
        null,
        null, 210, null, "parent_respondentsStatements"),
    RESPONDENTS_STATEMENTS("└─ Respondent statements", standardResolver("respStmtList"),
        false, false, false,
        (bundle) -> RespondentStatementV2.builder()
            .document(bundle.getDocument())
            .uploaderType(bundle.getUploaderType())
            .uploaderCaseRoles(bundle.getUploaderCaseRoles())
            .markAsConfidential(YesNo.from(bundle.isConfidential()).getValue())
            .translationRequirements(bundle.getTranslationRequirement())
            .build(),
        AA_PARENT_RESPONDENTS_STATEMENTS, 220, DEFAULT_NOTIFICATION_CONFIG, "parent_respondentsStatements"),
    RESPONDENTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("respWitnessStmtList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_RESPONDENTS_STATEMENTS, 230, DEFAULT_NOTIFICATION_CONFIG, "respondentWitnessStatements"),
    GUARDIAN_EVIDENCE("Guardian's evidence", standardResolver("guardianEvidenceList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 240, NO_CAFCASS_NOTIFICATION_CONFIG, "guardiansEvidence"),
    AA_PARENT_EXPERT_REPORTS("Expert Reports", null,
        false, false, false,
        null,
        null, 250, null, "parent_expertReports"),
    EXPERT_REPORTS("└─ Expert Reports", standardResolver("expertReportList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 260, DEFAULT_NOTIFICATION_CONFIG, "expertReports"),
    DRUG_AND_ALCOHOL_REPORTS("└─ Drug and alcohol reports", standardResolver("drugAndAlcoholReportList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 270, DEFAULT_NOTIFICATION_CONFIG, "drugAndAlcoholReports"),
    LETTER_OF_INSTRUCTION("└─ Letters of instruction / referrals", standardResolver("lettersOfInstructionList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 280, DEFAULT_NOTIFICATION_CONFIG, "lettersOfInstructionReferrals"),
    POLICE_DISCLOSURE("Police disclosure", standardResolver("policeDisclosureList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 290, DEFAULT_NOTIFICATION_CONFIG, "policeDisclosure"),
    MEDICAL_RECORDS("Medical records", standardResolver("medicalRecordList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 300, DEFAULT_NOTIFICATION_CONFIG, "medicalRecords"),
    COURT_CORRESPONDENCE("Court correspondence", standardResolver("correspondenceDocList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 310, COURT_CORRESPONDENCE_NOTIFICATION_CONFIG, "courtCorrespondence"),
    NOTICE_OF_ACTING_OR_ISSUE("Notice of acting / notice of issue", standardResolver("noticeOfActingOrIssueList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 320, DEFAULT_NOTIFICATION_CONFIG, "hearingNotices"),
    PREVIOUS_PROCEEDING("Previous Proceeding", standardResolver("previousProceedingList"),
        false, false, false,
        defaultWithDocumentBuilder(),
        null, 330, NO_CAFCASS_NOTIFICATION_CONFIG, "previousProceedings"),
    PLACEMENT_RESPONSES("Placement responses", null,
        false, false, false,
        null,
        null, 340, null, "placementApplicationsAndResponses"),
    GUARDIAN_REPORT("Guardian report", standardResolver("guardianReportsList"),
        true, false, true,
        defaultWithDocumentBuilder(),
        null, 350, CAFCASS_API_NOTIFICATION_CONFIG, "guardianReports"),
    ARCHIVED_DOCUMENTS("Archived migrated data", standardResolver("archivedDocumentsList"),
        true, true, true,
    defaultWithDocumentBuilder(),
        null, 999, DEFAULT_NOTIFICATION_CONFIG, "archivedDocuments");

    @Getter
    private String description;
    @Getter
    private Function<ConfidentialLevel, String> baseFieldNameResolver;
    @Getter
    private boolean hiddenFromLAUpload;
    @Getter
    private boolean hiddenFromCTSCUpload;
    @Getter
    private boolean hiddenFromSolicitorUpload;
    @Getter
    private Function<UploadBundle, Object> withDocumentBuilder;
    @Getter
    private DocumentType parentFolder;
    @Getter
    private final int displayOrder;
    @Getter
    private final DocumentUploadedNotificationConfiguration notificationConfiguration;
    @Getter
    private final String category;

    public boolean isUploadable() {
        if (isHiddenFromSolicitorUpload() && isHiddenFromLAUpload() && isHiddenFromCTSCUpload()) {
            return false;
        }
        return nonNull(baseFieldNameResolver) || PLACEMENT_RESPONSES == this;
    }

    public String getFieldName(DocumentUploaderType uploaderType, boolean confidential) {
        return getBaseFieldNameResolver().apply(getConfidentialLevel(uploaderType, confidential));
    }

    public String getFieldNameOfRemovedList() {
        return getBaseFieldNameResolver().apply(null);
    }

    public String getJsonFieldNameOfRemovedList() {
        return toJsonFieldName(getBaseFieldNameResolver().apply(null));
    }

    private ConfidentialLevel getConfidentialLevel(DocumentUploaderType uploaderType, boolean isConfidential) {
        switch (uploaderType) {
            case DESIGNATED_LOCAL_AUTHORITY:
            case SECONDARY_LOCAL_AUTHORITY:
                return isConfidential ? LA : NON_CONFIDENTIAL;
            case HMCTS:
                return isConfidential ? CTSC : NON_CONFIDENTIAL;
            case SOLICITOR:
            case BARRISTER:
            default:
                return NON_CONFIDENTIAL;
        }
    }

    private static Function<ConfidentialLevel, String> courtBundleResolver() {
        return confidentialLevel -> {
            if (confidentialLevel == null) {
                return "hearingDocuments.courtBundleListRemoved";
            }
            switch (confidentialLevel) {
                case NON_CONFIDENTIAL:
                    return "hearingDocuments.courtBundleListV2";
                case LA:
                case CTSC:
                    return standardNaming(confidentialLevel, "hearingDocuments.courtBundleList");
                default:
                    throw new IllegalArgumentException("unrecognised confidential level:" + confidentialLevel);
            }
        };
    }

    private static String standardNaming(ConfidentialLevel confidentialLevel, String baseFieldName) {
        if (confidentialLevel == null) {
            return baseFieldName + "Removed";
        }
        switch (confidentialLevel) {
            case NON_CONFIDENTIAL:
                return baseFieldName;
            case LA:
                return baseFieldName + "LA";
            case CTSC:
                return baseFieldName + "CTSC";
            default:
                throw new IllegalArgumentException("unrecognised confidential level:" + confidentialLevel);
        }
    }

    private static Function<ConfidentialLevel, String> standardResolver(String baseFieldName) {
        return confidentialLevel -> standardNaming(confidentialLevel, baseFieldName);
    }

    private static Function<UploadBundle, Object> defaultWithDocumentBuilder() {
        return (bundle) -> ManagedDocument.builder()
            .document(bundle.getDocument())
            .uploaderType(bundle.getUploaderType())
            .uploaderCaseRoles(bundle.getUploaderCaseRoles())
            .markAsConfidential(YesNo.from(bundle.isConfidential()).getValue())
            .translationRequirements(bundle.getTranslationRequirement())
            .build();
    }

    public List<String> getJsonFieldNames() {
        return Arrays.stream(ConfidentialLevel.values())
            .map(c -> this.baseFieldNameResolver == null ? null : this.baseFieldNameResolver.apply(c))
            .filter(Objects::nonNull)
            .map(f -> removeNested(f))
            .toList();
    }

    public List<String> getFieldNames() {
        return Arrays.stream(ConfidentialLevel.values())
            .map(c -> this.baseFieldNameResolver == null ? null : this.baseFieldNameResolver.apply(c))
            .filter(Objects::nonNull)
            .toList();
    }

    private static String removeNested(String fieldName) {
        String[] splitFieldNames = fieldName.split("\\.");
        if (splitFieldNames.length == 1) {
            return fieldName;
        } else {
            return splitFieldNames[1];
        }
    }

    public static DocumentType fromJsonFieldName(String jsonFieldName) {
        return Arrays.stream(DocumentType.values())
            .filter(dt -> dt.getJsonFieldNames().contains(jsonFieldName))
            .findFirst().orElse(null);
    }

    public static DocumentType fromFieldName(String fieldName) {
        try {
            return DocumentType.valueOf(fieldName);
        } catch (IllegalArgumentException ex) {
            return Arrays.stream(DocumentType.values())
                .filter(dt -> dt.getFieldNames().contains(fieldName))
                .findFirst().orElse(null);
        }
    }

    public static String toJsonFieldName(String fieldName) {
        return removeNested(fieldName);
    }

}
