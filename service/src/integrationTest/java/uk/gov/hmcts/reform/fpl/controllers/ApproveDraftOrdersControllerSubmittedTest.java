package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.Mockito.verify;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApproveDraftOrdersControllerSubmittedTest extends AbstractCallbackTest {

    @MockitoBean
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
