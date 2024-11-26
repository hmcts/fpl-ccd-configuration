package uk.gov.hmcts.reform.fpl.model.summary;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class SyntheticCaseSummary {
    String caseSummaryFlagAddedByFullName;
    String caseSummaryFlagAddedByEmail;
    DocumentReference caseSummaryFlagAssessmentForm;
    String caseSummaryCaseFlagNotes;

    String caseSummaryLanguageRequirement;
    String caseSummaryLALanguageRequirement;

    String caseSummaryOrdersRequested;

    LocalDate caseSummaryDateOfIssue;
    LocalDate deadline26week;

    String caseSummaryHasUnresolvedMessages;

    String caseSummaryCourtName;
    String caseSummaryPreviousCourtName;
    String caseSummaryHighCourtCase;
    String caseSummaryLAHighCourtCase;

    String caseSummaryHasNextHearing;
    String caseSummaryNextHearingType;
    LocalDate caseSummaryNextHearingDate;
    LocalDateTime caseSummaryNextHearingDateTime;
    NextHearingDetails nextHearingDetails;

    String caseSummaryNextHearingJudge;
    String caseSummaryNextHearingEmailAddress;
    DocumentReference caseSummaryNextHearingCMO;

    String caseSummaryHasPreviousHearing;
    String caseSummaryPreviousHearingType;
    LocalDate caseSummaryPreviousHearingDate;
    DocumentReference caseSummaryPreviousHearingCMO;

    String caseSummaryHasFinalHearing;
    LocalDate caseSummaryFinalHearingDate;

    String caseSummaryAllocatedJudgeName;
    String caseSummaryAllocatedJudgeEmail;


    Integer caseSummaryNumberOfChildren;
    String caseSummaryLASolicitorName;
    String caseSummaryLASolicitorEmail;
    String caseSummaryFirstRespondentLastName;
    String caseSummaryFirstRespondentLegalRep;
    String caseSummaryCafcassGuardian;

    String caseSummaryLATabHidden;

    public static SyntheticCaseSummary emptySummary() {
        return SyntheticCaseSummary.builder().build();
    }

}
