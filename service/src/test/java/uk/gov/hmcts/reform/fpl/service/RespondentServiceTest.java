package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class RespondentServiceTest {

    private final RespondentService service = new RespondentService();

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

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleRespondent() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("James")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
        Map<String, Object> value = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party)
            .containsEntry("firstName", "James")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(party.get("partyID")).isNotNull();
    }


    //TODO: improve test to feature all parts of PartyRespondent but as a Map<String, Object>
    @SuppressWarnings("unchecked")
    @Test
    void shouldMapToParameterNamesToConstructorArguments() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", ImmutableMap.of(
                        "firstName", "James",
                        "lastName", "James",
                        "dateOfBirth", "",
                        "telephoneNumber", ImmutableMap.of("telephoneNumber", "00000000000"),
                        "placeOfBirth", "James")
                ))));

        System.out.println("respondentObject = " + respondentObject);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
        Map<String, Object> value = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party)
            .containsEntry("firstName", "James")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(party.get("partyID")).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToMultipleRespondents() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("James")
                        .build()
                )),
            ImmutableMap.of(
                "id", "98765",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("Lucy")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
        Map<String, Object> firstValue = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> secondValue = (Map<String, Object>) respondents.get(1).get("value");
        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");

        assertThat(firstParty)
            .containsEntry("firstName", "James")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(firstParty.get("partyID")).isNotNull();

        assertThat(secondParty)
            .containsEntry("firstName", "Lucy")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(firstParty.get("partyID")).isNotNull();
    }

    @Test
    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfRespondents1IsNotPresent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(createData("respondent", "data"))
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyID() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .partyID("123")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
        Map<String, Object> value = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(party.get("partyID")).isEqualTo("123");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("James")
                        .partyID("123")
                        .build()
                )),
            ImmutableMap.of(
                "id", "98765",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("Lucy")
                        .build()
                ))));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        Map<String, Object> data = response.getData();
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
        Map<String, Object> firstValue = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> secondValue = (Map<String, Object>) respondents.get(1).get("value");
        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");

        assertThat(firstParty).containsEntry("firstName", "James");
        assertThat(firstParty.get("partyID")).isNotNull();

        assertThat(secondParty).containsEntry("firstName", "Lucy");
        assertThat(firstParty.get("partyID")).isNotNull();
    }


    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);

        return data;
    }
}
