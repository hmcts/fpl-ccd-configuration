package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.config.utils.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@SuppressWarnings({"VariableDeclarationUsageDistance", "unchecked"})
public abstract class AbstractEmailContentProvider {

    private final String uiBaseUrl;

    protected AbstractEmailContentProvider(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    protected ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(CaseDetails caseDetails) {
        String dataPresent = "Yes";
        String fullStop = "No";
        String timeFramePresent = "No";

        Map orders = Optional.ofNullable((Map) caseDetails.getData().get("orders"))
            .orElse(ImmutableMap.builder().build());
        ImmutableList.Builder<String> ordersAndDirectionsBuilder = ImmutableList.builder();

        boolean isDataPresent = buildOrders(orders, ordersAndDirectionsBuilder);
        buildDirections(orders, ordersAndDirectionsBuilder);

        Map hearing = Optional.ofNullable((Map) caseDetails.getData().get("hearing"))
            .orElse(ImmutableMap.builder().build());

        if (hearing.containsKey("timeFrame")) {
            timeFramePresent = "Yes";
        }

        if (isDataPresent) {
            dataPresent = "No";
            fullStop = "Yes";
        }

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", ordersAndDirectionsBuilder.build())
            .put("dataPresent", dataPresent)
            .put("fullStop", fullStop)
            .put("timeFramePresent", timeFramePresent)
            .put("timeFrameValue", Optional.ofNullable((String) hearing.get("timeFrame")).orElse(""))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    private boolean buildOrders(Map orders, ImmutableList.Builder<String> setBuilder) {
        List<String> orderType = (List) Optional.ofNullable(orders.get("orderType"))
            .orElse(ImmutableList.builder().build());
        List<String> emergencyProtectionOrders = (List) Optional.ofNullable(orders.get("emergencyProtectionOrders"))
            .orElse(ImmutableList.builder().build());

        for (String order : orderType) {
            if (!Objects.equals(order, OrderType.OTHER)) {
                setBuilder.add(OrderType.valueOf(order).getLabel());
            }
        }
        for (String emergencyProtectionOrder : emergencyProtectionOrders) {
            if (!Objects.equals(emergencyProtectionOrder, EmergencyProtectionOrdersType.OTHER)) {
                setBuilder.add(EmergencyProtectionOrdersType
                    .valueOf(emergencyProtectionOrder).getLabel());
            }

        }

        Optional.ofNullable(orders.get("otherOrder")).ifPresent(otherOrder -> setBuilder.add((String) otherOrder));
        Optional.ofNullable(orders.get("emergencyProtectionOrderDetails"))
            .ifPresent(emergencyProtectionOrderDetails -> setBuilder.add((String) emergencyProtectionOrderDetails));

        return orderType.isEmpty();
    }

    private void buildDirections(Map orders, ImmutableList.Builder<String> setBuilder) {
        List<String> directions = (List) Optional.ofNullable(orders.get("emergencyProtectionOrderDirections"))
            .orElse(ImmutableList.builder().build());
        for (String direction : directions) {
            if (!Objects.equals(direction, EmergencyProtectionOrderDirectionsType.OTHER)) {
                setBuilder.add(EmergencyProtectionOrderDirectionsType
                    .valueOf(direction).getLabel());
            }
        }

        Optional.ofNullable(orders.get("emergencyProtectionOrderDirectionDetails"))
            .ifPresent(emergencyProtectionOrderDirectionDetails ->
                setBuilder.add((String) emergencyProtectionOrderDirectionDetails));

        Optional.ofNullable(orders.get("directionDetails"))
            .ifPresent(directionDetails -> setBuilder.add((String) directionDetails));
    }
}
