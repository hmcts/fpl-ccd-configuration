package uk.gov.hmcts.reform.fpl.enums.cfv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.configuration.DocumentUploadedNotificationConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.CTSC;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.LA;
import static uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel.NON_CONFIDENTIAL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@AllArgsConstructor
public enum DocumentType {
    COURT_BUNDLE("Court Bundle", courtBundleResolver(),
        false, false, false, false,
        (document, documentUploaderType) -> HearingCourtBundle.builder()
            .courtBundle(List.of(
                element(CourtBundle.builder().document(document).uploaderType(documentUploaderType).build())
            ))
            .courtBundleNC(List.of(
                element(CourtBundle.builder().document(document).uploaderType(documentUploaderType).build())
            ))
            .build(),
        10, DocumentUploadedNotificationConfiguration.builder().build()),
    CASE_SUMMARY("Case Summary", standardResolver("hearingDocuments.caseSummaryList"),
        false, false, false, false,
        (document, documentUploaderType) -> CaseSummary.builder().document(document).uploaderType(documentUploaderType)
            .build(),
        20, DocumentUploadedNotificationConfiguration.builder().build()),
    POSITION_STATEMENTS("Position Statements", standardResolver("hearingDocuments.posStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        30, DocumentUploadedNotificationConfiguration.builder().build()),
    THRESHOLD("Threshold", standardResolver("thresholdList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        40, DocumentUploadedNotificationConfiguration.builder().build()),
    SKELETON_ARGUMENTS("Skeleton arguments", standardResolver("hearingDocuments.skeletonArgumentList"),
        false, false, false, false,
        (document, documentUploaderType) -> SkeletonArgument.builder().document(document)
            .uploaderType(documentUploaderType).build(),
        50, DocumentUploadedNotificationConfiguration.builder().build()),
    AA_PARENT_ORDERS("Orders", null,
        false, false, false, false,
        null,
        60, DocumentUploadedNotificationConfiguration.builder().build()),
    JUDGEMENTS("└─ Judgements/facts and reasons", standardResolver("judgementList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        70, DocumentUploadedNotificationConfiguration.builder().build()),
    TRANSCRIPTS("└─ Transcripts", standardResolver("transcriptList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        80, DocumentUploadedNotificationConfiguration.builder().build()),
    AA_PARENT_APPLICANTS_DOCUMENTS("Applicant's documents", null,
        false, false, false, false,
        null,
        90, DocumentUploadedNotificationConfiguration.builder().build()),
    DOCUMENTS_FILED_ON_ISSUE("└─ Documents filed on issue", standardResolver("documentsFiledOnIssueListLA"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        100, DocumentUploadedNotificationConfiguration.builder().build()),
    APPLICANTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("applicantWitnessStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        110, DocumentUploadedNotificationConfiguration.builder().build()),
    CARE_PLAN("└─ Care plan", standardResolver("carePlanList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        120, DocumentUploadedNotificationConfiguration.builder().build()),
    PARENT_ASSESSMENTS("└─ Parent assessments", standardResolver("parentAssessmentList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        130, DocumentUploadedNotificationConfiguration.builder().build()),
    FAMILY_AND_VIABILITY_ASSESSMENTS("└─ Family and viability assessments", standardResolver("famAndViabilityList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        140, DocumentUploadedNotificationConfiguration.builder().build()),
    APPLICANTS_OTHER_DOCUMENTS("└─ Applicant's other documents", standardResolver("applicantOtherDocList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        150, DocumentUploadedNotificationConfiguration.builder().build()),
    MEETING_NOTES("└─ Meeting notes", standardResolver("meetingNoteList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        160, DocumentUploadedNotificationConfiguration.builder().build()),
    CONTACT_NOTES("└─ Contact notes", standardResolver("contactNoteList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        170, DocumentUploadedNotificationConfiguration.builder().build()),
    AA_PARENT_RESPONDENTS_STATEMENTS("Respondent statements", null,
        false, false, false, false,
        null,
        180, DocumentUploadedNotificationConfiguration.builder().build()),
    RESPONDENTS_STATEMENTS("└─ Respondent statements", standardResolver("respStmtList"),
        false, false, false, false,
        (document, documentUploaderType) -> RespondentStatementV2.builder().document(document)
            .uploaderType(documentUploaderType).build(),
        190, DocumentUploadedNotificationConfiguration.builder().build()),
    RESPONDENTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("respWitnessStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        200, DocumentUploadedNotificationConfiguration.builder().build()),
    GUARDIAN_EVIDENCE("Guardian's evidence", standardResolver("guardianEvidenceList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        210, DocumentUploadedNotificationConfiguration.builder().build()),
    AA_PARENT_EXPERT_REPORTS("Expert Reports", null,
        false, false, false, false,
        null,
        220, DocumentUploadedNotificationConfiguration.builder().build()),
    EXPERT_REPORTS("└─ Expert Reports", standardResolver("expertReportList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        230, DocumentUploadedNotificationConfiguration.builder().build()),
    DRUG_AND_ALCOHOL_REPORTS("└─ Drug and alcohol reports", standardResolver("drugAndAlcoholReportList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        240, DocumentUploadedNotificationConfiguration.builder().build()),
    LETTER_OF_INSTRUCTION("└─ Letters of instruction / referrals", standardResolver("lettersOfInstructionList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        250, DocumentUploadedNotificationConfiguration.builder().build()),
    POLICE_DISCLOSURE("Police disclosure", standardResolver("policeDisclosureList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        260, DocumentUploadedNotificationConfiguration.builder().build()),
    MEDICAL_RECORDS("Medical records", standardResolver("medicalRecordList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        270, DocumentUploadedNotificationConfiguration.builder().build()),
    COURT_CORRESPONDENCE("Court correspondence", standardResolver("correspondenceDocList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        280, DocumentUploadedNotificationConfiguration.builder().build()),
    NOTICE_OF_ACTING_OR_ISSUE("Notice of acting / notice of issue", standardResolver("noticeOfActingOrIssueList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        290, DocumentUploadedNotificationConfiguration.builder().build()),
    PLACEMENT_RESPONSES("Placement responses", null,
        false, false, false, false,
        null,
        300, DocumentUploadedNotificationConfiguration.builder().build());

    @Getter
    private String description;
    @Getter
    private Function<ConfidentialLevel, String> baseFieldNameResolver;
    @Getter
    private boolean hiddenFromUpload;
    @Getter
    private boolean hiddenFromLAUpload;
    @Getter
    private boolean hiddenFromCTSCUpload;
    @Getter
    private boolean hiddenFromSolicitorUpload;
    @Getter
    private BiFunction<DocumentReference, DocumentUploaderType, Object> withDocumentBuilder;
    @Getter
    private final int displayOrder;
    @Getter
    private final DocumentUploadedNotificationConfiguration notificationConfiguration;

    public boolean isUploadable() {
        return nonNull(baseFieldNameResolver);
    }

    public String getFieldName(DocumentUploaderType uploaderType, boolean confidential) {
        return getBaseFieldNameResolver().apply(getConfidentialLevel(uploaderType,confidential));
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

    private static final Function<ConfidentialLevel, String> courtBundleResolver() {
        return confidentialLevel -> {
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

    private static final String standardNaming(ConfidentialLevel confidentialLevel, String baseFieldName) {
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

    private static final Function<ConfidentialLevel, String> standardResolver(String baseFieldName) {
        return confidentialLevel -> standardNaming(confidentialLevel, baseFieldName);
    }

    private static BiFunction<DocumentReference, DocumentUploaderType, Object> defaultWithDocumentBuilder() {
        return (document, uploaderType) -> ManagedDocument.builder()
            .document(document)
            .uploaderType(uploaderType)
            .build();
    }

    public List<String> getJsonFieldNames() {
        return Arrays.stream(ConfidentialLevel.values())
            .map(c -> this.baseFieldNameResolver == null ? null : this.baseFieldNameResolver.apply(c))
            .filter(Objects::nonNull)
            .map(f -> removeNested(f))
            .collect(Collectors.toList());
    }

    private String removeNested(String fieldName) {
        String[] splitFieldNames = fieldName.split("\\.");
        if (splitFieldNames.length == 1) {
            return fieldName;
        } else {
            return splitFieldNames[1];
        }
    }

    public static DocumentType fromJsonFieldName(String fieldName) {
        return Arrays.stream(DocumentType.values())
            .filter(dt -> dt.getJsonFieldNames().contains(fieldName))
            .findFirst().orElse(null);
    }

}
