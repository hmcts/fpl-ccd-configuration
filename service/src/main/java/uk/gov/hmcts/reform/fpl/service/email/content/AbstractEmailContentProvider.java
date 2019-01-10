package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.config.utils.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@SuppressWarnings({"LineLength", "VariableDeclarationUsageDistance"})
public abstract class AbstractEmailContentProvider {

    private final String uiBaseUrl;

    protected AbstractEmailContentProvider(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    @SuppressWarnings("unchecked")
    protected ImmutableMap.Builder<String, Object> getCasePersonalisationBuilder(CaseDetails caseDetails) {
        List<String> ordersAndDirections = buildOrdersAndDirections((Map<String, Object>) caseDetails.getData().get("orders"));

        Optional<String> timeFrame = Optional.ofNullable((Map<String, Object>) caseDetails.getData().get("hearing"))
            .map(hearing -> (String) hearing.get("timeFrame"));

        return ImmutableMap.<String, Object>builder()
            .put("ordersAndDirections", !ordersAndDirections.isEmpty() ? ordersAndDirections : "")
            .put("dataPresent", !ordersAndDirections.isEmpty() ? "Yes" : "No")
            .put("fullStop", !ordersAndDirections.isEmpty() ? "No" : "Yes")
            .put("timeFramePresent", timeFrame.isPresent() ? "Yes" : "No")
            .put("timeFrameValue", timeFrame.orElse(""))
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId());
    }

    private List<String> buildOrdersAndDirections(Map<String, Object> optionalOrders) {
        ImmutableList.Builder<String> ordersAndDirectionsBuilder = ImmutableList.builder();

        Optional.ofNullable(optionalOrders).ifPresent(orders -> {
            appendOrders(orders, ordersAndDirectionsBuilder);
            appendDirections(orders, ordersAndDirectionsBuilder);
        });

        return ordersAndDirectionsBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void appendOrders(Map<String, Object> orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.get("orderType")).ifPresent(orderTypes -> {
            for (String typeString : (List<String>) orderTypes) {
                OrderType type = OrderType.valueOf(typeString);
                if (type != OrderType.OTHER) {
                    builder.add(type.getLabel());
                }
            }
        });

        Optional.ofNullable(orders.get("emergencyProtectionOrders")).ifPresent(emergencyProtectionOrders -> {
            for (String typeString : (List<String>) emergencyProtectionOrders) {
                EmergencyProtectionOrdersType type = EmergencyProtectionOrdersType.valueOf(typeString);
                if (type != EmergencyProtectionOrdersType.OTHER) {
                    builder.add(type.getLabel());
                }
            }
        });

        Optional.ofNullable(orders.get("otherOrder")).ifPresent(otherOrder ->
            builder.add((String) otherOrder));
        Optional.ofNullable(orders.get("emergencyProtectionOrderDetails")).ifPresent(emergencyProtectionOrderDetails ->
            builder.add((String) emergencyProtectionOrderDetails));
    }

    @SuppressWarnings("unchecked")
    private void appendDirections(Map<String, Object> orders, ImmutableList.Builder<String> builder) {
        Optional.ofNullable(orders.get("emergencyProtectionOrderDirections")).ifPresent(emergencyProtectionOrderDirections -> {
            for (String typeString : (List<String>) emergencyProtectionOrderDirections) {
                EmergencyProtectionOrderDirectionsType type = EmergencyProtectionOrderDirectionsType.valueOf(typeString);
                if (type != EmergencyProtectionOrderDirectionsType.OTHER) {
                    builder.add(type.getLabel());
                }
            }
        });

        Optional.ofNullable(orders.get("emergencyProtectionOrderDirectionDetails")).ifPresent(emergencyProtectionOrderDirectionDetails ->
            builder.add((String) emergencyProtectionOrderDirectionDetails));

        Optional.ofNullable(orders.get("directionDetails")).ifPresent(directionDetails ->
            builder.add((String) directionDetails));
    }
}
