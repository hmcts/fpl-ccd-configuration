package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CMOReviewOutcome {
    @CCD(label = "Yes")
    SEND_TO_ALL_PARTIES,
    @CCD(label = "No, I need to make changes")
    JUDGE_AMENDS_DRAFT,
    @CCD(label = "No, the applicant needs to make changes")
    JUDGE_REQUESTED_CHANGES,
    @CCD(label = "Review later")
    REVIEW_LATER,
    @CCD(label = "The draft order is not required and should be removed")
    JUDGE_REMOVED
}
