package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;

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
            "approver", "YES",
            "previewOrder", "YES",
            "whichChildren", "YES");

        Map<String, String> careOrderQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("whichChildren", "YES"),
            Map.entry("furtherDirections", "YES"),
            Map.entry("approvalDate", "YES"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("orderDetails", "NO"),
            Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
            Map.entry("exclusionRequirementDetails", "NO"),
            Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO")
        );

        Map<String, String> epoQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("whichChildren", "YES"),

            Map.entry("approvalDate", "NO"),
            Map.entry("furtherDirections", "YES"),
            Map.entry("orderDetails", "NO"),
            Map.entry("approvalDateTime", "YES"),
            Map.entry("epoIncludePhrase", "YES"),
            Map.entry("epoChildrenDescription", "YES"),
            Map.entry("epoExpiryDate", "YES"),
            Map.entry("epoTypeAndPreventRemoval", "YES"),
            Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
            Map.entry("exclusionRequirementDetails", "NO"),
            Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO")
        );

        Map<String, String> blankOrderQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("whichChildren", "YES"),

            Map.entry("approvalDate", "YES"),
            Map.entry("furtherDirections", "NO"),
            Map.entry("orderDetails", "YES"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
            Map.entry("exclusionRequirementDetails", "NO"),
            Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO")
        );

        Map<String, String> supervisionOrderQuestions = Map.ofEntries(
            Map.entry("approver", "YES"),
            Map.entry("previewOrder", "YES"),
            Map.entry("whichChildren", "YES"),

            Map.entry("approvalDate", "YES"),
            Map.entry("furtherDirections", "YES"),
            Map.entry("orderDetails", "NO"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("manageOrdersExpiryDateWithMonth", "YES"),
            Map.entry("exclusionRequirementDetails", "NO"),
            Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO")
        );

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C35A_SUPERVISION_ORDER, supervisionOrderQuestions)
        );
    }
}
