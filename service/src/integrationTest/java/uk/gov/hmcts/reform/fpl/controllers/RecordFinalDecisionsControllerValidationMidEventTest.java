package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.RecordChildrenFinalDecisionsEventData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(RecordFinalDecisionsController.class)
@OverrideAutoConfiguration(enabled = true)
class RecordFinalDecisionsControllerValidationMidEventTest extends AbstractCallbackTest {

    RecordFinalDecisionsControllerValidationMidEventTest() {
        super("record-final-decisions");
    }

    @Test
    void shouldReturnAnErrorIfFinalDecisionDateInFuture() {
        CaseData caseData = CaseData.builder()
            .recordChildrenFinalDecisionsEventData(
                RecordChildrenFinalDecisionsEventData.builder()
                    .finalDecisionDate(LocalDate.now().plusDays(1))
                    .build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "validate");

        assertThat(response.getErrors()).containsOnly("The final decision date must be in the past");
    }

    @Test
    void shouldNotReturnAnErrorIfFinalDecisionDateIsToday() {
        CaseData caseData = CaseData.builder()
            .recordChildrenFinalDecisionsEventData(
                RecordChildrenFinalDecisionsEventData.builder()
                    .finalDecisionDate(LocalDate.now())
                    .build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "validate");

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorIfFinalDecisionDateInPast() {
        CaseData caseData = CaseData.builder()
            .recordChildrenFinalDecisionsEventData(
                RecordChildrenFinalDecisionsEventData.builder()
                    .finalDecisionDate(LocalDate.now().minusDays(1))
                    .build()
            ).build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "validate");

        assertThat(response.getErrors()).isEmpty();
    }

}
