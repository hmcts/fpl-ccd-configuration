package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.PopulateStandardDirectionsEvent;
import uk.gov.hmcts.reform.fpl.handlers.PopulateStandardDirectionsHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.GATEKEEPER_SUBMISSION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;

@ActiveProfiles("integration-test")
@WebMvcTest(NotifyGatekeeperController.class)
@OverrideAutoConfiguration(enabled = true)
class NotifyGatekeeperTest extends AbstractControllerTest {
    private static final String FPL_GATEKEEPER_EMAIL = "FamilyPublicLaw+gatekeeper@gmail.com";
    private static final String CAFCASS_GATEKEEPER_EMAIL = "Cafcass+gatekeeper@gmail.com";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private PopulateStandardDirectionsHandler populateStandardDirectionsHandler;

    NotifyGatekeeperTest() {
        super("notify-gatekeeper");
    }

    @Test
    void shouldReturnErrorsWhenFamilymanNumberIsNotProvided() {
        ImmutableMap<String, Object> data = ImmutableMap.of(
            "data", "test data"
        );

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails(data));

        assertThat(callbackResponse.getErrors()).containsExactly("Enter Familyman case number");
    }

    @Test
    void shouldResetGateKeeperEmailCollection() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(callbackRequest());

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        List<Element<EmailAddress>> gateKeeperEmailAddresses = caseData.getGateKeeperEmails();

        assertThat(gateKeeperEmailAddresses.size()).isEqualTo(1);
        assertThat(gateKeeperEmailAddresses.get(0).getValue().getEmail()).isEqualTo("");
    }

    @Test
    void shouldReturnPopulatedDirectionsByRoleInSubmittedCallback() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(populateStandardDirectionsHandler).populateStandardDirections(any(
            PopulateStandardDirectionsEvent.class));
    }

    @Test
    void shouldBuildGatekeeperNotificationTemplateWithCompleteValues() throws Exception {
        postSubmittedEvent(callbackRequest());

        verify(notificationClient, times(1)).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, FPL_GATEKEEPER_EMAIL,
            buildExpectedParameters(CAFCASS_GATEKEEPER_EMAIL), "12345");

        verify(notificationClient, times(1)).sendEmail(
            GATEKEEPER_SUBMISSION_TEMPLATE, CAFCASS_GATEKEEPER_EMAIL,
            buildExpectedParameters(FPL_GATEKEEPER_EMAIL), "12345");
    }

    private Map<String, Object> buildExpectedParameters(String email) {
        List<String> ordersAndDirections = ImmutableList.of("Emergency protection order",
            "Contact with any named person");

        return ImmutableMap.<String, Object>builder()
            .put("reference", "12345")
            .put("ordersAndDirections", ordersAndDirections)
            .put("gatekeeper_recipients", buildRecipientLabel(email))
            .put("urgentHearing", "Yes")
            .put("fullStop", "No")
            .put("timeFrameValue", "same day")
            .put("localAuthority", "Example Local Authority")
            .put("timeFramePresent", "Yes")
            .put("nonUrgentHearing", "No")
            .put("caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .put("firstRespondentName", "Smith")
            .put("dataPresent", "Yes")
            .build();
    }

    private String buildRecipientLabel(String email) {
        return String.format("%s has also received this notification", email);
    }

    private CaseDetails caseDetails(ImmutableMap<String, Object> caseData) {
        return CaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
    }
}
