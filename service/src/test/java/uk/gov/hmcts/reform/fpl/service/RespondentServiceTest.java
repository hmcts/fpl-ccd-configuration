package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class RespondentServiceTest {

    private final RespondentService service = new RespondentService();

    @SuppressWarnings("unchecked")
    @Test
    void shouldExpandRespondentCollectionWhenNoRespondents() {
        Map<String, Object> respondentObject = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(respondentObject)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.expandRespondentCollection(caseDetails);
        List<Map<String, Object>> respondents = (List<Map<String, Object>>) response.getData().get("respondents1");
        Map<String, Object> value = (Map<String, Object>) respondents.get(0).get("value");
        Map<String, Object> party = (Map<String, Object>) value.get("party");

        assertThat(response.getData()).containsOnlyKeys("respondents1");

        assertThat(respondents).hasSize(1);
        assertThat(party.get("partyId")).isNotNull();
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

        assertThat(party.get("partyId")).isNotNull();
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

        assertThat(firstParty.get("partyId")).isNotNull();

        assertThat(secondParty)
            .containsEntry("firstName", "Lucy")
            .containsEntry("partyType", "INDIVIDUAL");

        assertThat(secondParty.get("partyId")).isNotNull();
    }

    @Test
    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfRespondents1IsNotPresent() {
        Map<String, Object> data = new HashMap<>();
        data.put("respondent", "data");

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);

        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldKeepExistingPartyIDWhenAlreadyExists() {
        Map<String, Object> respondentObject = new HashMap<>();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .partyId("123")
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

        assertThat(party.get("partyId")).isEqualTo("123");
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
                        .partyId("123")
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
        assertThat(firstParty.get("partyId")).isEqualTo("123");

        assertThat(secondParty).containsEntry("firstName", "Lucy");
        assertThat(secondParty.get("partyId")).isNotNull();
    }

    @Nested
    class BuildRespondentLabel {

        @Test
        void shouldBuildExpectedLabelWhenSingleElementInList() {
            List<Element<Respondent>> respondents = ImmutableList.of(Element.<Respondent>builder()
                .value(Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName("James")
                        .lastName("Daniels")
                        .build())
                    .build())
                .build());

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenManyElementsInList() {
            List<Element<Respondent>> respondents = getRespondents();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("Respondent 1 - James Daniels\nRespondent 2 - Bob Martyn\n");
        }

        @Test
        void shouldBuildExpectedLabelWhenEmptyList() {
            List<Element<Respondent>> respondents = emptyList();

            String result = service.buildRespondentLabel(respondents);

            assertThat(result).isEqualTo("No respondents on the case");
        }

        private List<Element<Respondent>> getRespondents() {
            return ImmutableList.of(Element.<Respondent>builder()
                    .value(Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("James")
                            .lastName("Daniels")
                            .build())
                        .build())
                    .build(),
                Element.<Respondent>builder()
                    .value(Respondent.builder()
                        .party(RespondentParty.builder()
                            .firstName("Bob")
                            .lastName("Martyn")
                            .build())
                        .build())
                    .build());
        }
    }
}
