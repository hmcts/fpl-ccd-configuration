package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
public class ReviewDecision {
    private final DocumentReference document;
    private final CMOReviewOutcome decision;
    private final String changeRequestedByJudge;
}
