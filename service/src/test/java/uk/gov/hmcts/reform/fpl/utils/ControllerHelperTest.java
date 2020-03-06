package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ControllerHelper.removeTemporaryFields;

class ControllerHelperTest {

    private static Map<String, Object> data = new HashMap<>();
    private static CaseDetails caseDetails = CaseDetails.builder().data(data).build();

    @BeforeEach
    void populateMap() {
        data.put("key1", "some value 1");
        data.put("key2", "some value 2");
        data.put("key3", 3);
    }

    @AfterEach
    void clearMap() {
        data.clear();
    }

    @Test
    void shouldRemoveFieldsFromCaseDataMapWhenPresent() {
        removeTemporaryFields(caseDetails, "key1", "key2", "key3");

        assertThat(caseDetails.getData()).isEmpty();
    }

    @Test
    void shouldNotRemoveFieldsThatArePresentInMapWhenNotPassed() {
        removeTemporaryFields(caseDetails, "key1", "key3");

        assertThat(caseDetails.getData()).containsOnly(Map.entry("key2", "some value 2"));
    }
}
