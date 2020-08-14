package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;

@ExtendWith(MockitoExtension.class)
class OrdersSoughtCheckerIsStartedTest {

    @InjectMocks
    private OrdersSoughtChecker ordersSoughtChecker;

    @ParameterizedTest
    @NullSource
    @MethodSource("emptyOrders")
    void shouldReturnFalseWhenEmptyOrdersSought(Orders orders) {
        final CaseData caseData = CaseData.builder()
                .orders(orders)
                .build();

        assertThat(ordersSoughtChecker.isStarted(caseData)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonEmptyOrders")
    void shouldReturnTrueWhenNonEmptyOrdersSought(Orders orders) {
        final CaseData caseData = CaseData.builder()
                .orders(orders)
                .build();

        assertThat(ordersSoughtChecker.isStarted(caseData)).isTrue();
    }

    private static Stream<Arguments> nonEmptyOrders() {
        return Stream.of(
                Orders.builder().orderType(List.of(CARE_ORDER)).build(),
                Orders.builder().directions("Test").build())
                .map(Arguments::of);
    }

    private static Stream<Arguments> emptyOrders() {
        return Stream.of(
                Orders.builder()
                        .build(),
                Orders.builder()
                        .directions("")
                        .orderType(emptyList())
                        .build())
                .map(Arguments::of);
    }
}
