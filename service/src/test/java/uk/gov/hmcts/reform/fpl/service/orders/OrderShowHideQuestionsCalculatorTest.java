package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private final OrderShowHideQuestionsCalculator underTest = new OrderShowHideQuestionsCalculator();

    @ParameterizedTest(name = "Show hide map for {0}")
    @MethodSource("orderWithExpectedMap")
    void calculate(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> orderWithExpectedMap() {
        Map<String, String> commonQuestions = Map.of(
            "linkedToHearing", "YES",
            "approver", "YES",
            "previewOrder", "YES",
            "whichChildren", "YES");

        Map<String, String> careOrderQuestions = new HashMap<>(commonQuestions);
        careOrderQuestions.putAll(Map.of(
            "furtherDirections", "YES",
            "approvalDate", "YES",
            "approvalDateTime", "NO",
            "epoIncludePhrase", "NO",
            "epoChildrenDescription", "NO",
            "epoExpiryDate", "NO",
            "epoTypeAndPreventRemoval", "NO",
            "orderDetails", "NO"
        ));

        Map<String, String> epoQuestions = new HashMap<>(commonQuestions);
        epoQuestions.putAll(Map.of(
            "furtherDirections", "YES",
            "approvalDate", "NO",
            "approvalDateTime", "YES",
            "epoIncludePhrase", "YES",
            "epoChildrenDescription", "YES",
            "epoExpiryDate", "YES",
            "epoTypeAndPreventRemoval", "YES",
            "orderDetails", "NO"
        ));

        Map<String, String> blankOrderQuestions = new HashMap<>(commonQuestions);
        blankOrderQuestions.putAll(Map.of(
            "approvalDate", "YES",
            "furtherDirections", "NO",
            "orderDetails", "YES",
            "approvalDateTime", "NO",
            "epoIncludePhrase", "NO",
            "epoChildrenDescription", "NO",
            "epoExpiryDate", "NO",
            "epoTypeAndPreventRemoval", "NO"
        ));

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions)
        );
    }
}
