package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;

class CaseDetailsMapTest {

    @Test
    void shouldAddNonEmptyValue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of())
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails)
            .putIfNotEmpty("K1", "V1");

        assertThat(caseDetailsMap).containsEntry("K1", "V1");
    }

    @Test
    void shouldNotAddEmptyValue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("K0", "V0"))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails)
            .putIfNotEmpty("K1", null)
            .putIfNotEmpty("K2", emptyList());

        assertThat(caseDetailsMap)
            .containsEntry("K0", "V0")
            .doesNotContainKeys("K1", "K2");
    }

    @Test
    void shouldRemoveExistingValueIfNewValueIsEmpty() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("K0", "V0", "K1", "V1"))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails)
            .putIfNotEmpty("K0", null)
            .putIfNotEmpty("K1", emptyList());

        assertThat(caseDetailsMap).isEmpty();
    }
}
