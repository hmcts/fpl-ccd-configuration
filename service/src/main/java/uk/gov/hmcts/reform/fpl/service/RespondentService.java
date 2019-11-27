package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.PartyType;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class RespondentService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    public AboutToStartOrSubmitCallbackResponse expandRespondentCollection(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (!caseDetails.getData().containsKey("respondents1")) {
            // Populates first respondent so UI contains expanded Respondent Object.
            List<Map<String, Object>> populatedRespondent = new ArrayList<>();
            populatedRespondent.add(ImmutableMap.of(
                "id", UUID.randomUUID().toString(),
                "value", ImmutableMap.of(
                    "party", ImmutableMap.of(
                        // Variable within CCD party structure must be set to expand Collection.
                        // PartyId is a hidden field so setting a value will not persist to the db
                        "partyId", UUID.randomUUID().toString()
                    )
                ))
            );
            data.put("respondents1", populatedRespondent);
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

                    if (respondent.getPartyId() == null) {
                        partyBuilder.partyId(UUID.randomUUID().toString());
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

    public String buildRespondentLabel(List<Element<Respondent>> respondents) {
        StringBuilder sb = new StringBuilder();

        if (isNotEmpty(respondents)) {
            AtomicInteger i = new AtomicInteger(1);

            respondents.forEach(respondent -> {
                sb.append("Respondent")
                    .append(" ")
                    .append(i)
                    .append(" ")
                    .append("-")
                    .append(" ")
                    .append(defaultIfNull(respondent.getValue().getParty().firstName, ""))
                    .append(" ")
                    .append(defaultIfNull(respondent.getValue().getParty().lastName, ""))
                    .append("\n");

                i.incrementAndGet();
            });

        } else {
            sb.append("No respondents on the case");
        }

        return sb.toString();
    }
}
