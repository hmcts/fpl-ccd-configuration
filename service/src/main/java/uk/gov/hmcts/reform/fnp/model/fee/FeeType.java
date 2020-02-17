package uk.gov.hmcts.reform.fnp.model.fee;

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
        switch (orderType) {
            case CARE_ORDER:
                return CARE_ORDER;
            case INTERIM_CARE_ORDER:
                return INTERIM_CARE_ORDER;
            case SUPERVISION_ORDER:
                return SUPERVISION_ORDER;
            case INTERIM_SUPERVISION_ORDER:
                return INTERIM_SUPERVISION_ORDER;
            case EDUCATION_SUPERVISION_ORDER:
                return EDUCATION_SUPERVISION_ORDER;
            case EMERGENCY_PROTECTION_ORDER:
                return EMERGENCY_PROTECTION_ORDER;
            case OTHER:
                return OTHER;
            default:
                throw new IllegalStateException("Unexpected value: " + orderType);
        }
    }
}
