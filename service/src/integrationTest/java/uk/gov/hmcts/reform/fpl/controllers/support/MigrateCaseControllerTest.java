package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.NoSuchElementException;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.rd.model.Organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.controllers.support.MigrateCaseController.MIGRATION_ID_KEY;

import static org.mockito.BDDMockito.given;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private OrganisationService organisationService;

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
    class Dfpl3213 {
        private static final String MIGRATION_ID = "DFPL-3213";

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
                .hasMessageContaining("expected base location 401452 but found: 111111");
        }

        @Test
        void shouldMigrateFleetwoodOrdersCourtToPrestonForDfpl3213v2() {

            Map<String, Object> locationStructure = Map.of("baseLocation", "102476", "region", "4");
            Map<String, Object> ordersStructure = Map.of(
                "court", "438",
                "address", Map.of("PostCode", "FY7 6AA")
            );

            CaseDetails caseDetails = CaseDetails.builder()
                .id(1778521486149688L)
                .data(new java.util.HashMap<>(Map.of(
                    MIGRATION_ID_KEY, "DFPL-3213-v2",
                    "caseManagementLocation", locationStructure,
                    "orders", ordersStructure
                )))
                .build();

            CaseData mutatedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            // Assert
            assertThat(mutatedCaseData.getCaseManagementLocation()).isNotNull();
            assertThat(mutatedCaseData.getCaseManagementLocation().getBaseLocation()).isEqualTo("102476");
            assertThat(mutatedCaseData.getOrders().getCourt()).isEqualTo("303");
        }

    }

    @Test
    void shouldSuccessfullyMigrateOutsourcingPolicyWhenMigrationIdIsDFPL3347() {
        given(organisationService.findOrganisation("ZL7FAG5"))
            .willReturn(Optional.of(Organisation.builder()
                .organisationIdentifier("ZL7FAG5")
                .name("Test Organisation")
                .build()));

        CaseData caseData = extractCaseData(postAboutToSubmitEvent(
            CaseDetails.builder()
                .id(1783696286134453L)
                .data(Map.of("migrationId", "DFPL-3347"))
                .build()
        ));

        assertThat(caseData.getOutsourcingPolicy()).isNotNull();
        assertThat(caseData.getOutsourcingPolicy().getOrganisation().getOrganisationID())
            .isEqualTo("ZL7FAG5");
    }

    @Test
    void shouldSuccessfullyMigrateOutsourcingPolicyWhenMigrationIdIsDFPL3346() {
        given(organisationService.findOrganisation("CPYYWBZ"))
            .willReturn(Optional.of(Organisation.builder()
                .organisationIdentifier("CPYYWBZ")
                .name("Test Organisation")
                .build()));

        CaseData caseData = extractCaseData(postAboutToSubmitEvent(
            CaseDetails.builder()
                .id(1781013695412110L)
                .data(Map.of("migrationId", "DFPL-3346"))
                .build()
        ));

        assertThat(caseData.getOutsourcingPolicy()).isNotNull();
        assertThat(caseData.getOutsourcingPolicy().getOrganisation().getOrganisationID())
            .isEqualTo("CPYYWBZ");
    }

    @Nested
    class Dfpl3345 {
        private static final String MIGRATION_ID = "DFPL-3345";
        private static final long CASE_ID = 1777371329249951L;
        private static final String TARGET_UUID = "13f8bfee-4ed0-40b2-87ac-0300552584d1";

        @Test
        @SuppressWarnings("unchecked")
        void shouldRemoveTargetElementFromDraftOrdersRemovedWhenMigrationIdMatches() {
            String keepUuid = UUID.randomUUID().toString();

            Map<String, Object> targetElement = Map.of(
                "id", TARGET_UUID,
                "value", Map.of("title", "Confidential Draft Order")
            );

            Map<String, Object> keepElement = Map.of(
                "id", keepUuid,
                "value", Map.of("title", "Valid Draft Order")
            );

            CaseDetails caseDetails = CaseDetails.builder()
                .id(CASE_ID)
                .data(new HashMap<>(Map.of(
                    MIGRATION_ID_KEY, MIGRATION_ID,
                    "draftOrdersRemoved", new ArrayList<>(List.of(targetElement, keepElement))
                )))
                .build();

            CaseData mutatedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            // Assert
            assertThat(mutatedCaseData.getDraftOrdersRemoved()).hasSize(1);
            assertThat(mutatedCaseData.getDraftOrdersRemoved().get(0).getId())
                .isEqualTo(UUID.fromString(keepUuid));
        }

        @Test
        void shouldThrowExceptionWhenCaseIdDoesNotMatch() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(9999999999999999L)
                .data(new HashMap<>(Map.of(MIGRATION_ID_KEY, MIGRATION_ID)))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .hasRootCauseInstanceOf(AssertionError.class)
                .hasMessageContaining("DFPL-3345");
        }
    }


}
