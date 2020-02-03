package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
class PlacementAboutToSubmitEventControllerTest extends AbstractControllerTest {
    @MockBean
    private NotificationClient notificationClient;

    private static final Long CASE_ID = 12345L;
    private static final String CASE_REFERENCE = "12345";
    private static final String RESPONDENT_SURNAME = "Watson";
    private static final String LOCAL_AUTHORITY_CODE = "example";

    PlacementAboutToSubmitEventControllerTest() {
        super("placement");
    }

    @Test
    void shouldUpdatePlacementAndSendNotificationWhenOverwritingExistingChildPlacement() throws Exception {
        Element<Child> child1 = testChild();
        Element<Child> child2 = testChild();

        DocumentReference child1Application = testDocument();
        DocumentReference child2Application = testDocument();

        Element<Placement> child1Placement = element(testPlacement(child1, child1Application));
        Element<Placement> child2Placement = element(testPlacement(child2, child2Application));

        DocumentReference child2NewApplication = testDocument();
        Element<Placement> child2NewPlacement = element(child2Placement.getId(),
            testPlacement(child2, child2NewApplication));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(ImmutableMap.<String, Object>builder()
                .putAll(buildNotificationData())
                .putAll(ImmutableMap.of(
                    "children1", List.of(child1, child2),
                    "placements", List.of(child2Placement, child1Placement),
                    "placement", child2NewPlacement.getValue(),
                    "childrenList", child2.getId()))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        Map<String, Object> updatedCaseDetails = callbackResponse.getData();

        assertThat(updatedCaseDetails).doesNotContainKey("placement");
        assertThat(updatedCaseDetails).doesNotContainKey("placementChildName");
        assertThat(updatedCaseDetails).doesNotContainKey("singleChild");

        assertThat(updatedCaseDetails.get("placements")).isEqualTo(List.of(
            expectedPlacement(child1Placement, child1Application),
            expectedPlacement(child2NewPlacement, child2NewApplication)
        ));

        verify(notificationClient).sendEmail(
            eq(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), eq("admin@family-court.com"),
            eq(expectedTemplateParameters()), eq(CASE_REFERENCE));
    }

    @Test
    void shouldNotSendANotificationWhenUploadingPreviousChildPlacement() throws Exception {
        Element<Child> child1 = testChild();
        DocumentReference child1Application = testDocument();
        Element<Placement> child1Placement = element(testPlacement(child1, child1Application));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "children1", List.of(child1),
                "placements", List.of(child1Placement, child1Placement),
                "placement", child1Placement.getValue(),
                "childrenList", child1.getId()))
            .build();

        postAboutToSubmitEvent(caseDetails);

        verify(notificationClient, never()).sendEmail(
            eq(PLACEMENT_APPLICATION_NOTIFICATION_TEMPLATE), eq("admin@family-court.com"),
            eq(expectedTemplateParameters()), eq(CASE_REFERENCE));
    }

    private Map<String, Object> expectedPlacement(Element<Placement> placement, DocumentReference application) {
        return Map.of("id", placement.getId().toString(),
            "value", Map.of("placementChildName", placement.getValue().getChildName(),
                "placementChildId", placement.getValue().getChildId().toString(),
                "placementApplication", Map.of(
                    "document_binary_url", application.getBinaryUrl(),
                    "document_filename", application.getFilename(),
                    "document_url", application.getUrl()
                )));
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
