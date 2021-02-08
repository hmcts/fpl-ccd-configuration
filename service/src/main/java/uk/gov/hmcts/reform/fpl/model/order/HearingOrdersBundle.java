package uk.gov.hmcts.reform.fpl.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
public class HearingOrdersBundle {
    private UUID hearingId;
    String hearingName;
    private String judgeTitleAndName;
    private List<Element<HearingOrder>> orders;

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
        newOrders.stream()
            .map(Element::getValue)
            .forEach(order -> order.setType(type));

        orders = defaultIfNull(orders, new ArrayList<>());
        orders.removeIf(order -> Objects.equals(order.getValue().getType(), type));
        orders.addAll(newOrders);

        return this;
    }

    public List<Element<HearingOrder>> getOrders() {
        if (isNotEmpty(orders)) {
            orders.sort(comparingInt(order -> order.getValue().getType().ordinal()));
        }
        return orders;
    }

    public List<Element<HearingOrder>> getOrders(CMOStatus status) {
        List<Element<HearingOrder>> hearingOrders = defaultIfNull(getOrders(), newArrayList());

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
}
