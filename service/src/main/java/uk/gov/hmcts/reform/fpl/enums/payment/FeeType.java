package uk.gov.hmcts.reform.fpl.enums.payment;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

public enum FeeType {
    C2_WITHOUT_NOTICE,
    C2_WITH_NOTICE,
    CARE_ORDER,
    EDUCATION_SUPERVISION_ORDER,
    EMERGENCY_PROTECTION_ORDER,
    INTERIM_CARE_ORDER,
    INTERIM_SUPERVISION_ORDER,
    OTHER,
    PLACEMENT,
    SUPERVISION_ORDER;

    public static List<FeeType> fromOrderType(List<OrderType> orderTypes) {
        if (orderTypes == null) {
            return ImmutableList.of();
        }

        return orderTypes.stream()
            .map(FeeType::fromOrderType)
            .collect(toUnmodifiableList());
    }

    public static FeeType fromOrderType(OrderType orderType) {
        return FeeType.valueOf(orderType.name());
    }
}
