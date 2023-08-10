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
        null, 10),
    CASE_SUMMARY("Case Summary", standardResolver("hearingDocuments.caseSummaryList"),
        false, false, false, false,
        (document, documentUploaderType) -> CaseSummary.builder().document(document).uploaderType(documentUploaderType)
            .build(),
        null,20),
    POSITION_STATEMENTS("Position Statements", standardResolver("hearingDocuments.posStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 30),
    THRESHOLD("Threshold", standardResolver("thresholdList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 40),
    SKELETON_ARGUMENTS("Skeleton arguments", standardResolver("hearingDocuments.skeletonArgumentList"),
        false, false, false, false,
        (document, documentUploaderType) -> SkeletonArgument.builder().document(document)
            .uploaderType(documentUploaderType).build(),
        null, 50),
    AA_PARENT_ORDERS("Orders", null,
        false, false, false, false,
        null,
        null, 60),
    JUDGEMENTS("└─ Judgements/facts and reasons", standardResolver("judgementList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_ORDERS, 70),
    TRANSCRIPTS("└─ Transcripts", standardResolver("transcriptList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_ORDERS, 80),
    AA_PARENT_APPLICANTS_DOCUMENTS("Applicant's documents", null,
        false, false, false, false,
        null,
        null, 90),
    DOCUMENTS_FILED_ON_ISSUE("└─ Documents filed on issue", standardResolver("documentsFiledOnIssueList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 100),
    APPLICANTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("applicantWitnessStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 110),
    CARE_PLAN("└─ Care plan", standardResolver("carePlanList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 120),
    PARENT_ASSESSMENTS("└─ Parent assessments", standardResolver("parentAssessmentList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 130),
    FAMILY_AND_VIABILITY_ASSESSMENTS("└─ Family and viability assessments", standardResolver("famAndViabilityList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 140),
    APPLICANTS_OTHER_DOCUMENTS("└─ Applicant's other documents", standardResolver("applicantOtherDocList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 150),
    MEETING_NOTES("└─ Meeting notes", standardResolver("meetingNoteList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 160),
    CONTACT_NOTES("└─ Contact notes", standardResolver("contactNoteList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_APPLICANTS_DOCUMENTS, 170),
    AA_PARENT_RESPONDENTS_STATEMENTS("Respondent statements", null,
        false, false, false, false,
        null,
        null, 180),
    RESPONDENTS_STATEMENTS("└─ Respondent statements", standardResolver("respStmtList"),
        false, false, false, false,
        (document, documentUploaderType) -> RespondentStatementV2.builder().document(document)
            .uploaderType(documentUploaderType).build(),
        AA_PARENT_RESPONDENTS_STATEMENTS, 190),
    RESPONDENTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("respWitnessStmtList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_RESPONDENTS_STATEMENTS, 200),
    GUARDIAN_EVIDENCE("Guardian's evidence", standardResolver("guardianEvidenceList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 210),
    AA_PARENT_EXPERT_REPORTS("Expert Reports", null,
        false, false, false, false,
        null,
        null, 220),
    EXPERT_REPORTS("└─ Expert Reports", standardResolver("expertReportList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 230),
    DRUG_AND_ALCOHOL_REPORTS("└─ Drug and alcohol reports", standardResolver("drugAndAlcoholReportList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 240),
    LETTER_OF_INSTRUCTION("└─ Letters of instruction / referrals", standardResolver("lettersOfInstructionList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        AA_PARENT_EXPERT_REPORTS, 250),
    POLICE_DISCLOSURE("Police disclosure", standardResolver("policeDisclosureList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 260),
    MEDICAL_RECORDS("Medical records", standardResolver("medicalRecordList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 270),
    COURT_CORRESPONDENCE("Court correspondence", standardResolver("correspondenceDocList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 280),
    NOTICE_OF_ACTING_OR_ISSUE("Notice of acting / notice of issue", standardResolver("noticeOfActingOrIssueList"),
        false, false, false, false,
        defaultWithDocumentBuilder(),
        null, 290),
    PLACEMENT_RESPONSES("Placement responses", null,
        false, false, false, false,
        null,
        null, 300);

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
    private DocumentType parentFolder;
    @Getter
    private final int displayOrder;

    public boolean isUploadable() {
        return nonNull(baseFieldNameResolver) || PLACEMENT_RESPONSES == this;
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
