package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.ChildParty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class ChildrenMigrationService {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("children1") || !caseDetails.getData().containsKey("children")) {
            data.put("childrenMigrated", "Yes");

            if (!caseDetails.getData().containsKey("children1")) {
                List<Map<String, Object>> populatedChild = new ArrayList<>();
                populatedChild.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyID", UUID.randomUUID().toString()
                        )
                    )
                ));

                data.put("children1", populatedChild);
            }
        } else {
            data.put("childrenMigrated", "No");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @SuppressWarnings("unchecked")
    public AboutToStartOrSubmitCallbackResponse addHiddenValues(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("children1")) {
            List<Map<String, Object>> childrenParty = (List<Map<String, Object>>) data.get("children1");

            List<ChildParty> childrenPartyList = childrenParty.stream()
                .map(entry -> mapper.convertValue(entry.get("value"), Map.class))
                .map(map -> mapper.convertValue(map.get("party"), ChildParty.class))
                .map(child -> {
                    ChildParty.ChildPartyBuilder partyBuilder = child.toBuilder();

                    if (child.getPartyID() == null) {
                        partyBuilder.partyID(UUID.randomUUID().toString());
                        partyBuilder.partyType("INDIVIDUAL");
                    }

                    return partyBuilder.build();
                })
                .collect(toList());

            List<Map<String, Object>> children = childrenPartyList.stream()
                .map(item -> ImmutableMap.<String, Object>builder()
                    .put("id", UUID.randomUUID().toString())
                    .put("value", ImmutableMap.of(
                        "party", mapper.convertValue(item, Map.class)))
                    .build())
                .collect(toList());

            data.put("children1", children);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
