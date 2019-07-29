package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.OldApplicant;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicantMigrationService.class, ObjectMapper.class})
public class ApplicantMigrationServiceTest {

    @Autowired
    private ApplicantMigrationService service;

    @Test
    void shouldAddMigratedApplicantYesWhenNoApplicantData() {
        CaseData caseData = CaseData.builder().applicant(null).build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedApplicantYesWhenNewApplicantExists() {
        CaseData caseData = CaseData.builder()
            .applicants(
                ImmutableList.of(Element.<Applicant>builder()
                    .value(Applicant.builder()
                        .party(ApplicantParty.builder().build())
                        .build())
                    .build()))
            .build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedApplicantNoWhenOldApplicantExists() {
        CaseData caseData = CaseData.builder()
            .applicant(OldApplicant.builder().build())
            .build();

        String response = service.setMigratedValue(caseData);

        assertThat(response).isEqualTo("No");
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);

        return data;
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIdAndPartyTypeValuesToApplicant() {
        Map<String, Object> applicantObject = new HashMap<>();

        applicantObject.put("applicants", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ApplicantParty.builder()
                        .organisationName("Becky's Organisation")
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
            .containsEntry("organisationName", "Becky's Organisation")
            .containsEntry("partyType", "ORGANISATION");

        assertThat(party.get("partyId")).isNotNull();
    }

    @Test
    void shouldNotAddPartyIdAndPartyTypeValuesToDataStructureIfNewApplicantIsNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("applicant", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }
}
