package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.ConfidentialOrderBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.removeElementWithUUID;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class HearingOrdersBundle implements ConfidentialOrderBundle<HearingOrder> {
    private UUID hearingId;
    String hearingName;
    private String judgeTitleAndName;
    private List<Element<HearingOrder>> orders;
    private List<Element<HearingOrder>> ordersCTSC;
    private List<Element<HearingOrder>> ordersLA;
    private List<Element<HearingOrder>> ordersResp0;
    private List<Element<HearingOrder>> ordersResp1;
    private List<Element<HearingOrder>> ordersResp2;
    private List<Element<HearingOrder>> ordersResp3;
    private List<Element<HearingOrder>> ordersResp4;
    private List<Element<HearingOrder>> ordersResp5;
    private List<Element<HearingOrder>> ordersResp6;
    private List<Element<HearingOrder>> ordersResp7;
    private List<Element<HearingOrder>> ordersResp8;
    private List<Element<HearingOrder>> ordersResp9;
    private List<Element<HearingOrder>> ordersChild0;
    private List<Element<HearingOrder>> ordersChild1;
    private List<Element<HearingOrder>> ordersChild2;
    private List<Element<HearingOrder>> ordersChild3;
    private List<Element<HearingOrder>> ordersChild4;
    private List<Element<HearingOrder>> ordersChild5;
    private List<Element<HearingOrder>> ordersChild6;
    private List<Element<HearingOrder>> ordersChild7;
    private List<Element<HearingOrder>> ordersChild8;
    private List<Element<HearingOrder>> ordersChild9;
    private List<Element<HearingOrder>> ordersChild10;
    private List<Element<HearingOrder>> ordersChild11;
    private List<Element<HearingOrder>> ordersChild12;
    private List<Element<HearingOrder>> ordersChild13;
    private List<Element<HearingOrder>> ordersChild14;

    public HearingOrdersBundle updateHearing(UUID hearingId, HearingBooking hearing) {
        if (!isNull(hearing)) {
            this.setHearingId(hearingId);
            this.setHearingName(hearing.toLabel());
            this.setJudgeTitleAndName(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()));
        } else {
            this.setHearingId(hearingId);
            this.setHearingName("No hearing");
            this.setJudgeTitleAndName(null);
        }

        return this;
    }

    public HearingOrdersBundle updateOrders(List<Element<HearingOrder>> newOrders, HearingOrderType type) {
        orders = defaultIfNull(orders, new ArrayList<>());
        newOrders.stream()
            .forEach(order -> {
                order.getValue().setType(type);
                Optional<Integer> orderIndex = checkForExistingOrders(orders, order.getId());

                if (orderIndex.isEmpty()) {
                    orders.add(order);
                } else {
                    orders.set(orderIndex.get(), order);
                }
            });

        return this;
    }

    private Optional<Integer> checkForExistingOrders(List<Element<HearingOrder>> existingOrders, UUID orderId) {
        for (int i = 0; i < existingOrders.size(); i++) {
            if (existingOrders.get(i).getId().equals(orderId)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public List<Element<HearingOrder>> getOrders() {
        if (isNotEmpty(orders)) {
            orders.sort(comparingInt(order -> order.getValue().getType().ordinal()));
        }
        if (orders == null) {
            orders = newArrayList();
        }
        return orders;
    }

    public List<Element<HearingOrder>> getOrders(CMOStatus status) {
        List<Element<HearingOrder>> hearingOrders = defaultIfNull(getOrders(), newArrayList());

        return hearingOrders.stream()
            .filter(order -> status.equals(order.getValue().getStatus()))
            .collect(Collectors.toList());
    }

    public List<Element<HearingOrder>> getAllConfidentialOrdersByStatus(CMOStatus status) {
        List<Element<HearingOrder>> hearingOrders = defaultIfNull(getAllConfidentialOrders(), newArrayList());

        return hearingOrders.stream()
            .filter(order -> status.equals(order.getValue().getStatus()))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Element<HearingOrder>> getCaseManagementOrders() {
        if (isNotEmpty(orders)) {
            return orders.stream()
                .filter(hearingOrderElement -> hearingOrderElement.getValue().getType().isCmo())
                .collect(Collectors.toList());
        }

        return List.of();
    }

    @JsonIgnore
    public void updateConfidentialOrders(List<Element<HearingOrder>> newOrders,
                                         HearingOrderType type,
                                         List<String> suffixList) {
        suffixList.forEach(suffix -> {
            List<Element<HearingOrder>> confidentialOrders =
                defaultIfNull(getConfidentialOrdersBySuffix(suffix), new ArrayList<>());

            newOrders.forEach(newOrder -> {
                newOrder.getValue().setType(type);
                Optional<Integer> orderIndex = checkForExistingOrders(confidentialOrders, newOrder.getId());
                if (orderIndex.isEmpty()) {
                    confidentialOrders.add(newOrder);
                } else {
                    confidentialOrders.set(orderIndex.get(), newOrder);
                }
            });
            setConfidentialOrdersBySuffix(suffix, confidentialOrders);
        });
    }

    @Override
    public String getFieldBaseName() {
        return "orders";
    }

    public void removeOrderElement(Element<HearingOrder> orderToBeRemoved) {
        if (findElement(orderToBeRemoved.getId(), orders).isPresent()) {
            orders = removeElementWithUUID(orders, orderToBeRemoved.getId());
        } else {
            processAllConfidentialOrders((suffix, confidentialOrders) -> {
                if (findElement(orderToBeRemoved.getId(), confidentialOrders).isPresent()) {
                    setConfidentialOrdersBySuffix(suffix,
                        removeElementWithUUID(confidentialOrders, orderToBeRemoved.getId()));
                }
            });
        }
    }
}
