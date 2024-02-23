package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
public class ReviewDecision {
    private final DocumentReference document;
    private final DocumentReference judgeAmendedDocument;
    private final String hearing;
    private final CMOReviewOutcome decision;
    private final String changesRequestedByJudge;
    private final YesNo urgency;

    @JsonIgnore
    public boolean hasReviewOutcomeOf(CMOReviewOutcome reviewOutcome) {
        return reviewOutcome.equals(decision);
    }
}
