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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1064 {
        private final long invalidCaseId = 1643728359986136L;
        private final long validCaseId = 1659605693892067L;
        private final String migrationId = "DFPL-1064";

        @Test
        void shouldThrowExceptionIfWrongCaseId() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, case id not present in allowed list",
                    migrationId, invalidCaseId));
        }

        @Test
        void shouldSetSendToCtscToNo() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getSendToCtsc()).isEqualTo(YesNo.NO.getValue());
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1029 {

        final String migrationId = "DFPL-1029";
        final long expectedCaseId = 1638876373455956L;
        final long incorrectCaseId = 111111111111111L;

        @Test
        void shouldThrowExceptionWhenIncorrectCaseId() {
            CaseData caseData = CaseData.builder().id(incorrectCaseId).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, incorrectCaseId, expectedCaseId));
        }

        @Test
        void shouldSetCaseStateToCaseManagement() {
            CaseData caseData = CaseData.builder().id(expectedCaseId).build();

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);

            // pick a few of the temp fields from ManageOrderDocumentScopedFieldsCalculator and set on CaseDetails
            caseDetails.getData().put("others_label", "test");
            caseDetails.getData().put("appointedGuardians_label", "test");
            caseDetails.getData().put("manageOrdersCafcassRegion", "test");

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            // check that the migration has successfully removed them
            assertThat(response.getData()).extracting("others_label").isNull();
            assertThat(response.getData()).extracting("appointedGuardians_label").isNull();
            assertThat(response.getData()).extracting("manageOrdersCafcassRegion").isNull();
        }

    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1161 {

        private final String migrationId = "DFPL-1161";
        private final long validCaseId = 1660209462518487L;

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
    class Dfpl1204 {

        private final String migrationId = "DFPL-1204";
        private final long validCaseId = 1638528543085011L;
        private final UUID placementToRemove = UUID.fromString("88125c8b-8466-4af4-967f-197c3b82773c");
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
