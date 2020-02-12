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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testPlacement;

@ActiveProfiles("integration-test")
@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementSubmittedEventControllerTest extends AbstractControllerTest {
    @MockBean
    private NotificationClient notificationClient;
    @MockBean
    private CoreCaseDataService coreCaseDataService;

    private static final Long CASE_ID = 12345L;
    private static final String CASE_REFERENCE = "12345";
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String SEND_DOCUMENT_EVENT = "internal-change:SEND_DOCUMENT";

    private final DocumentReference documentReference = DocumentReference.builder().build();

    PlacementSubmittedEventControllerTest() {
        super("placement");
    }

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
                .id(CASE_ID)
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
            eq(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), eq("admin@family-court.com"),
            eq(expectedTemplateParameters()), eq(CASE_REFERENCE));
        verify(coreCaseDataService).triggerEvent(null, null, CASE_ID, SEND_DOCUMENT_EVENT, Map.of(
            "documentToBeSent", documentReference
        ));
    }

    @Test
    void shouldNotSendANotificationWhenUploadingPreviousChildPlacement() throws Exception {
        Element<Child> child1 = testChild();
        DocumentReference child1Application = testDocument();
        Element<Placement> child1Placement = element(testPlacement(child1, child1Application));

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(CASE_ID)
                .data(buildPlacementData(List.of(child1), List.of(child1Placement), child1.getId()))
                .build())
            .caseDetailsBefore(CaseDetails.builder()
                .id(CASE_ID)
                .data(buildPlacementData(List.of(child1), List.of(child1Placement), child1.getId()))
                .build())
            .build();

        postSubmittedEvent(callbackRequest);

        verify(notificationClient, never()).sendEmail(
            eq(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), eq("admin@family-court.com"),
            eq(expectedTemplateParameters()), eq(CASE_REFERENCE));
        verify(coreCaseDataService).triggerEvent(null, null, CASE_ID, SEND_DOCUMENT_EVENT, Map.of(
            "documentToBeSent", documentReference
        ));
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
        return ImmutableMap.of(
            "respondentLastName", RESPONDENT_SURNAME,
            "caseUrl", "http://fake-url/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345"
        );
    }

    private Map<String, Object> buildNotificationData() {
        return ImmutableMap.of(
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
