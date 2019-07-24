package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Respondents;
import uk.gov.hmcts.reform.fpl.model.common.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.migration.MigratedRespondent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {RespondentService.class, ObjectMapper.class})
@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
class RespondentServiceTest {

    @Autowired
    private RespondentService service;

    @Test
    void shouldAddMigratedRespondentYesWhenNoRespondentData() {
        CaseData caseData = CaseData.builder().respondents(null).build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedRespondentYesWhenRespondents1Exists() {
        CaseData caseData = CaseData.builder()
            .respondents1(
                ImmutableList.of(Element.<MigratedRespondent>builder()
                    .value(
                        MigratedRespondent.builder()
                            .build())
                    .build()))
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("Yes");
    }

    @Test
    void shouldAddMigratedRespondentNoWhenOldRespondentsExists() {
        CaseData caseData = CaseData.builder()
            .respondents(Respondents.builder().build())
            .build();

        String migratedValue = service.setMigratedValue(caseData);

        assertThat(migratedValue).isEqualTo("No");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddPartyIDAndPartyTypeValuesToSingleRespondent() {
        Map<String, Object> respondentObject = new HashMap<>();

        UUID uuid = UUID.randomUUID();

        respondentObject.put("respondents1", ImmutableList.of(
            ImmutableMap.of(
                "id", "12345",
                "value", ImmutableMap.of(
                    "party", RespondentParty.builder()
                        .firstName("James")
                        .build()
                ))));

        List<Element<MigratedRespondent>> data = ImmutableList.of(
            Element.<MigratedRespondent>builder()
                .id(uuid)
                .value(
                    MigratedRespondent.builder()
                        .party(
                            RespondentParty.builder()
                                .firstName("James")
                                .build())
                        .build())
                .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        CaseData caseData = service.addHiddenValues(caseDetails);

        assertThat(caseData.getRespondents1().get(0).getValue().getParty().firstName).isEqualTo("James");
        assertThat(caseData.getRespondents1().get(0).getValue().getParty().partyType).isEqualTo("INDIVIDUAL");
        assertThat(caseData.getRespondents1().get(0).getValue().getParty().partyId).isNotNull();
    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    void shouldAddPartyIDAndPartyTypeValuesToMultipleRespondents() {
//        Map<String, Object> respondentObject = new HashMap<>();
//
//        respondentObject.put("respondents1", ImmutableList.of(
//            ImmutableMap.of(
//                "id", "12345",
//                "value", ImmutableMap.of(
//                    "party", RespondentParty.builder()
//                        .firstName("James")
//                        .build()
//                )),
//            ImmutableMap.of(
//                "id", "98765",
//                "value", ImmutableMap.of(
//                    "party", RespondentParty.builder()
//                        .firstName("Lucy")
//                        .build()
//                ))));
//
//        CaseDetails caseDetails = CaseDetails.builder()
//            .data(respondentObject)
//            .build();
//
//        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);
//
//        Map<String, Object> data = response.getData();
//        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
//        Map<String, Object> firstValue = (Map<String, Object>) respondents.get(0).get("value");
//        Map<String, Object> secondValue = (Map<String, Object>) respondents.get(1).get("value");
//        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
//        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");
//
//        assertThat(firstParty)
//            .containsEntry("firstName", "James")
//            .containsEntry("partyType", "INDIVIDUAL");
//
//        assertThat(firstParty.get("partyId")).isNotNull();
//
//        assertThat(secondParty)
//            .containsEntry("firstName", "Lucy")
//            .containsEntry("partyType", "INDIVIDUAL");
//
//        assertThat(secondParty.get("partyId")).isNotNull();
//    }
//
//    @Test
//    void shouldNotAddPartyIDAndPartyTypeValuesToDataStructureIfRespondents1IsNotPresent() {
//        CaseDetails caseDetails = CaseDetails.builder()
//            .data(createData("respondent", "data"))
//            .build();
//
//        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);
//
//        assertThat(response.getData()).isEqualTo(caseDetails.getData());
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    void shouldKeepExistingPartyIDWhenAlreadyExists() {
//        Map<String, Object> respondentObject = new HashMap<>();
//
//        respondentObject.put("respondents1", ImmutableList.of(
//            ImmutableMap.of(
//                "id", "12345",
//                "value", ImmutableMap.of(
//                    "party", RespondentParty.builder()
//                        .partyId("123")
//                        .build()
//                ))));
//
//        CaseDetails caseDetails = CaseDetails.builder()
//            .data(respondentObject)
//            .build();
//
//        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);
//
//        Map<String, Object> data = response.getData();
//        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
//        Map<String, Object> value = (Map<String, Object>) respondents.get(0).get("value");
//        Map<String, Object> party = (Map<String, Object>) value.get("party");
//
//        assertThat(party.get("partyId")).isEqualTo("123");
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    void shouldKeepExistingPartyIdAndContinueAddingNewPartyId() {
//        Map<String, Object> respondentObject = new HashMap<>();
//
//        respondentObject.put("respondents1", ImmutableList.of(
//            ImmutableMap.of(
//                "id", "12345",
//                "value", ImmutableMap.of(
//                    "party", RespondentParty.builder()
//                        .firstName("James")
//                        .partyId("123")
//                        .build()
//                )),
//            ImmutableMap.of(
//                "id", "98765",
//                "value", ImmutableMap.of(
//                    "party", RespondentParty.builder()
//                        .firstName("Lucy")
//                        .build()
//                ))));
//
//        CaseDetails caseDetails = CaseDetails.builder()
//            .data(respondentObject)
//            .build();
//
//        AboutToStartOrSubmitCallbackResponse response = service.addHiddenValues(caseDetails);
//
//        Map<String, Object> data = response.getData();
//        List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents1");
//        Map<String, Object> firstValue = (Map<String, Object>) respondents.get(0).get("value");
//        Map<String, Object> secondValue = (Map<String, Object>) respondents.get(1).get("value");
//        Map<String, Object> firstParty = (Map<String, Object>) firstValue.get("party");
//        Map<String, Object> secondParty = (Map<String, Object>) secondValue.get("party");
//
//        assertThat(firstParty).containsEntry("firstName", "James");
//        assertThat(firstParty.get("partyId")).isEqualTo("123");
//
//        assertThat(secondParty).containsEntry("firstName", "Lucy");
//        assertThat(secondParty.get("partyId")).isNotNull();
//    }

    private Map<String, Object> createData(String key, String value) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);

        return data;
    }
}
