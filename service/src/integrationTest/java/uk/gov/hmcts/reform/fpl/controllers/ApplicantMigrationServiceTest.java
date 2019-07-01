package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.service.ApplicantMigrationService;

import java.util.HashMap;
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
}
