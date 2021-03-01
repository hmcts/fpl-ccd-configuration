package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CloseCase;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(CloseCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class CloseCaseControllerMidEventTest extends AbstractControllerTest {

    CloseCaseControllerMidEventTest() {
        super("close-case");
    }

    @Test
    void shouldReturnAnErrorIfDateIsInTheFuture() {
        CloseCase closeCase = CloseCase.builder()
            .date(dateNow().plusDays(2))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("closeCase", closeCase))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails);

        assertThat(response.getErrors()).containsOnly("The close case date must be in the past");
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsToday() {
        CloseCase closeCase = CloseCase.builder()
            .date(dateNow())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("closeCase", closeCase))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorIfDateIsInThePast() {
        CloseCase closeCase = CloseCase.builder()
            .date(dateNow().minusDays(2))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("closeCase", closeCase))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }
}
