package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseProgressionReportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.controllers.CaseProgressionReportController.CASE_PROGRESSION_REPORT_DETAILS;

@WebMvcTest(CaseProgressionReportController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseProgressReportControllerMidEventTest extends AbstractCallbackTest {

    @MockBean
    private CaseProgressionReportService caseProgressionReportService;

    CaseProgressReportControllerMidEventTest() {
        super("case-progression-report");
    }

    @Test
    void shouldPopulateCaseProgressionReport() {
        CaseData caseData = CaseData.builder().build();

        String response = "html";
        given(caseProgressionReportService.getHtmlReport(isA(CaseData.class)))
                .willReturn(response);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "fetch-data");

        assertThat(callbackResponse.getData()).containsKey(CASE_PROGRESSION_REPORT_DETAILS);
        assertThat(callbackResponse.getData().get(CASE_PROGRESSION_REPORT_DETAILS)).isEqualTo(response);
    }
}
