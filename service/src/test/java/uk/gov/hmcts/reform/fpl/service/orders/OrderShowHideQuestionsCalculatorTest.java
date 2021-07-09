package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSourceType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.Order.OTHER_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private static final Set<Order> ORDERS_WITH_IS_FINAL_ORDER_QUESTION = Set.of(
        C37_EDUCATION_SUPERVISION_ORDER,
        OTHER_ORDER
    );

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
            "linkApplication", "YES",
            "approver", "YES",
            "previewOrder", "YES");

        Map<String, String> careOrderQuestions = new HashMap<>(commonQuestions);
        careOrderQuestions.put("orderTitle", "NO");
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
        careOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        careOrderQuestions.put("isFinalOrder", "NO");
        careOrderQuestions.put("closeCase", "YES");
        careOrderQuestions.put("whichOthers", "YES");
        careOrderQuestions.put("dischargeOfCareDetails", "NO");
        careOrderQuestions.put("orderIsByConsent", "NO");
        careOrderQuestions.put("appointedGuardian", "NO");
        careOrderQuestions.put("parentResponsible", "NO");

        Map<String, String> dischargeOfCareQuestions = new HashMap<>(commonQuestions);
        dischargeOfCareQuestions.put("orderTitle", "NO");
        dischargeOfCareQuestions.put("furtherDirections", "YES");
        dischargeOfCareQuestions.put("approvalDate", "YES");
        dischargeOfCareQuestions.put("approvalDateTime", "NO");
        dischargeOfCareQuestions.put("epoIncludePhrase", "NO");
        dischargeOfCareQuestions.put("uploadOrderFile", "NO");
        dischargeOfCareQuestions.put("needSealing", "NO");
        dischargeOfCareQuestions.put("epoChildrenDescription", "NO");
        dischargeOfCareQuestions.put("epoExpiryDate", "NO");
        dischargeOfCareQuestions.put("epoTypeAndPreventRemoval", "NO");
        dischargeOfCareQuestions.put("orderDetails", "NO");
        dischargeOfCareQuestions.put("cafcassJurisdictions", "NO");
        dischargeOfCareQuestions.put("whichChildren", "YES");
        dischargeOfCareQuestions.put("dischargeOfCareDetails", "YES");
        dischargeOfCareQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        dischargeOfCareQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        dischargeOfCareQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        dischargeOfCareQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        dischargeOfCareQuestions.put("isFinalOrder", "YES");
        dischargeOfCareQuestions.put("whichOthers", "YES");
        dischargeOfCareQuestions.put("closeCase", "YES");
        dischargeOfCareQuestions.put("orderIsByConsent", "NO");
        dischargeOfCareQuestions.put("appointedGuardian", "NO");
        dischargeOfCareQuestions.put("parentResponsible", "NO");

        Map<String, String> epoQuestions = new HashMap<>(commonQuestions);
        epoQuestions.put("orderTitle", "NO");
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
        epoQuestions.put("dischargeOfCareDetails", "NO");
        epoQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        epoQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        epoQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        epoQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        epoQuestions.put("closeCase", "NO");
        epoQuestions.put("whichOthers", "YES");
        epoQuestions.put("isFinalOrder", "NO");
        epoQuestions.put("orderIsByConsent", "NO");
        epoQuestions.put("appointedGuardian", "NO");
        epoQuestions.put("parentResponsible", "NO");

        Map<String, String> blankOrderQuestions = new HashMap<>(commonQuestions);
        blankOrderQuestions.put("orderTitle", "YES");
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
        blankOrderQuestions.put("dischargeOfCareDetails", "NO");
        blankOrderQuestions.put("cafcassJurisdictions", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        blankOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        blankOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        blankOrderQuestions.put("closeCase", "NO");
        blankOrderQuestions.put("whichOthers", "YES");
        blankOrderQuestions.put("isFinalOrder", "NO");
        blankOrderQuestions.put("orderIsByConsent", "NO");
        blankOrderQuestions.put("appointedGuardian", "NO");
        blankOrderQuestions.put("parentResponsible", "NO");

        Map<String, String> supervisionOrderQuestions = new HashMap<>(commonQuestions);
        supervisionOrderQuestions.put("orderTitle", "NO");
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
        supervisionOrderQuestions.put("dischargeOfCareDetails", "NO");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        supervisionOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        supervisionOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        supervisionOrderQuestions.put("closeCase", "YES");
        supervisionOrderQuestions.put("whichOthers", "YES");
        supervisionOrderQuestions.put("isFinalOrder", "NO");
        supervisionOrderQuestions.put("orderIsByConsent", "NO");
        supervisionOrderQuestions.put("appointedGuardian", "NO");
        supervisionOrderQuestions.put("parentResponsible", "NO");

        Map<String, String> specialGuardianshipOrderQuestions = new HashMap<>(commonQuestions);
        specialGuardianshipOrderQuestions.put("orderTitle", "NO");
        specialGuardianshipOrderQuestions.put("approvalDate", "NO");
        specialGuardianshipOrderQuestions.put("furtherDirections", "YES");
        specialGuardianshipOrderQuestions.put("orderDetails", "NO");
        specialGuardianshipOrderQuestions.put("approvalDateTime", "YES");
        specialGuardianshipOrderQuestions.put("epoIncludePhrase", "NO");
        specialGuardianshipOrderQuestions.put("epoChildrenDescription", "NO");
        specialGuardianshipOrderQuestions.put("uploadOrderFile", "NO");
        specialGuardianshipOrderQuestions.put("needSealing", "NO");
        specialGuardianshipOrderQuestions.put("epoExpiryDate", "NO");
        specialGuardianshipOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        specialGuardianshipOrderQuestions.put("cafcassJurisdictions", "NO");
        specialGuardianshipOrderQuestions.put("whichChildren", "YES");
        specialGuardianshipOrderQuestions.put("dischargeOfCareDetails", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        specialGuardianshipOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        specialGuardianshipOrderQuestions.put("closeCase", "NO");
        specialGuardianshipOrderQuestions.put("isFinalOrder", "YES");
        specialGuardianshipOrderQuestions.put("orderIsByConsent", "YES");
        specialGuardianshipOrderQuestions.put("whichOthers", "YES");
        specialGuardianshipOrderQuestions.put("appointedGuardian", "YES");
        specialGuardianshipOrderQuestions.put("parentResponsible", "NO");

        Map<String, String> appointmentOfChildrensGuardianQuestions = new HashMap<>(commonQuestions);
        appointmentOfChildrensGuardianQuestions.put("orderTitle", "NO");
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
        appointmentOfChildrensGuardianQuestions.put("dischargeOfCareDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        appointmentOfChildrensGuardianQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        appointmentOfChildrensGuardianQuestions.put("closeCase", "NO");
        appointmentOfChildrensGuardianQuestions.put("isFinalOrder", "NO");
        appointmentOfChildrensGuardianQuestions.put("orderIsByConsent", "NO");
        appointmentOfChildrensGuardianQuestions.put("appointedGuardian", "NO");
        appointmentOfChildrensGuardianQuestions.put("whichOthers", "YES");
        appointmentOfChildrensGuardianQuestions.put("parentResponsible", "NO");

        Map<String, String> childArrangementSpecificOrder = new HashMap<>(commonQuestions);
        childArrangementSpecificOrder.put("orderTitle", "NO");
        childArrangementSpecificOrder.put("approvalDate", "YES");
        childArrangementSpecificOrder.put("furtherDirections", "YES");
        childArrangementSpecificOrder.put("orderDetails", "YES");
        childArrangementSpecificOrder.put("approvalDateTime", "NO");
        childArrangementSpecificOrder.put("epoIncludePhrase", "NO");
        childArrangementSpecificOrder.put("uploadOrderFile", "NO");
        childArrangementSpecificOrder.put("needSealing", "NO");
        childArrangementSpecificOrder.put("epoChildrenDescription", "NO");
        childArrangementSpecificOrder.put("epoExpiryDate", "NO");
        childArrangementSpecificOrder.put("epoTypeAndPreventRemoval", "NO");
        childArrangementSpecificOrder.put("cafcassJurisdictions", "NO");
        childArrangementSpecificOrder.put("whichChildren", "YES");
        childArrangementSpecificOrder.put("dischargeOfCareDetails", "NO");
        childArrangementSpecificOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        childArrangementSpecificOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        childArrangementSpecificOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        childArrangementSpecificOrder.put("childArrangementSpecificIssueProhibitedSteps", "YES");
        childArrangementSpecificOrder.put("closeCase", "YES");
        childArrangementSpecificOrder.put("isFinalOrder", "YES");
        childArrangementSpecificOrder.put("orderIsByConsent", "YES");
        childArrangementSpecificOrder.put("appointedGuardian", "NO");
        childArrangementSpecificOrder.put("whichOthers", "YES");
        childArrangementSpecificOrder.put("parentResponsible", "NO");

        return Stream.of(
            Arguments.of(C32_CARE_ORDER, careOrderQuestions),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, dischargeOfCareQuestions),
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C35A_SUPERVISION_ORDER, supervisionOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, specialGuardianshipOrderQuestions),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER,
                childArrangementSpecificOrder),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, appointmentOfChildrensGuardianQuestions)
        );
    }

    @ParameterizedTest(name = "Show hide map for upload order {0}")
    @MethodSource("finalManualUploadOrders")
    void calculateManualUploadWithFinalOrderQuestion(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> finalManualUploadOrders() {
        return ORDERS_WITH_IS_FINAL_ORDER_QUESTION.stream()
            .map(order -> Arguments.of(order, Map.ofEntries(
                Map.entry("approver", "NO"),
                Map.entry("previewOrder", "YES"),
                Map.entry("orderTitle", "NO"),
                Map.entry("furtherDirections", "NO"),
                Map.entry("orderDetails", "NO"),
                Map.entry("whichChildren", "YES"),
                Map.entry("hearingDetails", "NO"),
                Map.entry("linkApplication", "NO"),
                Map.entry("approvalDate", "YES"),
                Map.entry("approvalDateTime", "NO"),
                Map.entry("dischargeOfCareDetails", "NO"),
                Map.entry("childArrangementSpecificIssueProhibitedSteps", "NO"),
                Map.entry("epoIncludePhrase", "NO"),
                Map.entry("epoExpiryDate", "NO"),
                Map.entry("isFinalOrder", "YES"),
                Map.entry("epoTypeAndPreventRemoval", "NO"),
                Map.entry("epoChildrenDescription", "NO"),
                Map.entry("manageOrdersExclusionRequirementDetails", "NO"),
                Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO"),
                Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
                Map.entry("cafcassJurisdictions", "NO"),
                Map.entry("needSealing", "YES"),
                Map.entry("uploadOrderFile", "YES"),
                Map.entry("closeCase", "YES"),
                Map.entry("orderIsByConsent", "NO"),
                Map.entry("appointedGuardian", "NO"),
                Map.entry("whichOthers", "YES"),
                Map.entry("parentResponsible", "NO")
                )
            ));
    }

    @ParameterizedTest(name = "Show hide map for upload order {0}")
    @MethodSource("nonFinalManualUploadOrders")
    void calculateManualUpload(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> nonFinalManualUploadOrders() {
        return Arrays.stream(Order.values())
            .filter(order -> OrderSourceType.MANUAL_UPLOAD == order.getSourceType())
            .filter(order -> !ORDERS_WITH_IS_FINAL_ORDER_QUESTION.contains(order))
            .map(order -> Arguments.of(order, Map.ofEntries(
                Map.entry("approver", "NO"),
                Map.entry("previewOrder", "YES"),
                Map.entry("orderTitle", "NO"),
                Map.entry("furtherDirections", "NO"),
                Map.entry("orderDetails", "NO"),
                Map.entry("whichChildren", "YES"),
                Map.entry("hearingDetails", "NO"),
                Map.entry("linkApplication", "NO"),
                Map.entry("approvalDate", "YES"),
                Map.entry("approvalDateTime", "NO"),
                Map.entry("dischargeOfCareDetails", "NO"),
                Map.entry("childArrangementSpecificIssueProhibitedSteps", "NO"),
                Map.entry("epoIncludePhrase", "NO"),
                Map.entry("epoExpiryDate", "NO"),
                Map.entry("isFinalOrder", "NO"),
                Map.entry("epoTypeAndPreventRemoval", "NO"),
                Map.entry("epoChildrenDescription", "NO"),
                Map.entry("manageOrdersExclusionRequirementDetails", "NO"),
                Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO"),
                Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
                Map.entry("cafcassJurisdictions", "NO"),
                Map.entry("needSealing", "YES"),
                Map.entry("uploadOrderFile", "YES"),
                Map.entry("closeCase", "YES"),
                Map.entry("orderIsByConsent", "NO"),
                Map.entry("appointedGuardian", "NO"),
                Map.entry("whichOthers", "YES"),
                Map.entry("parentResponsible", "NO")
                )
            ));
    }
}
