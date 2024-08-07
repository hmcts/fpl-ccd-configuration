package uk.gov.hmcts.reform.fpl.service.cmo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;

@Component
public class ReviewDecisionValidator {

    public List<String> validateReviewDecision(ReviewDecision reviewDecision, String orderName) {

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())
            && reviewDecision.getJudgeAmendedDocument() == null) {
            return List.of(String.format("Add the new %s", orderName));
        }

        if (JUDGE_REQUESTED_CHANGES.equals(reviewDecision.getDecision())
            && isBlank(reviewDecision.getChangesRequestedByJudge())) {
            return List.of(String.format("Add what the LA needs to change on the %s", orderName));
        }

        return emptyList();
    }
}
