package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.OTHER;
import static uk.gov.hmcts.reform.fpl.model.PlacementOrderAndNotices.PlacementOrderAndNoticesType.PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMap;
import static uk.gov.hmcts.reform.fpl.utils.OrderIssuedNotificationTestHelper.getExpectedParametersMapForRepresentatives;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacementOrderAndNotices;
import static uk.gov.hmcts.reform.fpl.utils.matchers.JsonMatcher.eqJson;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractCallbackTest {

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String CASE_ID = "12345";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    PlacementSubmittedEventControllerTest() {
        super("placement");
    }

    @AfterEach
    void resetMocks() {
        reset(notificationClient);
    }

    @Nested
    class PlacementOrderNotification {
        private final Element<Child> child1 = testChild();
        private final Element<Child> child2 = element(child1.getValue().toBuilder()
            .party(child1.getValue().getParty().toBuilder()
                .dateOfBirth(dateNow().minusMonths(1))
                .lastName("Watson")
                .build())
            .build());

        private final DocumentReference child1Application = testDocumentReference();
        private final DocumentReference child2Application = testDocumentReference();

        private final Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
        private final Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

        @Test
        void shouldNotifyHmctsAdminWhenAddingNewChildPlacementAndCtscIsDisabled() throws Exception {
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
                PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                NOTIFICATION_REFERENCE);

            verify(notificationClient, never()).sendEmail(
                PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedTemplateParameters(),
                NOTIFICATION_REFERENCE);
        }

        @Test
        void shouldNotifyCtscAdminWhenAddingNewChildPlacementAndCtscIsEnabled() throws Exception {
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
                PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "FamilyPublicLaw+ctsc@gmail.com",
                expectedTemplateParameters(),
                NOTIFICATION_REFERENCE);
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
                PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
                "admin@family-court.com",
                expectedTemplateParameters(),
                NOTIFICATION_REFERENCE);
        }

        private Map<String, Object> expectedTemplateParameters() {
            return Map.of(
                "respondentLastName", "Watson",
                "caseUrl", "http://fake-url/cases/case-details/12345#Placement");
        }
    }

    @Nested
    class NoticeOfPlacementOrderNotification {
        private final Element<Child> childElement = testChild();
        private final Element<Placement> childPlacement = element(testPlacement(childElement, testDocumentReference()));
        private static final String ADMIN_EMAIL_ADDRESS = "admin@family-court.com";
        private static final String CTSC_EMAIL_ADDRESS = "FamilyPublicLaw+ctsc@gmail.com";
        private static final String DIGITAL_SERVED_REPRESENTATIVE_ADDRESS = "paul@example.com";
        private static final String EMAIL_SERVED_REPRESENTATIVE_ADDRESS = "bill@example.com";

        @Test
        void shouldSendEmailNotificationsWhenNewNoticeOfPlacementOrder() throws NotificationClientException {
            given(documentDownloadService.downloadDocument(anyString())).willReturn(PDF);

            postSubmittedEvent(callbackRequestWithEmptyCaseDetailsBefore());

            verify(notificationClient).sendEmail(
                eq(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE),
                eq(ADMIN_EMAIL_ADDRESS),
                eqJson(expectedParameters()),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq(LOCAL_AUTHORITY_1_INBOX),
                eqJson(expectedParameters()),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE,
                DIGITAL_SERVED_REPRESENTATIVE_ADDRESS,
                expectedParameters(),
                NOTIFICATION_REFERENCE);

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
                eq(EMAIL_SERVED_REPRESENTATIVE_ADDRESS),
                eqJson(getExpectedParametersMapForRepresentatives(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(),
                    false)),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(ADMIN_EMAIL_ADDRESS),
                eqJson(getExpectedParametersMap(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(), false)),
                eq(NOTIFICATION_REFERENCE));

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(CTSC_EMAIL_ADDRESS),
                any(),
                any());

            verifyNoMoreInteractions(notificationClient);
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
                eq(ADMIN_EMAIL_ADDRESS),
                any(),
                any());

            verify(notificationClient).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(CTSC_EMAIL_ADDRESS),
                eqJson(getExpectedParametersMap(IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER.getLabel(), false)),
                eq(NOTIFICATION_REFERENCE));
        }

        @Test
        void shouldNotSendEmailNotificationWhenNoChangesToOrder() throws NotificationClientException {
            postSubmittedEvent(callbackRequestWithMatchingCaseDetailsBefore());

            verify(notificationClient, never()).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq(LOCAL_AUTHORITY_1_INBOX),
                any(),
                any());

            verify(notificationClient, never()).sendEmail(
                eq(NOTICE_OF_PLACEMENT_ORDER_UPLOADED_TEMPLATE),
                eq(DIGITAL_SERVED_REPRESENTATIVE_ADDRESS),
                any(),
                any());

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_ADMIN),
                eq(ADMIN_EMAIL_ADDRESS),
                any(),
                any());

            verify(notificationClient, never()).sendEmail(
                eq(ORDER_ISSUED_NOTIFICATION_TEMPLATE_FOR_REPRESENTATIVES),
                eq(EMAIL_SERVED_REPRESENTATIVE_ADDRESS),
                any(),
                any());
        }

        private Map<String, Object> expectedParameters() {
            return Map.of(
                "respondentLastName", childElement.getValue().getParty().getLastName(),
                "caseUrl", "http://fake-url/cases/case-details/12345#Placement");
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
                    "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
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
                            .email(DIGITAL_SERVED_REPRESENTATIVE_ADDRESS)
                            .build()),
                        element(randomUUID(), Representative.builder()
                            .servingPreferences(EMAIL)
                            .email(EMAIL_SERVED_REPRESENTATIVE_ADDRESS)
                            .build())),
                    "children1", List.of(childElement),
                    "childrenList", childElement.getId()))
                .build();
        }
    }

    @Nested
    class SendDocumentEvent {
        private static final String SEND_DOCUMENT_EVENT = "internal-change-SEND_DOCUMENT";

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
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "respondents1", List.of(
                Map.of("value", Respondent.builder()
                    .party(RespondentParty.builder()
                        .lastName("Watson")
                        .build())
                    .build())));
    }
}
