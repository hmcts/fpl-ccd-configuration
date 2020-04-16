package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.Long.parseLong;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.OTHER;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForCaseRoleUsers;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacementOrderAndNotices;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractControllerTest {

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String CASE_ID = "12345";

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> dataCaptor;

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
        void shouldNotifyHmctsAdminWhenAddingNewChildPlacementAndCtscIsDisabled() throws Exception {
            Element<Child> child1 = testChild();
            Element<Child> child2 = testChild();

            DocumentReference child1Application = testDocumentReference();
            DocumentReference child2Application = testDocumentReference();

            Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
            Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .id(parseLong(CASE_ID))
                    .data(ImmutableMap.<String, Object>builder()
                        .putAll(buildNotificationData())
                        .putAll(buildPlacementData(List.of(child1, child2), List.of(child2Placement, child1Placement),
                            child2.getId()))
                        .build())
                    .build())
                .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
                .build();

            postSubmittedEvent(callbackRequest);

            verify(notificationClient).sendEmail(
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                CASE_ID);

            verify(notificationClient, never()).sendEmail(
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedTemplateParameters(),
                CASE_ID);
        }

        @Test
        void shouldNotifyCtscAdminWhenAddingNewChildPlacementAndCtscIsEnabled() throws Exception {
            Element<Child> child1 = testChild();
            Element<Child> child2 = testChild();

            DocumentReference child1Application = testDocumentReference();
            DocumentReference child2Application = testDocumentReference();

            Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
            Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder()
                    .id(parseLong(CASE_ID))
                    .data(ImmutableMap.<String, Object>builder()
                        .putAll(buildNotificationData())
                        .putAll(buildPlacementData(List.of(child1, child2), List.of(child2Placement, child1Placement),
                            child2.getId()))
                        .put("sendToCtsc", "Yes")
                        .build())
                    .build())
                .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
                .build();

            postSubmittedEvent(callbackRequest);

            verify(notificationClient, never()).sendEmail(
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                CASE_ID);

            verify(notificationClient).sendEmail(
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedTemplateParameters(),
                String.valueOf(CASE_ID));
        }

        @Test
        void shouldNotSendANotificationWhenUploadingPreviousChildPlacement() throws Exception {
            Element<Child> child1 = testChild();
            DocumentReference child1Application = testDocumentReference();
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
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                CASE_ID);
        }

        private Map<String, Object> expectedTemplateParameters() {
            return Map.of(
                "respondentLastName", "Watson",
                "caseUrl",
                String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, parseLong(CASE_ID)));
        }
    }

    @Nested
    class NoticeOfPlacementOrderNotification {
        private final Element<Child> childElement = testChild();
        private final Element<Placement> childPlacement = element(testPlacement(childElement, testDocumentReference()));

        @Test
        void shouldSendEmailNotificationsWhenNewNoticeOfPlacementOrder() throws NotificationClientException {
            given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

            postSubmittedEvent(callbackRequestWithEmptyCaseDetailsBefore());

            verify(notificationClient).sendEmail(
                NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedParameters(),
                CASE_ID);

            verify(notificationClient).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                "local-authority@local-authority.com",
                expectedParameters(),
                CASE_ID);

            verify(notificationClient).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                "representative@example.com",
                expectedParameters(),
                CASE_ID);

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq("admin@family-court.com"),
                dataCaptor.capture(),
                eq(CASE_ID));

            assertEquals(dataCaptor.getValue(),
                getExpectedParametersForCaseRoleUsers(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq("FamilyPublicLaw+ctsc@gmail.com"),
                dataCaptor.capture(),
                eq(CASE_ID));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
                eq("bill@example.com"),
                dataCaptor.capture(),
                eq(CASE_ID));

            assertEquals(dataCaptor.getValue(),
                getExpectedParametersForRepresentatives(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));

            verifyZeroInteractions(notificationClient);
        }

        @Test
        void shouldSendNotificationToCtscAdminWhenNewNoticeOfPlacementOrderAndCtscIsEnabled()
            throws NotificationClientException {
            given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

            UUID representativeId = randomUUID();
            Respondent respondent = respondent();

            CaseDetails caseDetails = populatedCaseDetails(representativeId, respondent);

            caseDetails.setData(ImmutableMap.<String, Object>builder()
                .putAll(caseDetails.getData())
                .put("sendToCtsc", "Yes")
                .build());

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(CaseDetails.builder().data(new HashMap<>()).build())
                .build();

            postSubmittedEvent(callbackRequest);

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq("admin@family-court.com"),
                dataCaptor.capture(),
                eq(CASE_ID));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq("FamilyPublicLaw+ctsc@gmail.com"),
                dataCaptor.capture(),
                eq(CASE_ID));

            assertEquals(dataCaptor.getValue(),
                getExpectedParametersForCaseRoleUsers(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(), false));

        }

        @Test
        void shouldNotSendEmailNotificationWhenNoChangesToOrder() throws NotificationClientException {
            postSubmittedEvent(callbackRequestWithMatchingCaseDetailsBefore());

            verify(notificationClient, never()).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                "local-authority@local-authority.com",
                expectedParameters(),
                CASE_ID);

            verify(notificationClient, never()).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                "representative@example.com",
                expectedParameters(),
                CASE_ID);
        }

        private Map<String, Object> expectedParameters() {
            return Map.of(
                "respondentLastName", "Jones",
                "caseUrl",
                String.format("%s/case/%s/%s/%s", "http://fake-url", JURISDICTION, CASE_TYPE, parseLong(CASE_ID)));
        }

        private Respondent respondent() {
            return Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .lastName("Jones")
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
                .id(parseLong(CASE_ID))
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
                            .build()),
                        element(randomUUID(), Representative.builder()
                            .servingPreferences(EMAIL)
                            .email("bill@example.com")
                            .build())),
                    "children1", List.of(childElement),
                    "childrenList", childElement.getId()))
                .build();
        }
    }

    @Nested
    class SendDocumentEvent {
        private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";

        @Test
        void shouldSendDocumentForEachUpdatedPlacementOrder() {
            Element<Child> child = testChild();
            DocumentReference updatedDocumentReference = DocumentReference.builder().binaryUrl("updated_url0").build();
            PlacementOrderAndNotices updatedPlacementOrderAndNotices = PlacementOrderAndNotices.builder()
                .type(PLACEMENT_ORDER)
                .document(updatedDocumentReference)
                .build();

            List<PlacementOrderAndNotices> placementOrderAndNoticesBefore = List.of(
                testPlacementOrderAndNotices(PLACEMENT_ORDER, "url0"),
                testPlacementOrderAndNotices(PLACEMENT_ORDER, "url1"),
                testPlacementOrderAndNotices(NOTICE_OF_PROCEEDINGS, "url2"),
                testPlacementOrderAndNotices(NOTICE_OF_HEARING, "url3"),
                testPlacementOrderAndNotices(OTHER, "url4"),
                testPlacementOrderAndNotices(NOTICE_OF_PLACEMENT_ORDER, "url5"));
            List<PlacementOrderAndNotices> placementOrderAndNotices = List.of(
                updatedPlacementOrderAndNotices,
                testPlacementOrderAndNotices(PLACEMENT_ORDER, "url1"),
                testPlacementOrderAndNotices(NOTICE_OF_PROCEEDINGS, "updated_url2"),
                testPlacementOrderAndNotices(NOTICE_OF_HEARING, "updated_url3"),
                testPlacementOrderAndNotices(OTHER, "updated_url4"),
                testPlacementOrderAndNotices(NOTICE_OF_PLACEMENT_ORDER, "updated_url5"));

            CaseDetails caseDetailsBefore = buildCaseDetailsWithPlacementOrderAndNotices(placementOrderAndNoticesBefore,
                child);
            CaseDetails caseDetails = buildCaseDetailsWithPlacementOrderAndNotices(placementOrderAndNotices, child);

            CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

            postSubmittedEvent(callbackRequest);

            verify(coreCaseDataService).triggerEvent("PUBLICLAW",
                "CARE_SUPERVISION_EPO",
                parseLong(CASE_ID),
                SEND_DOCUMENT_EVENT,
                Map.of("documentToBeSent", updatedDocumentReference));
        }

        private CaseDetails buildCaseDetailsWithPlacementOrderAndNotices(
            List<PlacementOrderAndNotices> placementOrderAndNoticesList, Element<Child> child) {
            Placement placement = testPlacement(child, placementOrderAndNoticesList);

            return buildCaseDetails((buildPlacementData(List.of(child),
                List.of(element(placement)),
                child.getId())));
        }

        private CaseDetails buildCaseDetails(Map<String, Object> data) {
            return CaseDetails.builder()
                .id(parseLong(CASE_ID))
                .jurisdiction("PUBLICLAW")
                .caseTypeId("CARE_SUPERVISION_EPO")
                .data(ImmutableMap.<String, Object>builder().putAll(buildNotificationData())
                    .putAll(data)
                    .build())
                .build();
        }
    }

    private Map<String, Object> buildPlacementData(List<Element<Child>> children,
                                                   List<Element<Placement>> placements,
                                                   UUID childID) {
        return Map.of(
            "children1", children,
            "confidentialPlacements", placements,
            "childrenList", childID);
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
