package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@SuppressWarnings("VariableDeclarationUsageDistance")
public abstract class AbstractEmailContentProvider {

    private static final String ORDER_KEY = "orders";
    private static final String DIRECTIONS_KEY = "directionsAndInterim";

    private final String uiBaseUrl;

    protected AbstractEmailContentProvider(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    protected ImmutableMap.Builder<String, String> getCasePersonalisationBuilder(CaseDetails caseDetails) {
        String dataPresent = "Yes";
        String fullStop = "No";
        String timeFramePresent = "No";

        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get(ORDER_KEY)).orElse(ImmutableMap.builder().build());

        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());
        String directions = (String) Optional.ofNullable(orders.get(DIRECTIONS_KEY)).orElse("");

        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ORDER_KEY + i, "^" + orderType.get(i));
            } else {
                orderTypeArray.put(ORDER_KEY + i, "");
            }
        }

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        }

        if (orderType.isEmpty()) {
            dataPresent = "No";
            fullStop = "Yes";
        }

        return ImmutableMap.<String, String>builder()
            .putAll(orderTypeArray.build())
            .put("dataPresent", dataPresent)
            .put("fullStop", fullStop)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put(DIRECTIONS_KEY, !directions.isEmpty() ? "^" + directions : "")
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }
}
