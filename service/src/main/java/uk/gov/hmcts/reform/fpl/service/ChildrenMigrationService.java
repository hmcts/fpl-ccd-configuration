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
public class ChildrenMigrationService {
    private static final String MIGRATED_CHILDREN_KEY = "children1";

    public AboutToStartOrSubmitCallbackResponse setMigratedValue(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        if (caseDetails.getData().containsKey(MIGRATED_CHILDREN_KEY)
            || !caseDetails.getData().containsKey("children")) {
            data.put("childrenMigrated", "Yes");

            if (!caseDetails.getData().containsKey(MIGRATED_CHILDREN_KEY)) {
                List<Map<String, Object>> populatedChild = new ArrayList<>();
                populatedChild.add(ImmutableMap.of(
                    "id", UUID.randomUUID().toString(),
                    "value", ImmutableMap.of(
                        "party", ImmutableMap.of(
                            "partyID", UUID.randomUUID().toString()
                        )
                    )
                ));
                data.put(MIGRATED_CHILDREN_KEY, populatedChild);
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        } else {
            data.put("childrenMigrated", "No");

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(data)
                .build();
        }
    }
}
