package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@SuppressWarnings("VariableDeclarationUsageDistance")
public abstract class AbstractEmailContentProvider {

    private final String uiBaseUrl;

    protected AbstractEmailContentProvider(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    protected ImmutableMap.Builder<String, String> getCasePersonalisationBuilder(CaseDetails caseDetails) {
        String dataPresent = "Yes";
        String fullStop = "No";
        String timeFramePresent = "No";

        String directions = (String) Optional.ofNullable(caseDetails.getData().get("orders_directions"))
            .orElse("");
        List orderOptions = (List) Optional.ofNullable(caseDetails.getData().get("orders_option"))
            .orElse(Collections.emptyList());

        ImmutableMap.Builder<String, String> orderOptionsArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderOptions.size()) {
                orderOptionsArray.put("orders" + i, "^" + orderOptions.get(i));
            } else {
                orderOptionsArray.put("orders" + i, "");
            }
        }

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        }

        if (orderOptions.isEmpty()) {
            dataPresent = "No";
            fullStop = "Yes";
        }

        return ImmutableMap.<String, String>builder()
            .putAll(orderOptionsArray.build())
            .put("dataPresent", dataPresent)
            .put("fullStop", fullStop)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put("directionsAndInterim", !directions.isEmpty() ? "^" + directions : "")
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }
}
