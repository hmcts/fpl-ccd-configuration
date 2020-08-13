package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
public class NotifyGatekeeperControllerSubmittedTest extends AbstractControllerTest {
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String CAFCASS_EMAIL = "Cafcass+gatekeeper@gmail.com";
    private static final String SUBMITTED = "Submitted";
    private static final String GATEKEEPING = "Gatekeeping";
    private static final String CASE_ID = "12345";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    NotifyGatekeeperControllerSubmittedTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnPopulatedDirectionsByRoleInSubmittedCallback() {
        postSubmittedEvent(buildCallbackRequest(SUBMITTED));

        verify(populateStandardDirectionsHandler).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldNotPublishPopulateStandardDirectionsEventWhenEventIsNotInSubmittedState() {
        postSubmittedEvent(buildCallbackRequest(GATEKEEPING));

        verify(populateStandardDirectionsHandler, never()).populateStandardDirections(any());
    }

    @Test
    void shouldNotifyMultipleGatekeepers() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(notificationClient).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(GATEKEEPER_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE));

        verify(notificationClient).sendEmail(
            eq(GATEKEEPER_SUBMISSION_TEMPLATE), eq(CAFCASS_EMAIL),
            anyMap(), eq(NOTIFICATION_REFERENCE));
    }

    private CallbackRequest buildCallbackRequest(String state) {
        CallbackRequest callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().setState(state);
        return callbackRequest;
    }
}
