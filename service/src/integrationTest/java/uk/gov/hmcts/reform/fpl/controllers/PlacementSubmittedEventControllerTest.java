package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractControllerTest {

    @MockBean
    private NotificationClient notificationClient;

    PlacementSubmittedEventControllerTest() {
        super("placement");
    }

    //TODO neaten up.
    @Test
    void shouldSendEmailNotificationWhenNewOrder() throws NotificationClientException {
        UUID uuid = randomUUID();

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Nelson")
                .build())
            .build();
        respondent.addRepresentative(uuid);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(1L)
                .data(Map.of(
                    "caseLocalAuthority", "example",
                    "confidentialPlacements", List.of(element(Placement.builder()
                        .orderAndNotices(wrapElements(PlacementOrderAndNotices.builder()
                            .type(NOTICE_OF_PLACEMENT_ORDER)
                            .build()))
                        .build())),
                    "respondents1", wrapElements(respondent),
                    "representatives", List.of(element(uuid, Representative.builder()
                        .servingPreferences(DIGITAL_SERVICE)
                        .email("representative@example.com")
                        .build()))))
                .build())
            .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
            .build();

        Map<String, Object> parameters = Map.of(
            "respondentLastName", "Nelson",
            "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 1L));

        postSubmittedEvent(callbackRequest);

        verify(notificationClient, times(1)).sendEmail(
            eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
            eq("local-authority@local-authority.com"),
            eq(parameters),
            eq("1"));

        verify(notificationClient, times(1)).sendEmail(
            eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
            eq("representative@example.com"),
            eq(parameters),
            eq("1"));
    }
}
