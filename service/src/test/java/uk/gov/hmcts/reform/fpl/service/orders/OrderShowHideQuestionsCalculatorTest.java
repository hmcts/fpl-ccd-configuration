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
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;

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
            "previewOrder", "YES");

        Map<String, String> careOrderQuestions = new HashMap<>(commonQuestions);
        careOrderQuestions.putAll(Map.of(
            "furtherDirections", "YES",
            "approvalDate", "YES",
            "approvalDateTime", "NO",
            "epoIncludePhrase", "NO",
            "epoChildrenDescription", "NO",
            "epoExpiryDate", "NO",
            "epoTypeAndPreventRemoval", "NO",
            "orderDetails", "NO",
            "cafcassJurisdictions", "NO",
            "whichChildren", "YES"
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
            "orderDetails", "NO",
            "cafcassJurisdictions", "NO",
            "whichChildren", "YES"
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
            "epoTypeAndPreventRemoval", "NO",
            "cafcassJurisdictions", "NO",
            "whichChildren", "YES"
        ));

        Map<String, String> appointmentOfChildrensGuardianQuestions = new HashMap<>(commonQuestions);
        appointmentOfChildrensGuardianQuestions.putAll(Map.of(
            "approvalDate", "YES",
            "furtherDirections", "YES",
            "orderDetails", "NO",
            "approvalDateTime", "NO",
            "epoIncludePhrase", "NO",
            "epoChildrenDescription", "NO",
            "epoExpiryDate", "NO",
            "epoTypeAndPreventRemoval", "NO",
            "cafcassJurisdictions", "YES",
            "whichChildren", "NO"
        ));

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, appointmentOfChildrensGuardianQuestions)
        );
    }
}
