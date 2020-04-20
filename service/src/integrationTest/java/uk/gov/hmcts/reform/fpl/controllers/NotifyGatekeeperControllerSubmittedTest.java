package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.service.notify.NotificationClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
public class NotifyGatekeeperControllerSubmittedTest extends AbstractControllerTest {
    private static final String GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";
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
    void shouldBuildGatekeeperNotificationTemplateWithCompleteValues() throws Exception {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");
        Map<String, Object> expectedGatekeeperParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("ordersAndDirections", ordersAndDirections)
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "same day")
            .put("urgentHearing", "Yes")
            .put("nonUrgentHearing", "No")
            .put("firstRespondentName", "Smith")
            .put("reference", "12345")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        postSubmittedEvent(buildCallbackRequest(SUBMITTED));

        verify(notificationClient).sendEmail(GATEKEEPER_SUBMISSION_TEMPLATE, GATEKEEPER_EMAIL,
            expectedGatekeeperParameters, "12345");
    }

    private CallbackRequest buildCallbackRequest(String state) {
        CallbackRequest callbackRequest = callbackRequest();
        callbackRequest.getCaseDetails().setState(state);
        return callbackRequest;
    }
}
