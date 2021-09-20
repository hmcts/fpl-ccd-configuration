package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerAboutToStartTest extends AbstractCallbackTest {

    CaseExtensionControllerAboutToStartTest() {
        super("case-extension");
    }

    @Test
    void shouldPopulateShouldBeCompletedByDateWith26WeekTimeline() {
        LocalDate dateSubmitted = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("dateSubmitted", dateSubmitted,
                "caseCompletionDate", ""))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData().get("shouldBeCompletedByDate")).isEqualTo("13 May 2031");
    }

    @Test
    void shouldPopulateShouldBeCompletedByDateWithCaseCompletionDate() {
        LocalDate dateSubmitted = LocalDate.of(2030, 11, 12);
        LocalDate caseCompletionDate = LocalDate.of(2040, 12, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("dateSubmitted", dateSubmitted,
                "caseCompletionDate", caseCompletionDate))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData().get("shouldBeCompletedByDate")).isEqualTo("12 December 2040");
    }
}
