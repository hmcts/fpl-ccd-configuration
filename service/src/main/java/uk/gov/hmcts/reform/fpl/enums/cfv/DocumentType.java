package uk.gov.hmcts.reform.fpl.enums.cfv;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

import static java.util.Objects.nonNull;

@AllArgsConstructor
public enum DocumentType {
    BUNDLE("Bundle", courtBundleResolver(),  false,
        true, true,
        10),
    CASE_SUMMARY("Case Summary", standardResolver("hearingDocuments.caseSummaryList"), false,
        true, true,
        20),
    THRESHOLD("Threshold", standardResolver("thresholdList"), false,
        true, false,
        30),
    DRAFT_ORDER_FOR_REVIEW("Draft order for review prior to hearing", null, true,
        false, false,
        40),
    SKELETON_ARGUMENTS("Skeleton arguments", standardResolver("hearingDocuments.skeletonArgumentList"), false,
        true, true,
        50),
    APPLICATIONS("Applications", null, true,
        false, false,
        70),
    APPLICANTS_DOCUMENTS("Applicant's documents", null, false,
        true, true,
        140),
    DOCUMENTS_FILED_ON_ISSUE("└─ Documents filed on issue", standardResolver("documentsFiledOnIssueList"), false,
        true,  false,
        150),
    APPLICANTS_WITNESS_STATEMENTS("└─ Witness statements", standardResolver("applicantWitnessStmtList"), false,
        true,  true,
        160),
    CARE_PLAN("└─ Care plan", standardResolver("carePlanList"), false,
        true, false,
        170),
    PARENT_ASSESSMENTS("└─ Parent assessments", standardResolver("parentAssessmentList"), false,
        true, true,
        180),
    FAMILY_AND_VIABILITY_ASSESSMENTS("└─ Family and viability assessments", standardResolver("famAndViabilityList"), false,
        true, true,
        190),
    APPLICANTS_OTHER_DOCUMENTS("└─ Applicant’s other documents", standardResolver("applicantOtherDocList"), false,
        true, true,
        200);

    @Getter
    private String description;
    @Getter
    private Function<ConfidentialLevel, String> baseFieldNameResolver;
    @Getter
    private boolean hidden;
    @Getter
    private boolean uploadableByLA;
    @Getter
    private boolean uploadableByCTSC;
    @Getter
    private final int displayOrder;

    public boolean isUploadable() {
        return nonNull(baseFieldNameResolver);
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
}
