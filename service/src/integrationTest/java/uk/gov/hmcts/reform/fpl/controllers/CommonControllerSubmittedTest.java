package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class CommonControllerSubmittedTest extends AbstractControllerTest {

    CommonControllerSubmittedTest() {
        super("add-case-number");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldNotSendNotificationToRoboticsWhenCaseNumberUpdated() {
        final CallbackRequest callbackRequest = callbackRequest();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(callbackRequest.getCaseDetails().getId()),
            eq("internal-update-task-list"),
            anyMap());
    }
}
