package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;

class ReviewDecisionTest {
    @Test
    void shouldReturnTrueIfReviewOutcomesMatch() {
        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();
        assertThat(reviewDecision.hasReviewOutcomeOf(SEND_TO_ALL_PARTIES)).isTrue();
    }

    @Test
    void shouldReturnFalseIfReviewOutcomesDoNotMatch() {
        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();
        assertThat(reviewDecision.hasReviewOutcomeOf(JUDGE_AMENDS_DRAFT)).isFalse();
    }
}
