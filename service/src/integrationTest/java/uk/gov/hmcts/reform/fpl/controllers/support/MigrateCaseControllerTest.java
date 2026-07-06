package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.controllers.support.MigrateCaseController.MIGRATION_ID_KEY;


@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

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

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

    @Nested
    class Dfpl2894 {
        private static final String MIGRATION_ID = "DFPL-2894";

        @Test
        void shouldMigrateCaseManagementLocationFromFleetwoodToBlackpool() {

            Map<String, Object> locationStructure = Map.of("baseLocation", "401452", "region", "4");
            Map<String, Object> courtStructure = Map.of("code", "438",
                "name", "Family Court sitting at Fleetwood",
                "epimmsId", "401452");
            Map<String, Object> ordersStructure = Map.of("court", "438",
                "address", Map.of("PostCode", "FY7 6AA"));

            CaseDetails caseDetails = CaseDetails.builder()
                .id(1778521486149688L)
                .data(new java.util.HashMap<>(Map.of(
                    MIGRATION_ID_KEY, MIGRATION_ID,
                    "caseManagementLocation", locationStructure,
                    "court", courtStructure,
                    "orders", ordersStructure,
                    "blackburnLancasterDFJCourt", "438",
                    "caseSummaryCourtName", "Family Court sitting at Fleetwood"
                )))
                .build();


            CaseData mutatedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            // Assert
            assertThat(mutatedCaseData.getCaseManagementLocation()).isNotNull();
            assertThat(mutatedCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("214320");
            assertThat(mutatedCaseData.getCourt().getCode()).isEqualTo("131");
            assertThat(mutatedCaseData.getOrders().getCourt()).isEqualTo("131");
        }

        @Test
        void shouldThrowExceptionWhenControllerEncountersMissingCaseManagementLocation() {

            CaseDetails caseDetails = CaseDetails.builder()
                .id(1778521486149688L)
                .data(new java.util.HashMap<>(Map.of(MIGRATION_ID_KEY, MIGRATION_ID)))
                .build();

            //  Assert
            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .hasRootCauseInstanceOf(AssertionError.class)
                .hasMessageContaining("caseManagementLocation structure is missing");
        }

        @Test
        void shouldThrowExceptionWhenControllerEncountersNonFleetwoodBaseLocation() {

            Map<String, Object> invalidLocationStructure = Map.of("baseLocation", "111111", "region", "1");

            CaseDetails caseDetails = CaseDetails.builder()
                .id(1778521486149688L)
                .data(new java.util.HashMap<>(Map.of(
                    MIGRATION_ID_KEY, MIGRATION_ID,
                    "caseManagementLocation", invalidLocationStructure
                )))
                .build();

            // Assert
            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .hasRootCauseInstanceOf(AssertionError.class)
                .hasMessageContaining("expected Fleetwood (401452) but found baseLocation: 111111");
        }
    }

}
