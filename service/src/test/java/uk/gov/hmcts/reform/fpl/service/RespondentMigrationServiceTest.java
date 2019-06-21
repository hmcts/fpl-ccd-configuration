package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class RespondentMigrationServiceTest {

    private final RespondentMigrationService service = new RespondentMigrationService();

    @Test
    void shouldAddMigratedRespondentYesWhenNoRespondentData() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("respondentsMigrated", "Yes");
    }

    @Test
    void shouldAddMigratedRespondentYesWhenRespondents1Exists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("respondents1", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("respondentsMigrated", "Yes");
    }

    @Test
    void shouldAddMigratedRespondentNoWhenOldRespondentsExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("respondents", "some value"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);

        assertThat(response.getData()).containsEntry("respondentsMigrated", "No");
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);

        return data;
    }
}

