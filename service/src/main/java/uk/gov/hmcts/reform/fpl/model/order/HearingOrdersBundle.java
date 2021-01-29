package uk.gov.hmcts.reform.fpl.model.order;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Data
@Builder(toBuilder = true)
public class HearingOrdersBundle {
    private UUID hearingId;
    private String hearingName;
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
}
