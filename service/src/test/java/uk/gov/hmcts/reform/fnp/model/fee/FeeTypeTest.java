package uk.gov.hmcts.reform.fnp.model.fee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.OrderType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2ApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromOrderType;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;

class FeeTypeTest {
    private static Stream<Arguments> orderToFeeTypeSource() {
        // Will throw an IllegalArgumentException if there is no corresponding FeeType
        return Arrays.stream(OrderType.values())
            .map(orderType -> Arguments.of(List.of(orderType), List.of(FeeType.valueOf(orderType.name()))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyListWhenNullOrEmptyListIsPassed(List<OrderType> list) {
        assertThat(fromOrderType(list)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("orderToFeeTypeSource")
    void shouldReturnCorrespondingFeeTypeForOrderType(List<OrderType> orderType, List<FeeType> feeType) {
        assertThat(fromOrderType(orderType)).isEqualTo(feeType);
    }

    @Test
    void shouldReturnCorrespondingFeeTypeForC2ApplicationType() {
        assertThat(fromC2ApplicationType(WITH_NOTICE)).isEqualTo(C2_WITH_NOTICE);
        assertThat(fromC2ApplicationType(WITHOUT_NOTICE)).isEqualTo(C2_WITHOUT_NOTICE);
    }
}
