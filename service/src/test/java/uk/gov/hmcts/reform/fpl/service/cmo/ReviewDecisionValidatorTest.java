package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;

class ReviewDecisionValidatorTest {

    private static final String ORDER_NAME = "Order Name";
    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);

    private final ReviewDecisionValidator underTest = new ReviewDecisionValidator();

    @Test
    void validateIfNoDecision() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder().build(), ORDER_NAME);

        assertThat(actual).isEqualTo(emptyList());
    }

    @Test
    void validateIfJudgeAmendDraftButDocumentNotPresent() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder()
            .decision(JUDGE_AMENDS_DRAFT)
            .judgeAmendedDocument(null)
            .build(), ORDER_NAME);

        assertThat(actual).isEqualTo(List.of("Add the new Order Name"));
    }

    @Test
    void validateIfJudgeAmendDraftAndDocumentPresent() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder()
            .decision(JUDGE_AMENDS_DRAFT)
            .judgeAmendedDocument(DOCUMENT_REFERENCE)
            .build(), ORDER_NAME);

        assertThat(actual).isEqualTo(emptyList());
    }

    @Test
    void validateIfJudgeRequestedChangesRequestNotSet() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder()
            .decision(JUDGE_REQUESTED_CHANGES)
            .changesRequestedByJudge(null)
            .build(), ORDER_NAME);

        assertThat(actual).isEqualTo(List.of("Add what the LA needs to change on the Order Name"));
    }

    @Test
    void validateIfJudgeRequestedChangesRequestEmpty() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder()
            .decision(JUDGE_REQUESTED_CHANGES)
            .changesRequestedByJudge("")
            .build(), ORDER_NAME);

        assertThat(actual).isEqualTo(List.of("Add what the LA needs to change on the Order Name"));
    }

    @Test
    void validateIfJudgeRequestedChangesRequestCompleted() {

        List<String> actual = underTest.validateReviewDecision(ReviewDecision.builder()
            .decision(JUDGE_REQUESTED_CHANGES)
            .changesRequestedByJudge("Change this")
            .build(), ORDER_NAME);

        assertThat(actual).isEqualTo(emptyList());
    }
}
