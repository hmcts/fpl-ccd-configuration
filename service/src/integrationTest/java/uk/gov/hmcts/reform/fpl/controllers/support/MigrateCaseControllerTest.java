package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
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
    class Dfpl1238 {

        private final String migrationId = "DFPL-1238";
        private final long validCaseId = 1635423187428763L;

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
    class Dfpl1241 {

        private final String migrationId = "DFPL-1241";
        private final long validCaseId = 1652968793683878L;

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
    class Dfpl1244 {

        private final String migrationId = "DFPL-1244";
        private final long validCaseId = 1644912253936021L;

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

    @Nested
    class Dfpl1352 {

        private final String migrationId = "DFPL-1352";

        @Test
        void shouldThrowExceptionWhenInHighCourt() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court(RCJ_HIGH_COURT_NAME, "highcourt@email.com", RCJ_HIGH_COURT_CODE,
                    RCJ_HIGH_COURT_REGION, null))
                .sendToCtsc("No")
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-1352, case reference = 12345}, "
                    + "Skipping migration as case is in the High Court");
        }

        @Test
        void shouldThrowExceptionWhenAlreadySendingToCtsc() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court("Name", "court@email.com", "001", "Region", null))
                .sendToCtsc("Yes")
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-1352, case reference = 12345}, "
                    + "Skipping migration as case is already sending to the CTSC");
        }

        @Test
        void shouldMigrateCaseIfInNormalCourtAndNotSendingToCtsc() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court("Name", "court@email.com", "001", "Region", null))
                .sendToCtsc("No")
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getSendToCtsc()).isEqualTo("Yes");
        }

    }
}
