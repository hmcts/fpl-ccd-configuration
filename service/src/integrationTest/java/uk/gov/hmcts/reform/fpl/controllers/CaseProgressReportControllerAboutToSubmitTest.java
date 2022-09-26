package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.events.CaseProgressionReportEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.CaseProgressionReportEventData;
import uk.gov.hmcts.reform.fpl.service.EventService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.controllers.CaseProgressionReportController.CASE_PROGRESSION_REPORT_DETAILS;

@WebMvcTest(CaseProgressionReportController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseProgressReportControllerAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private EventService eventService;

    @Captor
    private ArgumentCaptor<CaseProgressionReportEvent> caseProgressionReportEventArgumentCaptor;

    CaseProgressReportControllerAboutToSubmitTest() {
        super("case-progression-report");
    }

    @Test
    void shouldCleanUpCaseProgressionReport() {
        CaseProgressionReportEventData caseProgressionReportEventData = CaseProgressionReportEventData.builder()
                .swanseaDFJCourts("344")
                .build();

        CaseData caseData = CaseData.builder()
                .caseProgressionReportEventData(caseProgressionReportEventData)
                .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        Map<String, Object> data = callbackResponse.getData();

        assertThat(data).doesNotContainKey(CASE_PROGRESSION_REPORT_DETAILS);
        assertThat(data).doesNotContainKey("swanseaDFJCourts");

        verify(idamClient).getUserDetails(isA(String.class));
        verify(eventService).publishEvent(caseProgressionReportEventArgumentCaptor.capture());
        CaseProgressionReportEvent caseProgressionReportEvent = caseProgressionReportEventArgumentCaptor.getValue();
        CaseProgressionReportEventData eventData = caseProgressionReportEvent.getCaseData().getCaseProgressionReportEventData();
        assertThat(eventData.getSwanseaDFJCourts())
                .isEqualTo("344");
        assertThat(eventData.getCentralLondonDFJCourts())
                .isNull();
    }
}

