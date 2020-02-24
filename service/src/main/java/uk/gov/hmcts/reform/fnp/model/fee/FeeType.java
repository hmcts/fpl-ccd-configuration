package uk.gov.hmcts.reform.fnp.model.fee;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

public enum FeeType {
    // Names should match OrderType enum names exactly
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

    private static final Map<OrderType, FeeType> orderToFeeMap = buildOrderFeeMap();

    public static FeeType fromOrderType(@Nonnull OrderType orderType) {
        return orderToFeeMap.get(orderType);
    }

    public static List<FeeType> fromOrderType(List<OrderType> orderTypes) {
        if (isEmpty(orderTypes)) {
            return ImmutableList.of();
        }

        return orderTypes.stream()
            .map(FeeType::fromOrderType)
            .filter(Objects::nonNull)
            .collect(toUnmodifiableList());
    }

    private static Map<OrderType, FeeType> buildOrderFeeMap() {
        return Map.of(
            OrderType.CARE_ORDER, CARE_ORDER,
            OrderType.EDUCATION_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER,
            OrderType.EMERGENCY_PROTECTION_ORDER, EMERGENCY_PROTECTION_ORDER,
            OrderType.INTERIM_CARE_ORDER, INTERIM_CARE_ORDER,
            OrderType.INTERIM_SUPERVISION_ORDER, INTERIM_SUPERVISION_ORDER,
            OrderType.SUPERVISION_ORDER, SUPERVISION_ORDER,
            OrderType.OTHER, OTHER
        );
    }
}
