package uk.gov.hmcts.reform.fpl.model;

import java.util.List;

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
    private final ApproveOrderUrgencyOption urgencyOption;

    @JsonIgnore
    public boolean hasReviewOutcomeOf(CMOReviewOutcome reviewOutcome) {
        return reviewOutcome.equals(decision);
    }

    public ApproveOrderUrgencyOption getUrgencyOption() {
        if (urgencyOption == null) {
            return ApproveOrderUrgencyOption.builder().urgency(List.of()).build();
        }
        return urgencyOption;
    }
}
