package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractControllerTest {

    @MockBean
    private NotificationClient notificationClient;

    PlacementSubmittedEventControllerTest() {
        super("placement");
    }

    @AfterEach
    void resetMocks() {
        reset(notificationClient);
    }

    @Nested
    class PlacementOrderNotification {

        @Test
        void shouldSendNotificationWhenAddingNewChildPlacement() throws Exception {
            Element<Child> child1 = testChild();
            Element<Child> child2 = testChild();

            DocumentReference child1Application = testDocument();
            DocumentReference child2Application = testDocument();

            Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
            Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .id(12345L)
                    .data(ImmutableMap.<String, Object>builder()
                        .putAll(buildNotificationData())
                        .putAll(buildPlacementData(List.of(child1, child2), List.of(child2Placement, child1Placement),
                            child2.getId()))
                        .build())
                    .build())
                .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
                .build();

            postSubmittedEvent(callbackRequest);

            verify(notificationClient, times(1)).sendEmail(
                eq(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE),
                eq("admin@family-court.com"),
                eq(expectedTemplateParameters()),
                eq("12345"));
        }

        @Test
        void shouldNotSendANotificationWhenUploadingPreviousChildPlacement() throws Exception {
            Element<Child> child1 = testChild();
            DocumentReference child1Application = testDocument();
            Element<Placement> child1Placement = element(testPlacement(child1, child1Application));

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .data(buildPlacementData(List.of(child1), List.of(child1Placement), child1.getId()))
                    .build())
                .caseDetailsBefore(CaseDetails.builder()
                    .data(buildPlacementData(List.of(child1), List.of(child1Placement), child1.getId()))
                    .build())
                .build();

            postSubmittedEvent(callbackRequest);

            verify(notificationClient, never()).sendEmail(
                eq(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE),
                eq("admin@family-court.com"),
                eq(expectedTemplateParameters()),
                eq("12345"));
        }

        private Map<String, Object> buildPlacementData(List<Element<Child>> children,
                                                       List<Element<Placement>> placements,
                                                       UUID childID) {
            return Map.of(
                "children1", children,
                "confidentialPlacements", placements,
                "childrenList", childID);
        }

        private Map<String, Object> expectedTemplateParameters() {
            return Map.of(
                "respondentLastName", "Watson",
                "caseUrl", String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, 12345L));
        }

        private Map<String, Object> buildNotificationData() {
            return Map.of(
                "caseLocalAuthority", "example",
                "respondents1", List.of(
                    Map.of("value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName("Watson")
                            .build())
                        .build())));
        }
    }

    @Nested
    class NoticeOfPlacementOrderNotification {
        private final Element<Child> childElement = testChild();
        private final Element<Placement> childPlacement = element(testPlacement(childElement, testDocument()));

        @Test
        void shouldSendEmailNotificationsWhenNewNoticeOfPlacementOrder() throws NotificationClientException {
            postSubmittedEvent(callbackRequestWithEmptyCaseDetailsBefore());

            verify(notificationClient, times(1)).sendEmail(
                eq(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE),
                eq("admin@family-court.com"),
                eq(expectedParameters()),
                eq("1"));

            verify(notificationClient, times(1)).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq("local-authority@local-authority.com"),
                eq(expectedParameters()),
                eq("1"));

            verify(notificationClient, times(1)).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq("representative@example.com"),
                eq(expectedParameters()),
                eq("1"));
        }

        @Test
        void shouldNotSendEmailNotificationWhenNoChangesToOrder() throws NotificationClientException {
            postSubmittedEvent(callbackRequestWithMatchingCaseDetailsBefore());

            verify(notificationClient, never()).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq("local-authority@local-authority.com"),
                eq(expectedParameters()),
                eq("1"));

            verify(notificationClient, never()).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq("representative@example.com"),
                eq(expectedParameters()),
                eq("1"));
        }

        private Map<String, Object> expectedParameters() {
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

        private CallbackRequest callbackRequestWithEmptyCaseDetailsBefore() {
            UUID representativeId = randomUUID();
            Respondent respondent = respondent();
            respondent.addRepresentative(representativeId);

            return CallbackRequest.builder()
                .caseDetails(populatedCaseDetails(representativeId, respondent))
                .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
                .build();
        }

        private CallbackRequest callbackRequestWithMatchingCaseDetailsBefore() {
            UUID representativeId = randomUUID();
            Respondent respondent = respondent();
            respondent.addRepresentative(representativeId);

            return CallbackRequest.builder()
                .caseDetails(populatedCaseDetails(representativeId, respondent))
                .caseDetailsBefore(populatedCaseDetails(representativeId, respondent))
                .build();
        }

        private CaseDetails populatedCaseDetails(UUID representativeId, Respondent respondent) {
            return CaseDetails.builder()
                .id(1L)
                .data(Map.of(
                    "caseLocalAuthority", "example",
                    "confidentialPlacements", List.of(element(Placement.builder()
                        .childId(childElement.getId())
                        .application(childPlacement.getValue().getApplication())
                        .orderAndNotices(wrapElements(PlacementOrderAndNotices.builder()
                            .type(NOTICE_OF_PLACEMENT_ORDER)
                            .document(DocumentReference.buildFromDocument(document()))
                            .build()))
                        .build())),
                    "respondents1", wrapElements(respondent),
                    "representatives", List.of(element(representativeId, Representative.builder()
                        .servingPreferences(DIGITAL_SERVICE)
                        .email("representative@example.com")
                        .build())),
                    "children1", List.of(childElement),
                    "childrenList", childElement.getId()))
                .build();
        }
    }
}
