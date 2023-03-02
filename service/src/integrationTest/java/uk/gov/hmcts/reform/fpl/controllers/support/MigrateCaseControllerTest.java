package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private TaskListService taskListService;

    @MockBean
    private TaskListRenderer taskListRenderer;

    @MockBean
    private CaseSubmissionChecker caseSubmissionChecker;

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1195 {

        private final String migrationId = "DFPL-1195";
        private final long validCaseId = 1655911528192218L;

        @Test
        void shouldRemoveAllPlacementCollections() {
            List<Element<Placement>> placements = List.of(
                element(Placement.builder()
                    .application(testDocumentReference())
                    .build()),
                element(Placement.builder()
                    .application(testDocumentReference())
                    .build())
            );
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .placementEventData(PlacementEventData.builder()
                    .placements(placements)
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getPlacementEventData().getPlacements()).isEmpty();
            assertThat(response.getData()).extracting("placementsNonConfidential", "placementsNonConfidentialNotices")
                .containsExactly(null, null);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1277 {
        private final String migrationId = "DFPL-1277";
        private final long validCaseId = 1659933720451883L;
        private final UUID placementToRemove = UUID.fromString("f1b6d2d8-e960-4b36-a9ae-56723c25ac31");
        private final UUID placementToRemain = UUID.randomUUID();
        private final DocumentReference documentToRemain = testDocumentReference();

        @Test
        void shouldOnlyRemoveSelectPlacement() {
            List<Element<Placement>> placements = List.of(
                element(placementToRemove, Placement.builder()
                    .application(testDocumentReference())
                    .build()),
                element(placementToRemain, Placement.builder()
                    .application(documentToRemain)
                    .build())
            );

            List<Element<Placement>> placementsRemaining = List.of(
                element(placementToRemain, Placement.builder()
                    .application(documentToRemain)
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .placementEventData(PlacementEventData.builder()
                    .placements(placements)
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getPlacementEventData().getPlacements()).isEqualTo(placementsRemaining);
            assertThat(responseData.getPlacementEventData()
                .getPlacementsNonConfidential(true)).isEqualTo(placementsRemaining);
            assertThat(responseData.getPlacementEventData()
                .getPlacementsNonConfidential(false)).isEqualTo(placementsRemaining);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1218 {
        private final String migrationId = "DFPL-1218";
        private final long validCaseId = 1651753104228873L;
        private final UUID placementToRemove = UUID.fromString("e32706b1-22e5-4bd9-ba05-355fe69028d0");
        private final UUID placementToRemain = UUID.randomUUID();
        private final DocumentReference documentToRemain = testDocumentReference();

        @Test
        void shouldOnlyRemoveSelectPlacement() {
            List<Element<Placement>> placements = List.of(
                element(placementToRemove, Placement.builder()
                    .application(testDocumentReference())
                    .build()),
                element(placementToRemain, Placement.builder()
                    .application(documentToRemain)
                    .build())
            );

            List<Element<Placement>> placementsRemaining = List.of(
                element(placementToRemain, Placement.builder()
                    .application(documentToRemain)
                    .build())
            );

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .placementEventData(PlacementEventData.builder()
                    .placements(placements)
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getPlacementEventData().getPlacements()).isEqualTo(placementsRemaining);
            assertThat(responseData.getPlacementEventData()
                .getPlacementsNonConfidential(true)).isEqualTo(placementsRemaining);
            assertThat(responseData.getPlacementEventData()
                .getPlacementsNonConfidential(false)).isEqualTo(placementsRemaining);
        }
    }
}
