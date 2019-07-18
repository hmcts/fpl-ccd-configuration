package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class RespondentService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("respondents1") || !caseDetails.getData().containsKey("respondents")) {
            data.put("respondentsMigrated", "Yes");

            if (!caseDetails.getData().containsKey("respondents1")) {
                // Populates first respondent so UI contains expanded Respondent Object.
                List<Map<String, Object>> populatedRespondent = new ArrayList<>();
                populatedRespondent.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyId", UUID.randomUUID().toString()
                        )
                    ))
                );
                data.put("respondents1", populatedRespondent);
            }
        } else {
            data.put("respondentsMigrated", "No");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse addHiddenValues(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("respondents1")) {
            List<Map<String, Object>> respondentParties = (List<Map<String, Object>>) data.get("respondents1");

            List<RespondentParty> respondentPartyList = respondentParties.stream()
                .map(entry -> mapper.convertValue(entry.get("value"), Map.class))
                .map(map -> mapper.convertValue(map.get("party"), RespondentParty.class))
                .map(respondent -> {
                    RespondentParty.RespondentPartyBuilder partyBuilder = respondent.toBuilder();

                    if (respondent.getPartyID() == null) {
                        partyBuilder.partyID(UUID.randomUUID().toString());
                        partyBuilder.partyType(PartyType.INDIVIDUAL);
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Map<String, Object>> respondents = respondentPartyList.stream()
                .map(item -> ImmutableMap.<String, Object>builder()
                    .put("id", UUID.randomUUID().toString())
                    .put("value", ImmutableMap.of(
                        "party", mapper.convertValue(item, Map.class),
                        "leadRespondentIndicator", "No"))
                    .build())
                .collect(toList());

            data.put("respondents1", respondents);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
