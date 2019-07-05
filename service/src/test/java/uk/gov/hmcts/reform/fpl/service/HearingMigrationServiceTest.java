package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class HearingMigrationServiceTest {

    @InjectMocks
    private HearingMigrationService classUnderTest;

    @Test
    void shouldSetMigratedHearingToYesWhenNoHearingDataPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedHearingToYesWhenHearing1Exists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("Hearing1", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedHearingToNoWhenMigratedHearingExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("hearing", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = classUnderTest.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("hearingMigrated", "No");
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
