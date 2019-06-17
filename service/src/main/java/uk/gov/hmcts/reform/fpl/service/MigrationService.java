package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Email;
import uk.gov.hmcts.reform.fpl.model.NewRespondent;
import uk.gov.hmcts.reform.fpl.model.Party;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.TelephoneNumber;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
@SuppressWarnings("unchecked")
public class MigrationService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String firstName;
    private String lastName;

    public CaseDetails migrateCase(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        // ADD NEW STRUCTURE TO CASE DATA

        data.put("respondents", migrateRespondents(objectMapper.convertValue(data.get("respondents"), Map.class)));

        return CaseDetails.builder()
            .data(data)
            .build();
    }

    // Will be old case -> new case. For now oldRespondent -> newRespondent

    // Give method a Respondent object and it will return the new data structure
    private List<Map<String, Object>> migrateRespondents(Map<String, Object> respondents) {
        /// FIRST RESPONDENT

        Respondent firstRespondent = objectMapper.convertValue(respondents.get("firstRespondent"), Respondent.class);
        NewRespondent migratedFirstRespondent = migrateIndividualRespondent(firstRespondent);

        /// ADDITIONAL RESPONDENT

        List<Map<String, Object>> additionalRespondents =
            (List<Map<String, Object>>) objectMapper.convertValue(respondents.get("additional"), List.class);

        List<NewRespondent> migratedRespondentCollection = additionalRespondents.stream()
            .map(respondent -> migrateIndividualRespondent(objectMapper.convertValue(respondent.get("value"), Respondent.class)))
            .collect(toList());

        // ADD FIRST RESPONDENT TO ADDITIONAL RESPONDENT LIST

        migratedRespondentCollection.add(migratedFirstRespondent);

        List<Map<String, Object>> newStructure = new ArrayList<>();

        /// BUILD NEW STRUCTURE


        migratedRespondentCollection.forEach(item ->
            newStructure.add(ImmutableMap.of(
                "id", UUID.randomUUID().toString(),
                "value", item)));

        return newStructure;
    }

    private NewRespondent migrateIndividualRespondent(Respondent or) {
        if (or.getName() != null) {
            firstName = or.getName().split("\\s+")[0];
            lastName = or.getName().split("\\s+")[1];
        }

        return NewRespondent.builder()
            .party(Party.builder()
                .partyID(UUID.randomUUID().toString())
                .idamID("")
                .partyType("Individual")
                .title("")
                .firstName(firstName)
                .lastName(lastName)
                .organisationName("")
                .dateOfBirth(or.getDob())
                .address(or.getAddress())
                .email(Email.builder()
                    .email("")
                    .emailUsageType("")
                    .build())
                .telephoneNumber(TelephoneNumber.builder()
                    .telephoneNumber(or.getTelephone())
                    .telephoneUsageType("")
                    .contactDirection("")
                    .build())
                .build())
            .leadRespondentIndicator("")
            .build();
    }
}
