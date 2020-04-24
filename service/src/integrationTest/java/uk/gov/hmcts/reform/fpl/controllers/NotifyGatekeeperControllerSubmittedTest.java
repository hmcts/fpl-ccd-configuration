package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.hmcts.reform.fpl.model.notify.sendtogatekeeper.NotifyGatekeeperTemplate;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
public class NotifyGatekeeperControllerSubmittedTest extends AbstractControllerTest {
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String CAFCASS_EMAIL = "Cafcass+gatekeeper@gmail.com";
    private static final String SUBMITTED = "Submitted";
    private static final String GATEKEEPING = "Gatekeeping";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    NotifyGatekeeperControllerSubmittedTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnPopulatedDirectionsByRoleInSubmittedCallback() throws Exception {
        postSubmittedEvent(buildCallbackRequest(SUBMITTED));

        verify(populateStandardDirectionsHandler).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldNotPublishPopulateStandardDirectionsEventWhenEventIsNotInSubmittedState() throws IOException {
        postSubmittedEvent(buildCallbackRequest(GATEKEEPING));

        verify(populateStandardDirectionsHandler, never()).populateStandardDirections(any());
    }

    @Test
    void shouldNotifyMultipleGatekeepersWithExpectedNotificationParameters() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(notificationClient).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL,
            getTemplate(CAFCASS_EMAIL).toMap(mapper), "12345");

        verify(notificationClient).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, CAFCASS_EMAIL,
            getTemplate(GATEKEEPER_EMAIL).toMap(mapper), "12345");
    }

    private NotifyGatekeeperTemplate getTemplate(String email) {
        NotifyGatekeeperTemplate expectedTemplate = new NotifyGatekeeperTemplate();

        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");

        expectedTemplate.setReference("12345");
        expectedTemplate.setOrdersAndDirections(ordersAndDirections);
        expectedTemplate.setGatekeeperRecipients(buildRecipientLabel(email));
        expectedTemplate.setUrgentHearing(YES.getValue());
        expectedTemplate.setFullStop(NO.getValue());
        expectedTemplate.setTimeFramePresent(YES.getValue());
        expectedTemplate.setLocalAuthority("Example Local Authority");
        expectedTemplate.setTimeFrameValue("same day");
        expectedTemplate.setNonUrgentHearing(NO.getValue());
        expectedTemplate.setCaseUrl("http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
        expectedTemplate.setFirstRespondentName("Smith");
        expectedTemplate.setDataPresent(YES.getValue());
        return expectedTemplate;
    }

    private String buildRecipientLabel(String email) {
        return String.format("%s has also received this notification", email);
    }

    private CallbackRequest buildCallbackRequest(String state) {
        CallbackRequest callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().setState(state);
        return callbackRequest;
    }
}
