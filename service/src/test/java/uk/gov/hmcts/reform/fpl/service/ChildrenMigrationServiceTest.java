package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildrenMigrationServiceTest {
    private final ChildrenMigrationService service = new ChildrenMigrationService();
    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Test
    void shouldSetMigratedChildrenToYesWhenNoChildrenDataPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("childrenMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedChildrenToYesWhenChildren1Exists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("children1", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("childrenMigrated", "Yes");
    }

    @Test
    void shouldSetMigratedChildrenToNoWhenOldChildrenExists() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("children", "some value"))
            .build();
        AboutToStartOrSubmitCallbackResponse response = service.setMigratedValue(caseDetails);
        assertThat(response.getData()).containsEntry("childrenMigrated", "No");
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
