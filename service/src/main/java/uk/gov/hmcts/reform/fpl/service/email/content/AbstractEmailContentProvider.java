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
    private static final String DIRECTIONS_KEY = "directions";

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
        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        ImmutableMap.Builder<String, String> directionsArray = ImmutableMap.builder();

        boolean isDataPresent = buildOrders(orders, orderTypeArray);
        buildDirections(orders, directionsArray);

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        }

        if (isDataPresent) {
            dataPresent = "No";
            fullStop = "Yes";
        }

        return ImmutableMap.<String, String>builder()
            .putAll(orderTypeArray.build())
            .put("dataPresent", dataPresent)
            .put("fullStop", fullStop)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .putAll(directionsArray.build())
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    private boolean buildOrders(Map orders, ImmutableMap.Builder<String, String> orderTypeArray) {
        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());
        List emergencyProtectionOrders = (List) Optional.ofNullable(orders.get("emergencyProtectionOrders"))
            .orElse(ImmutableList.builder().build());
        int j = 0;
        for (int i = 0; i < 11; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ORDER_KEY + i, "^" + orderType.get(i));
            } else if (j < emergencyProtectionOrders.size()) {
                orderTypeArray.put(ORDER_KEY + i, "^" + emergencyProtectionOrders.get(j));
                j++;
            } else {
                orderTypeArray.put(ORDER_KEY + i, "");
            }
        }

        orderTypeArray.put(ORDER_KEY + "11", (String) Optional.ofNullable(orders.get("otherOrder")).orElse(""));
        orderTypeArray.put(ORDER_KEY + "12", (String) Optional.ofNullable(orders.get("emergencyProtectionOrderDetails"))
            .orElse(""));
        return orderType.isEmpty();
    }

    private void buildDirections(Map orders, ImmutableMap.Builder<String, String> directionsArray) {
        List directions = (List) Optional.ofNullable(orders.get("emergencyProtectionDirections"))
            .orElse(ImmutableList.builder().build());
        for (int i = 0; i < 5; i++) {
            if (i < directions.size()) {
                directionsArray.put(DIRECTIONS_KEY + i, "^" + directions.get(i));
            } else {
                directionsArray.put(DIRECTIONS_KEY + i, "");
            }
        }

        directionsArray.put(DIRECTIONS_KEY + "5",
            (String) Optional.ofNullable(orders.get("emergencyProtectionDirectionsDetails")).orElse(""));
        directionsArray.put(DIRECTIONS_KEY + "6",
            (String) Optional.ofNullable(orders.get("directionsDetails")).orElse(""));
    }
}
