package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.Mockito.verify;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ManageOrdersSubmittedControllerTest extends AbstractCallbackTest {

    private static final Long CASE_ID = 1614860986487554L;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    ManageOrdersSubmittedControllerTest() {
        super("manage-orders");
    }

    @Test
    public void testPostSubmitEventIsTriggered() {
        CaseData caseData = CaseData.builder().id(CASE_ID).build();
        postSubmittedEvent(caseData);
        verify(coreCaseDataService).triggerEvent(CASE_ID,
            "internal-change-manage-order",
            asCaseDetails(caseData).getData());
    }
}
