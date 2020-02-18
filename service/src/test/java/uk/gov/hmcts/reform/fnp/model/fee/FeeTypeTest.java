package uk.gov.hmcts.reform.fnp.model.fee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.OTHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromOrderType;

class FeeTypeTest {
    private static Stream<Arguments> orderToFeeTypeSource() {
        return Stream.of(
            Arguments.of(OrderType.CARE_ORDER, CARE_ORDER),
            Arguments.of(OrderType.EDUCATION_SUPERVISION_ORDER, EDUCATION_SUPERVISION_ORDER),
            Arguments.of(OrderType.EMERGENCY_PROTECTION_ORDER, EMERGENCY_PROTECTION_ORDER),
            Arguments.of(OrderType.INTERIM_CARE_ORDER, INTERIM_CARE_ORDER),
            Arguments.of(OrderType.INTERIM_SUPERVISION_ORDER, INTERIM_SUPERVISION_ORDER),
            Arguments.of(OrderType.OTHER, OTHER),
            Arguments.of(OrderType.SUPERVISION_ORDER, SUPERVISION_ORDER)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenNullOrEmptyListIsPassed(List<OrderType> list) {
        assertThat(fromOrderType(list)).isEmpty();
    }

    @Test
    void shouldReturnAListWithoutNullsWhenSourceListHasNulls() {
        List<OrderType> orderTypeList = new ArrayList<>();
        orderTypeList.add(OrderType.CARE_ORDER);
        orderTypeList.add(null);
        orderTypeList.add(OrderType.OTHER);

        assertThat(fromOrderType(orderTypeList)).containsOnly(CARE_ORDER, OTHER);
    }

    @ParameterizedTest
    @MethodSource("orderToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForOrderType(OrderType orderType, FeeType feeType) {
        assertThat(fromOrderType(orderType)).isEqualTo(feeType);
    }

    @ParameterizedTest
    @NullSource
    void shouldReturnNullWhenNullIsPassed(OrderType undefined) {
        assertThat(fromOrderType(undefined)).isNull();
    }
}
