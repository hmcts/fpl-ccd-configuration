package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.Mockito.verify;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerSubmittedTest extends AbstractCallbackTest {

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ApproveDraftOrdersControllerSubmittedTest() {
        super("approve-draft-orders");
    }

    @Test
    public void shouldTriggerPostHandlingEvent() {
        postSubmittedEvent(CaseData.builder().id(1L).build());
        verify(coreCaseDataService)
            .performPostSubmitCallbackWithoutChange(1L, "internal-change-approve-order");
    }
}
