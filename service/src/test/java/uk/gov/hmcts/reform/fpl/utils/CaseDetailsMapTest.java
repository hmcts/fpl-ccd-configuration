package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
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
    void shouldOverrideExistingValueWithNonEmptyValue() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("K0", "V0"))
            .build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails)
            .putIfNotEmpty("K0", "V1");

        assertThat(caseDetailsMap).containsEntry("K0", "V1");
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


    @Test
    void shouldUpdateBatchOfProperties() {

        final Map<String, Object> initialMap = new HashMap<>();
        initialMap.put("K0", "V0");
        initialMap.put("K1", "V1");

        final Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("K0", null);
        updateMap.put("K2", "");
        updateMap.put("K3", "V3");

        final Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("K1", "V1");
        expectedMap.put("K3", "V3");

        CaseDetails caseDetails = CaseDetails.builder().data(initialMap).build();

        CaseDetailsMap caseDetailsMap = caseDetailsMap(caseDetails)
            .putIfNotEmpty(updateMap);

        assertThat(caseDetailsMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldRemoveEntries() {
        final Map<String, Object> initialMap = new HashMap<>();
        initialMap.put("K0", "V0");
        initialMap.put("K1", "V1");
        initialMap.put("K2", "V2");

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder().data(initialMap).build());

        caseDetailsMap.removeAll("K1", "K2");

        assertThat(caseDetailsMap).containsExactly(entry("K0", "V0"));
    }

    @Test
    void shouldBuildFromMap() {
        final Map<String, Object> initialMap = new HashMap<>();
        initialMap.put("K0", null);
        initialMap.put("K1", "V1");
        initialMap.put("K2", null);

        CaseDetailsMap caseDetailsMap = caseDetailsMap(initialMap);

        assertThat(caseDetailsMap).containsExactly(entry("K1", "V1"));
    }

}
