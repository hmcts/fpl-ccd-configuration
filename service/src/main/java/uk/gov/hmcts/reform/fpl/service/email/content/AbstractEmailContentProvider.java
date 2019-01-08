package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.config.utils.OrderType;

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

    protected ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(CaseDetails caseDetails) {
        String dataPresent = "Yes";
        String fullStop = "No";
        String timeFramePresent = "No";

        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get("orders")).orElse(ImmutableMap.builder().build());
        ImmutableSet.Builder<String> ordersAndDirectionsBuilder = ImmutableSet.builder();

        boolean isDataPresent = buildOrders(orders, ordersAndDirectionsBuilder);
        buildDirections(orders, ordersAndDirectionsBuilder);

        List<String> ordersAndDirectionsList = ImmutableList.copyOf(ordersAndDirectionsBuilder.build());

        Map hearing =
            Optional.ofNullable((Map) caseDetails.getData().get("hearing")).orElse(ImmutableMap.builder().build());

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        }

        if (isDataPresent) {
            dataPresent = "No";
            fullStop = "Yes";
        }

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", ordersAndDirectionsList)
            .put("dataPresent", dataPresent)
            .put("fullStop", fullStop)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    private boolean buildOrders(Map orders, ImmutableSet.Builder<String> setBuilder) {
        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());
        List emergencyProtectionOrders = (List) Optional.ofNullable(orders.get("emergencyProtectionOrders"))
            .orElse(ImmutableList.builder().build());

        for (int i = 0; i < orderType.size(); i++) {
            if (orderType.get(i) != "OTHER") {
                setBuilder.add(OrderType.valueOf((String) orderType.get(i)).getLabel());
            }
        }
        for (int i = 0; i < emergencyProtectionOrders.size(); i++) {
            if (emergencyProtectionOrders.get(i) != "OTHER") {
                setBuilder.add(EmergencyProtectionOrdersType
                    .valueOf((String) emergencyProtectionOrders.get(i)).getLabel());
            }

        }

        String otherOrder = (String) Optional.ofNullable(orders.get("otherOrder")).orElse("");
        String emergencyProtectionOrderDetails = (String) Optional.ofNullable(orders
            .get("emergencyProtectionOrderDetails")).orElse("");

        if (!otherOrder.isEmpty()) {
            setBuilder.add(otherOrder);
        }
        if (!emergencyProtectionOrderDetails.isEmpty()) {
            setBuilder.add(emergencyProtectionOrderDetails);
        }

        return orderType.isEmpty();
    }

    private void buildDirections(Map orders, ImmutableSet.Builder<String> setBuilder) {
        List directions = (List) Optional.ofNullable(orders.get("emergencyProtectionOrderDirections"))
            .orElse(ImmutableList.builder().build());
        for (int i = 0; i < directions.size(); i++) {
            if (directions.get(i) != "OTHER") {
                setBuilder.add(EmergencyProtectionOrderDirectionsType
                    .valueOf((String) directions.get(i)).getLabel());
            }
        }

        String emergencyProtectionOrderDirectionDetails =
            (String) Optional.ofNullable(orders.get("emergencyProtectionOrderDirectionDetails")).orElse("");
        String directionDetails = (String) Optional.ofNullable(orders.get("directionDetails")).orElse("");

        if (!emergencyProtectionOrderDirectionDetails.isEmpty()) {
            setBuilder.add(emergencyProtectionOrderDirectionDetails);
        }
        if (!directionDetails.isEmpty()) {
            setBuilder.add(directionDetails);
        }
    }
}
