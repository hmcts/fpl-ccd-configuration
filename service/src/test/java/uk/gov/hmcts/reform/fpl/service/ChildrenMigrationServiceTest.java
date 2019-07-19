package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ChildrenMigrationServiceTest {

    private final ChildrenMigrationService service = new ChildrenMigrationService();

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

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleChild() {
        Map<String, Object> childObject = new HashMap<>();

        childObject.put("children1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .firstName("James")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(childObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children1");
        Map<String, Object> value = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party)
            .containsEntry("firstName", "James")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(party.get("partyID")).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleChildren() {
        Map<String, Object> childObject = new HashMap<>();

        childObject.put("children1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .firstName("James")
                        .build()
                )),
            ImmutableMap.of(
                "id", "98765",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .firstName("Lucy")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(childObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children1");
        Map<String, Object> firstValue = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> secondValue = (Map<String, Object>) children.get(1).get("value");
        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");

        assertThat(firstParty)
            .containsEntry("firstName", "James")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(firstParty.get("partyID")).isNotNull();

        assertThat(secondParty)
            .containsEntry("firstName", "Lucy")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(secondParty.get("partyID")).isNotNull();
    }

    @Test
    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfChildren1IsNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("children", "data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyID() {
        Map<String, Object> childObject = new HashMap<>();

        childObject.put("children1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .partyID("123")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(childObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children1");
        Map<String, Object> value = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party.get("partyID")).isEqualTo("123");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        Map<String, Object> childObject = new HashMap<>();

        childObject.put("children1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .firstName("James")
                        .partyID("123")
                        .build()
                )),
            ImmutableMap.of(
                "id", "98765",
                "value", ImmutableMap.of(
                    "party", ChildParty.builder()
                        .firstName("Lucy")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(childObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> children = (List<Map<String, Object>>) data.get("children1");
        Map<String, Object> firstValue = (Map<String, Object>) children.get(0).get("value");
        Map<String, Object> secondValue = (Map<String, Object>) children.get(1).get("value");
        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");

        assertThat(firstParty).containsEntry("firstName", "James");
        assertThat(firstParty.get("partyID")).isNotNull();

        assertThat(secondParty).containsEntry("firstName", "Lucy");
        assertThat(secondParty.get("partyID")).isNotNull();
    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        return data;
    }
}
