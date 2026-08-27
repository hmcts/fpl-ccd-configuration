package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.NoSuchElementException;
import java.util.Optional;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    @SuppressWarnings("unchecked")
    void shouldFilterPlacementRespondentWhenMigrationIdAndTargetIdMatches() throws Exception {
        String migrationId = "DFPL-3296";
        long targetCaseId = 1767800818952560L;
        String targetElementId = "0592fa9e-547c-4db0-8c08-6905489fcf8e";

        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("test@test.com")
            .build();

        Respondent respondent = Respondent.builder()
            .solicitor(solicitor)
            .build();


        Map<String, Object> respondentElement = Map.of(
            "id", targetElementId,
            "value", respondent
        );

        Map<String, Object> placementValue = new HashMap<>();
        placementValue.put("placementChildName", "test child");
        placementValue.put("placementRespondentsToNotify", List.of(respondentElement));

        Map<String, Object> placementElement = Map.of(
            "id", UUID.randomUUID().toString(),
            "value", placementValue
        );


        CaseData caseData = CaseData.builder().id(targetCaseId).build();
        CaseDetails caseDetails = asCaseDetails(caseData);

        caseDetails.getData().put("migrationId", migrationId);
        caseDetails.getData().put("placements", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidential", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidentialNotices", new ArrayList<>(List.of(placementElement)));


        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("migrate-case")
            .build();

        // MockMvc execution to simulate endpoint invocation
        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/callback/migrate-case/about-to-submit")
                .header("authorization", "Bearer token")
                .header("user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(callbackRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Map and extract response payload
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.registerModule(new JavaTimeModule());

        Map<String, Object> responseMap = testMapper.readValue(responseContent, new TypeReference<>() {});
        Map<String, Object> dataField = (Map<String, Object>) responseMap.get("data");

        // Assertions
        assertThat(dataField).containsKeys("placements", "placementsNonConfidential",
            "placementsNonConfidentialNotices");

        List<Map<String, Object>> noticesList = (List<Map<String, Object>>) dataField
            .get("placementsNonConfidentialNotices");
        assertThat(noticesList).hasSize(1);

        Map<String, Object> innerValue = (Map<String, Object>) noticesList.getFirst().get("value");
        List<?> respondentsToNotify = (List<?>) innerValue.get("placementRespondentsToNotify");

        // Verification: The element matching the target UUID was successfully dropped!
        assertThat(respondentsToNotify).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLogSkipMessageWhenCaseMatchesButTargetIdIsNotFound() throws Exception {
        String migrationId = "DFPL-3296";
        long targetCaseId = 1767800818952560L;

        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("test@test.com")
            .build();

        Respondent respondent = Respondent.builder()
            .solicitor(solicitor)
            .build();

        Map<String, Object> respondentElement = Map.of(
            "id", UUID.randomUUID().toString(),
            "value", respondent
        );

        Map<String, Object> placementValue = new HashMap<>();
        placementValue.put("placementChildName", "test child");
        placementValue.put("placementRespondentsToNotify", List.of(respondentElement));

        Map<String, Object> placementElement = Map.of(
            "id", UUID.randomUUID().toString(),
            "value", placementValue
        );


        CaseData caseData = CaseData.builder().id(targetCaseId).build();
        CaseDetails caseDetails = asCaseDetails(caseData);

        caseDetails.getData().put("migrationId", migrationId);
        caseDetails.getData().put("placements", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidential", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidentialNotices", new ArrayList<>(List.of(placementElement)));

        // Callback request
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("migrate-case")
            .build();

        // Hit MockMvc endpoint
        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/callback/migrate-case/about-to-submit")
                .header("authorization", "Bearer token")
                .header("user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(callbackRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Check response
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.registerModule(new JavaTimeModule());

        Map<String, Object> responseMap = testMapper.readValue(responseContent, new TypeReference<>() {});
        Map<String, Object> dataField = (Map<String, Object>) responseMap.get("data");

        // Assertions
        List<Map<String, Object>> noticesList = (List<Map<String, Object>>) dataField
            .get("placementsNonConfidentialNotices");
        Map<String, Object> innerValue = (Map<String, Object>) noticesList.getFirst().get("value");
        List<?> respondentsToNotify = (List<?>) innerValue.get("placementRespondentsToNotify");

        assertThat(respondentsToNotify).hasSize(1);
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


}
