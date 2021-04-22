package uk.gov.hmcts.reform.fpl.service.orders;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private final OrderShowHideQuestionsCalculator underTest = new OrderShowHideQuestionsCalculator();

    @ParameterizedTest
    @MethodSource("orderWithExpectedMap")
    void calculate(Order order, Map<String, String> expectedShowHideMap) {
        Assertions.assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> orderWithExpectedMap() {
        Map<String, String> commonQuestions = Map.of(
            "approver", "YES",
            "previewOrder", "YES",
            "furtherDirections", "YES",
            "whichChildren", "YES");

        Map<String, String> careOrderQuestions = new HashMap<>(commonQuestions);
        careOrderQuestions.putAll(Map.of(
            "approvalDate", "YES",
            "approvalDateTime", "NO",
            "epoOrderType", "NO",
            "epoIncludePhrase", "NO",
            "epoChildrenDescription", "NO",
            "epoExpiryDate", "NO",
            "epoPreventRemoval", "NO"
        ));

        Map<String, String> epoQuestions = new HashMap<>(commonQuestions);
        epoQuestions.putAll(Map.of(
            "approvalDate", "NO",
            "approvalDateTime", "YES",
            "epoOrderType", "YES",
            "epoIncludePhrase", "YES",
            "epoChildrenDescription", "YES",
            "epoExpiryDate", "YES",
            "epoPreventRemoval", "YES"
        ));

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions)
        );
    }
}
