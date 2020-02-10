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
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
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
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
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

    @Test
    void shouldSendEmailNotificationWhenNewOrder() throws NotificationClientException {
        UUID uuid = randomUUID();
        Respondent respondent = respondent();
        respondent.addRepresentative(uuid);

        postSubmittedEvent(callbackRequest(uuid, respondent));

        verify(notificationClient, times(1)).sendEmail(
            eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
            eq("local-authority@local-authority.com"),
            eq(parameters()),
            eq("1"));

        verify(notificationClient, times(1)).sendEmail(
            eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
            eq("representative@example.com"),
            eq(parameters()),
            eq("1"));
    }

    private Map<String, Object> parameters() {
        return Map.of(
            "respondentLastName", "Nelson",
            "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 1L));
    }

    private Respondent respondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Nelson")
                .build())
            .build();
    }

    private CallbackRequest callbackRequest(UUID uuid, Respondent respondent) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(1L)
                .data(Map.of(
                    "caseLocalAuthority", "example",
                    "confidentialPlacements", List.of(element(Placement.builder()
                        .orderAndNotices(wrapElements(PlacementOrderAndNotices.builder()
                            .type(NOTICE_OF_PLACEMENT_ORDER)
                            .document(DocumentReference.buildFromDocument(document()))
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
    }
}
