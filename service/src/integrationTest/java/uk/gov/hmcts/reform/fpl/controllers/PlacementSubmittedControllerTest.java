package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.service.notify.NotificationClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedControllerTest extends AbstractControllerTest {
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    private static final Long CASE_ID = 123456L;
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";

    private final DocumentReference documentReference = testDocument();

    PlacementSubmittedControllerTest() {
        super("placement");
    }

    @Test
    void shouldNotifyHmctsAdminWhenAddingNewChildPlacementAndCtscIsDisabled() throws Exception {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        Element<Placement> child1Placement = element(testPlacement(child1, testDocument()));
        Element<Placement> child2Placement = element(testPlacement(child2, testDocument()));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .jurisdiction("PUBLICLAW")
                .caseTypeId("CARE_SUPERVISION_EPO")
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
            NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, "admin@family-court.com",
            expectedTemplateParameters(), String.valueOf(CASE_ID));

        verify(notificationClient, never()).sendEmail(
            NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, "FamilyPublicLaw+ctsc@gmail.com",
            expectedTemplateParameters(), String.valueOf(CASE_ID));

        verify(coreCaseDataService).triggerEvent(null, null, CASE_ID, SEND_DOCUMENT_EVENT, Map.of(
            "documentToBeSent", documentReference
        ));

        verify(notificationClient).sendEmail(NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE,
            "admin@family-court.com",
            expectedTemplateParameters(),
            String.valueOf(CASE_ID));
    }

    @Test
    void shouldNotifyCtscAdminWhenAddingNewChildPlacementAndCtscIsEnabled() throws Exception {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        DocumentReference child1Application = testDocument();
        DocumentReference child2Application = testDocument();

        Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
        Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
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
            NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE, "admin@family-court.com",
            expectedTemplateParameters(), (String.valueOf(CASE_ID)));

        verify(notificationClient).sendEmail(
            (NEW_PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), "FamilyPublicLaw+ctsc@gmail.com",
            expectedTemplateParameters(), String.valueOf(CASE_ID));
    }

    @Test
    void shouldNotSendANotificationWhenUploadingPreviousChildPlacement() throws Exception {
        Element<Child> child1 = testChild();
        Element<Placement> child1Placement = element(testPlacement(child1, testDocument()));
        CaseDetails unchangedCaseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .jurisdiction("PUBLICLAW")
            .caseTypeId("CARE_SUPERVISION_EPO")
            .data(ImmutableMap.<String, Object>builder().putAll(buildNotificationData())
                .putAll(buildPlacementData(List.of(child1), List.of(child1Placement), child1.getId()))
                .build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(unchangedCaseDetails)
            .caseDetailsBefore(unchangedCaseDetails.toBuilder().build())
            .build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never()).sendEmail(anyString(), any(), any(), any());
    }

    @Test
    void shouldTriggerSendDocumentIfPlacementApplicationHasChanged() {
        DocumentReference expectedDocumentReference = DocumentReference.builder().binaryUrl("binary_url_new").build();
        Element<Child> child = testChild();

        Placement placementBefore = testPlacement(child, DocumentReference.builder().binaryUrl("binary_url").build());
        Placement placement = testPlacement(child, expectedDocumentReference);

        Map<String, Object> caseDataBefore = buildPlacementData(List.of(child),
            wrapElements(placementBefore),
            child.getId());
        Map<String, Object> caseData = buildPlacementData(List.of(child), wrapElements(placement), child.getId());

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(CASE_ID)
            .jurisdiction("PUBLICLAW")
            .caseTypeId("CARE_SUPERVISION_EPO")
            .data(ImmutableMap.<String, Object>builder()
                .putAll(buildNotificationData())
                .putAll(caseDataBefore)
                .build())
            .build();
        CaseDetails caseDetails = caseDetailsBefore.toBuilder()
            .data(ImmutableMap.<String, Object>builder().putAll(buildNotificationData()).putAll(caseData).build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService).triggerEvent("PUBLICLAW",
            "CARE_SUPERVISION_EPO",
            CASE_ID,
            SEND_DOCUMENT_EVENT,
            Map.of("documentToBeSent", expectedDocumentReference));
    }

    @Test
    void shouldNotTriggerSendDocumentIfPlacementApplicationHasNotChanged() {
        Element<Child> child = testChild();
        Element<Placement> childPlacement = element(testPlacement(child, testDocument()));
        CaseDetails unchangedCaseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .jurisdiction("PUBLICLAW")
            .caseTypeId("CARE_SUPERVISION_EPO")
            .data(ImmutableMap.<String, Object>builder()
                .putAll(buildNotificationData())
                .putAll(buildPlacementData(List.of(child), List.of(childPlacement), child.getId()))
                .build())
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(unchangedCaseDetails)
            .caseDetailsBefore(unchangedCaseDetails.toBuilder().build())
            .build();

        postSubmittedEvent(callbackRequest);

        verify(coreCaseDataService, never()).triggerEvent(any(), any(), any(), any());
    }

    private Map<String, Object> buildPlacementData(List<Element<Child>> children,
                                                  List<Element<Placement>> placements,
                                                  UUID childID) {
        return Map.of(
            "children1", children,
            "placements", placements,
            "placement", Placement.builder().application(documentReference).build(),
            "childrenList", childID);
    }

    private Map<String, Object> expectedTemplateParameters() {
        return Map.of(
            "respondentLastName", RESPONDENT_SURNAME,
            "caseUrl", String.format("http://fake-url/case/%s/%s/%s", JURISDICTION, CASE_TYPE, CASE_ID)
        );
    }

    private Map<String, Object> buildNotificationData() {
        return Map.of(
            "caseLocalAuthority", LOCAL_AUTHORITY_CODE,
            "respondents1", List.of(
                ImmutableMap.of(
                    "value", Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName(RESPONDENT_SURNAME)
                            .build())
                        .build()))
        );
    }
}
