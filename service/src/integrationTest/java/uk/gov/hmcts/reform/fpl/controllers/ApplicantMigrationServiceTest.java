package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ApplicantMigrationServiceTest {
    private final ApplicantMigrationService service = new ApplicantMigrationService();

    @Test
    void shouldAddMigratedApplicantYesWhenNoApplicantData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("applicantsMigrated", "Yes");
    }

    @Test
    void shouldAddMigratedApplicantYesWhenApplicantExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("applicants", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("applicantsMigrated", "Yes");
    }

    @Test
    void shouldAddMigratedApplicantNoWhenOldApplicantExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("applicant", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("applicantsMigrated", "No");
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);

        return data;
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToApplicant() {
        Map<String, Object> applicantObject = new HashMap<>();

        applicantObject.put("applicants", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ApplicantParty.builder()
                        .organisationName("Beckys Organisation")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(applicantObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> applicant = (List<Map<String, Object>>) data.get("applicants");
        Map<String, Object> value = (Map<String, Object>) applicant.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party)
            .containsEntry("organisationName", "Beckys Organisation")
            .containsEntry("partyType", "ORGANISATION");

        assertThat(party.get("partyID")).isNotNull();
    }

    @Test
    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfNewApplicantIsNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("applicant", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }
}
