package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

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

    @Test
    @SuppressWarnings("unchecked")
    void shouldFilterPlacementEmailsWhenMigrationIdMatches() throws Exception {
        String migrationId = "DFPL-3296";
        String targetEmail = "****@***";


        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email(targetEmail)
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

        // Wrap within CaseDetails
        CaseData caseData = CaseData.builder().id(1767800818952560L).build();
        CaseDetails caseDetails = asCaseDetails(caseData);

        caseDetails.getData().put("migrationId", migrationId);
        caseDetails.getData().put("placements", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidential", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidentialNotices", new ArrayList<>(List.of(placementElement)));

        //  Construct raw callback request
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("migrate-case")
            .build();

        // MockMvc to simulate rest call
        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/callback/migrate-case/about-to-submit")
                .header("authorization", "Bearer token")
                .header("user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(callbackRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // setting up Jackson and raw JSON content dynamically
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.registerModule(new JavaTimeModule());

        Map<String, Object> responseMap = testMapper.readValue(responseContent, new TypeReference<>() {});
        Map<String, Object> dataField = (Map<String, Object>) responseMap.get("data");

        // Assertions
        assertThat(dataField).containsKeys(
            "placements", "placementsNonConfidential", "placementsNonConfidentialNotices");

        List<Map<String, Object>> noticesList = (List<Map<String, Object>>) dataField
            .get("placementsNonConfidentialNotices");
        assertThat(noticesList).hasSize(1);

        Map<String, Object> innerValue = (Map<String, Object>) noticesList.getFirst().get("value");
        List<?> respondentsToNotify = (List<?>) innerValue.get("placementRespondentsToNotify");

        assertThat(respondentsToNotify).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldLogSkipMessageWhenMigrationIdMatchesButTargetEmailIsNotFound() throws Exception {
        String migrationId = "DFPL-3296";

        // Create a respondent with a completely different email address
        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("completely-different-email@test.com")
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

        // Wrap within CaseDetails using the exact expected Case ID
        CaseData caseData = CaseData.builder().id(1767800818952560L).build();
        CaseDetails caseDetails = asCaseDetails(caseData);

        caseDetails.getData().put("migrationId", migrationId);
        caseDetails.getData().put("placements", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidential", new ArrayList<>(List.of(placementElement)));
        caseDetails.getData().put("placementsNonConfidentialNotices", new ArrayList<>(List.of(placementElement)));

        //  callback request
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("migrate-case")
            .build();

        //  Hit MockMvc endpoint
        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/callback/migrate-case/about-to-submit")
                .header("authorization", "Bearer token")
                .header("user-id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(callbackRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        //  Check response
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.registerModule(new JavaTimeModule());

        Map<String, Object> responseMap = testMapper.readValue(responseContent, new TypeReference<>() {});
        Map<String, Object> dataField = (Map<String, Object>) responseMap.get("data");

        //  Assertions
        List<Map<String, Object>> noticesList = (List<Map<String, Object>>) dataField
            .get("placementsNonConfidentialNotices");
        Map<String, Object> innerValue = (Map<String, Object>) noticesList.getFirst().get("value");
        List<?> respondentsToNotify = (List<?>) innerValue.get("placementRespondentsToNotify");

        assertThat(respondentsToNotify).hasSize(1);
    }
}
