package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
public class ReviewDecision {
    private final DocumentReference document;
    private final DocumentReference judgeAmendedDocument;
    private final String hearing;
    private final CMOReviewOutcome decision;
    private final String changesRequestedByJudge;

    @JsonIgnore
    public boolean hasReviewOutcomeOf(CMOReviewOutcome reviewOutcome) {
        return reviewOutcome.equals(decision);
    }

    @JsonIgnore
    public boolean hasBeenApproved() {
        return CMOReviewOutcome.SEND_TO_ALL_PARTIES.equals(decision)
            || CMOReviewOutcome.JUDGE_AMENDS_DRAFT.equals(decision);
    }
}
