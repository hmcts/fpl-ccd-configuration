package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OthersMigrationService {
    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey("others1") || !caseDetails.getData().containsKey("others")) {
            data.put("othersMigrated", "Yes");

            if (!caseDetails.getData().containsKey("others1")) {
                List<Map<String, Object>> populatedRespondent = new ArrayList<>();
                populatedRespondent.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyId", UUID.randomUUID().toString()
                        )
                    ))
                );
                data.put("others1", populatedRespondent);
            }
        } else {
            data.put("othersMigrated", "No");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
