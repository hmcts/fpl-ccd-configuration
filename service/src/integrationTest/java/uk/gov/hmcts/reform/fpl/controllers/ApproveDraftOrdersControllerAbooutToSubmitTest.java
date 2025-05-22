package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.JudicialService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerAbooutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private JudicialService judicialService;

    ApproveDraftOrdersControllerAbooutToSubmitTest() {
        super("approve-draft-orders");
    }

    @Test
    void shouldCaptureJudgeTitleAndName() {
        when(judicialService.getJudgeTitleAndNameOfCurrentUser()).thenReturn("Judge Name");

        CaseData returnedCaseData = extractCaseData(postAboutToSubmitEvent(CaseData.builder().build()));
        assertThat(returnedCaseData.getReviewDraftOrdersData().getJudgeTitleAndName())
            .isEqualTo("Judge Name");
    }
}
