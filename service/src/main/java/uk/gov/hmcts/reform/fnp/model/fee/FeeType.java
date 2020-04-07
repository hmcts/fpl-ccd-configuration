package uk.gov.hmcts.reform.fnp.model.fee;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;

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

    private static final Map<OrderType, FeeType> orderToFeeMap;

    static {
        orderToFeeMap = Map.of(
            OrderType.CARE_ORDER, CARE_ORDER,
            OrderType.EDUCATION_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER,
            OrderType.EMERGENCY_PROTECTION_ORDER, EMERGENCY_PROTECTION_ORDER,
            OrderType.INTERIM_CARE_ORDER, INTERIM_CARE_ORDER,
            OrderType.INTERIM_SUPERVISION_ORDER, INTERIM_SUPERVISION_ORDER,
            OrderType.SUPERVISION_ORDER, SUPERVISION_ORDER,
            OrderType.OTHER, OTHER
        );
    }

    public static List<FeeType> fromOrderType(List<OrderType> orderTypes) {
        if (isEmpty(orderTypes)) {
            return ImmutableList.of();
        }

        return orderTypes.stream()
            .map(orderToFeeMap::get)
            .collect(toUnmodifiableList());
    }

    public static FeeType fromC2ApplicationType(C2ApplicationType c2ApplicationType) {
        if (c2ApplicationType == WITH_NOTICE) {
            return C2_WITH_NOTICE;
        }
        return C2_WITHOUT_NOTICE;
    }
}
