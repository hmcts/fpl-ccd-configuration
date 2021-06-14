package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSourceType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
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
            "hearingDetails", "YES",
            "approver", "YES",
            "previewOrder", "YES");

        Map<String, String> careOrderQuestions = new HashMap<>(commonQuestions);

        careOrderQuestions.put("furtherDirections", "YES");
        careOrderQuestions.put("approvalDate", "YES");
        careOrderQuestions.put("approvalDateTime", "NO");
        careOrderQuestions.put("epoIncludePhrase", "NO");
        careOrderQuestions.put("uploadOrderFile", "NO");
        careOrderQuestions.put("needSealing", "NO");
        careOrderQuestions.put("epoChildrenDescription", "NO");
        careOrderQuestions.put("epoExpiryDate", "NO");
        careOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        careOrderQuestions.put("orderDetails", "NO");
        careOrderQuestions.put("cafcassJurisdictions", "NO");
        careOrderQuestions.put("whichChildren", "YES");
        careOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        careOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        careOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        careOrderQuestions.put("closeCase", "YES");

        Map<String, String> epoQuestions = new HashMap<>(commonQuestions);
        epoQuestions.put("furtherDirections", "YES");
        epoQuestions.put("approvalDate", "NO");
        epoQuestions.put("approvalDateTime", "YES");
        epoQuestions.put("epoIncludePhrase", "YES");
        epoQuestions.put("uploadOrderFile", "NO");
        epoQuestions.put("needSealing", "NO");
        epoQuestions.put("epoChildrenDescription", "YES");
        epoQuestions.put("epoExpiryDate", "YES");
        epoQuestions.put("epoTypeAndPreventRemoval", "YES");
        epoQuestions.put("orderDetails", "NO");
        epoQuestions.put("cafcassJurisdictions", "NO");
        epoQuestions.put("whichChildren", "YES");
        epoQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        epoQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        epoQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        epoQuestions.put("closeCase", "NO");

        Map<String, String> blankOrderQuestions = new HashMap<>(commonQuestions);
        blankOrderQuestions.put("approvalDate", "YES");
        blankOrderQuestions.put("furtherDirections", "NO");
        blankOrderQuestions.put("orderDetails", "YES");
        blankOrderQuestions.put("approvalDateTime", "NO");
        blankOrderQuestions.put("epoIncludePhrase", "NO");
        blankOrderQuestions.put("uploadOrderFile", "NO");
        blankOrderQuestions.put("needSealing", "NO");
        blankOrderQuestions.put("epoChildrenDescription", "NO");
        blankOrderQuestions.put("epoExpiryDate", "NO");
        blankOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        blankOrderQuestions.put("whichChildren", "YES");
        blankOrderQuestions.put("cafcassJurisdictions", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        blankOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        blankOrderQuestions.put("closeCase", "NO");

        Map<String, String> supervisionOrderQuestions = new HashMap<>(commonQuestions);
        supervisionOrderQuestions.put("approvalDate", "YES");
        supervisionOrderQuestions.put("furtherDirections", "YES");
        supervisionOrderQuestions.put("orderDetails", "NO");
        supervisionOrderQuestions.put("approvalDateTime", "NO");
        supervisionOrderQuestions.put("epoIncludePhrase", "NO");
        supervisionOrderQuestions.put("uploadOrderFile", "NO");
        supervisionOrderQuestions.put("needSealing", "NO");
        supervisionOrderQuestions.put("epoChildrenDescription", "NO");
        supervisionOrderQuestions.put("epoExpiryDate", "NO");
        supervisionOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        supervisionOrderQuestions.put("cafcassJurisdictions", "NO");
        supervisionOrderQuestions.put("whichChildren", "YES");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        supervisionOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        supervisionOrderQuestions.put("closeCase", "YES");

        Map<String, String> appointmentOfChildrensGuardianQuestions = new HashMap<>(commonQuestions);
        appointmentOfChildrensGuardianQuestions.put("approvalDate", "YES");
        appointmentOfChildrensGuardianQuestions.put("furtherDirections", "YES");
        appointmentOfChildrensGuardianQuestions.put("orderDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("approvalDateTime", "NO");
        appointmentOfChildrensGuardianQuestions.put("epoIncludePhrase", "NO");
        appointmentOfChildrensGuardianQuestions.put("uploadOrderFile", "NO");
        appointmentOfChildrensGuardianQuestions.put("needSealing", "NO");
        appointmentOfChildrensGuardianQuestions.put("epoChildrenDescription", "NO");
        appointmentOfChildrensGuardianQuestions.put("epoExpiryDate", "NO");
        appointmentOfChildrensGuardianQuestions.put("epoTypeAndPreventRemoval", "NO");
        appointmentOfChildrensGuardianQuestions.put("cafcassJurisdictions", "YES");
        appointmentOfChildrensGuardianQuestions.put("whichChildren", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        appointmentOfChildrensGuardianQuestions.put("closeCase", "NO");

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C35A_SUPERVISION_ORDER, supervisionOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, appointmentOfChildrensGuardianQuestions)
        );
    }

    @ParameterizedTest(name = "Show hide map for upload order {0}")
    @MethodSource("manualUploadOrders")
    void calculateManualUpload(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> manualUploadOrders() {
        return Arrays.stream(Order.values())
            .filter(order -> OrderSourceType.MANUAL_UPLOAD == order.getSourceType())
            .map(order -> Arguments.of(order, Map.ofEntries(
                Map.entry("approver", "NO"),
                Map.entry("previewOrder", "YES"),
                Map.entry("furtherDirections", "NO"),
                Map.entry("orderDetails", "NO"),
                Map.entry("whichChildren", "YES"),
                Map.entry("hearingDetails", "NO"),
                Map.entry("approvalDate", "YES"),
                Map.entry("approvalDateTime", "NO"),
                Map.entry("epoIncludePhrase", "NO"),
                Map.entry("epoExpiryDate", "NO"),
                Map.entry("epoTypeAndPreventRemoval", "NO"),
                Map.entry("epoChildrenDescription", "NO"),
                Map.entry("manageOrdersExclusionRequirementDetails", "NO"),
                Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO"),
                Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
                Map.entry("cafcassJurisdictions", "NO"),
                Map.entry("needSealing", "YES"),
                Map.entry("uploadOrderFile", "YES"),
                Map.entry("closeCase", "YES")
                )
            ));
    }
}
