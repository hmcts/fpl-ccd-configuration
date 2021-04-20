package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private final OrderShowHideQuestionsCalculator underTest = new OrderShowHideQuestionsCalculator();

    @ParameterizedTest
    @MethodSource("blankOrderWithExpectedMap")
    void calculateBlankOrder(Order order, Map<String,String> expectedShowHideMap) {
        assertThat(underTest.calculate(C21_BLANK_ORDER)).isEqualTo(expectedShowHideMap);
    }

    @ParameterizedTest
    @MethodSource("careOrderWithExpectedMap")
    void calculateCareOrder(Order order, Map<String,String> expectedShowHideMap) {
        assertThat(underTest.calculate(Order.C32_CARE_ORDER)).isEqualTo(expectedShowHideMap);
    }

    private static Stream<Arguments> careOrderWithExpectedMap() {
        return Stream.of(
            Arguments.of(C32_CARE_ORDER, Map.of(
                "approvalDate", "YES",
                "approver", "YES",
                "previewOrder", "YES",
                "furtherDirections", "YES",
                "orderDetails", "NO",
                "whichChildren", "YES"
            ))

        );
    }

    private static Stream<Arguments> blankOrderWithExpectedMap() {
        return Stream.of(
            Arguments.of(C21_BLANK_ORDER, Map.of(
                "approvalDate", "YES",
                "approver", "YES",
                "previewOrder", "YES",
                "furtherDirections", "NO",
                "orderDetails", "YES",
                "whichChildren", "YES"
            ))

        );
    }
}
