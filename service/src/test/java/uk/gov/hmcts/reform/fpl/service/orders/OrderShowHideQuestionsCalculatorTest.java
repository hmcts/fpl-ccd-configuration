package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;
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
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A81_PLACEMENT_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.AMENED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C26_SECURE_ACCOMMODATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C29_RECOVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32A_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C32B_DISCHARGE_OF_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34B_AUTHORITY_TO_REFUSE_CONTACT;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C35A_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C39_CHILD_ASSESSMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C42_FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C44A_LEAVE_TO_CHANGE_A_SURNAME;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C63A_DECLARATION_OF_PARENTAGE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.TRANSPARENCY_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.OTHER_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private static final Set<Order> MANUAL_ORDERS_WITH_IS_FINAL_ORDER_QUESTION = Set.of(
        C37_EDUCATION_SUPERVISION_ORDER,
        OTHER_ORDER
    );

    private final OrderShowHideQuestionsCalculator underTest = new OrderShowHideQuestionsCalculator();

    @Test
    void calculateAmendment() {
        assertThat(underTest.calculate(AMENED_ORDER)).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
            Map.entry("orderToAmend", "YES"),
            Map.entry("uploadAmendedOrder", "YES"),
            Map.entry("uploadOrderFile", "NO"),
            Map.entry("approvalDateTime", "NO"),
            Map.entry("approver", "NO"),
            Map.entry("previewOrder", "NO"),
            Map.entry("approvalDate", "NO"),
            Map.entry("whichChildren", "NO"),
            Map.entry("epoIncludePhrase", "NO"),
            Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
            Map.entry("hearingDetails", "NO"),
            Map.entry("dischargeOfCareDetails", "NO"),
            Map.entry("manageOrdersExclusionRequirementDetails", "NO"),
            Map.entry("furtherDirections", "NO"),
            Map.entry("orderDetails", "NO"),
            Map.entry("epoChildrenDescription", "NO"),
            Map.entry("epoExpiryDate", "NO"),
            Map.entry("needSealing", "NO"),
            Map.entry("linkApplication", "NO"),
            Map.entry("isFinalOrder", "NO"),
            Map.entry("cafcassJurisdictions", "NO"),
            Map.entry("translationRequirements", "NO"),
            Map.entry("epoTypeAndPreventRemoval", "NO"),
            Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO"),
            Map.entry("closeCase", "NO"),
            Map.entry("orderIsByConsent", "NO"),
            Map.entry("whichOthers", "NO"),
            Map.entry("appointedGuardian", "NO"),
            Map.entry("respondentsRefused", "NO"),
            Map.entry("refuseContactQuestions", "NO"),
            Map.entry("orderTitle", "NO"),
            Map.entry("childArrangementSpecificIssueProhibitedSteps", "NO"),
            Map.entry("reasonForSecureAccommodation", "NO"),
            Map.entry("orderJurisdiction", "NO"),
            Map.entry("childLegalRepresentation", "NO"),
            Map.entry("selectSingleChild", "NO"),
            Map.entry("parentResponsible", "NO"),
            Map.entry("childPlacementApplications", "NO"),
            Map.entry("childPlacementQuestions", "NO"),
            Map.entry("childPlacementQuestionsForBlankOrder", "NO"),
            Map.entry("manageOrdersChildAssessment", "NO"),
            Map.entry("manageOrdersEducationSupervision", "NO"),
            Map.entry("orderPlacedChildInCustody", "NO"),
            Map.entry("manageOrdersVaryOrExtendSupervisionOrder", "NO"),
            Map.entry("leaveToChangeChildSurname", "NO"),
            Map.entry("partyAllowedContactsAndConditions", "NO"),
            Map.entry("declarationOfParentage", "NO"),
            Map.entry("familyAssistanceOrder", "NO"),
            Map.entry("manageOrdersTransparencyOrder", "NO")
        ));
    }

    @ParameterizedTest(name = "Show hide map for {0}")
    @MethodSource("orderWithExpectedMap")
    void calculate(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    @ParameterizedTest(name = "Show hide map for upload order {0}")
    @MethodSource("finalManualUploadOrders")
    void calculateManualUploadWithFinalOrderQuestion(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    @ParameterizedTest(name = "Show hide map for upload order {0}")
    @MethodSource("nonFinalManualUploadOrders")
    void calculateManualUpload(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> orderWithExpectedMap() {
        Map<String, String> commonQuestions = Map.of(
            "hearingDetails", "YES",
            "linkApplication", "YES",
            "approver", "YES",
            "previewOrder", "YES",
            "orderToAmend", "NO",
            "translationRequirements", "NO",
            "uploadAmendedOrder", "NO"
        );

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
        careOrderQuestions.put("selectSingleChild", "NO");
        careOrderQuestions.put("reasonForSecureAccommodation", "NO");
        careOrderQuestions.put("childLegalRepresentation", "NO");
        careOrderQuestions.put("orderJurisdiction", "NO");
        careOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        careOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        careOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        careOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        careOrderQuestions.put("isFinalOrder", "NO");
        careOrderQuestions.put("closeCase", "YES");
        careOrderQuestions.put("whichOthers", "NO");
        careOrderQuestions.put("dischargeOfCareDetails", "NO");
        careOrderQuestions.put("orderIsByConsent", "NO");
        careOrderQuestions.put("appointedGuardian", "NO");
        careOrderQuestions.put("respondentsRefused", "NO");
        careOrderQuestions.put("refuseContactQuestions", "NO");
        careOrderQuestions.put("parentResponsible", "NO");
        careOrderQuestions.put("childPlacementApplications", "NO");
        careOrderQuestions.put("childPlacementQuestions", "NO");
        careOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        careOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        careOrderQuestions.put("orderPlacedChildInCustody", "NO");
        careOrderQuestions.put("manageOrdersChildAssessment", "NO");
        careOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        careOrderQuestions.put("leaveToChangeChildSurname", "NO");
        careOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        careOrderQuestions.put("declarationOfParentage", "NO");
        careOrderQuestions.put("familyAssistanceOrder", "NO");
        careOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        dischargeOfCareQuestions.put("selectSingleChild", "NO");
        dischargeOfCareQuestions.put("reasonForSecureAccommodation", "NO");
        dischargeOfCareQuestions.put("childLegalRepresentation", "NO");
        dischargeOfCareQuestions.put("orderJurisdiction", "NO");
        dischargeOfCareQuestions.put("dischargeOfCareDetails", "YES");
        dischargeOfCareQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        dischargeOfCareQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        dischargeOfCareQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        dischargeOfCareQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        dischargeOfCareQuestions.put("isFinalOrder", "YES");
        dischargeOfCareQuestions.put("whichOthers", "NO");
        dischargeOfCareQuestions.put("closeCase", "YES");
        dischargeOfCareQuestions.put("orderIsByConsent", "NO");
        dischargeOfCareQuestions.put("appointedGuardian", "NO");
        dischargeOfCareQuestions.put("respondentsRefused", "NO");
        dischargeOfCareQuestions.put("refuseContactQuestions", "NO");
        dischargeOfCareQuestions.put("parentResponsible", "NO");
        dischargeOfCareQuestions.put("childPlacementApplications", "NO");
        dischargeOfCareQuestions.put("childPlacementQuestions", "NO");
        dischargeOfCareQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        dischargeOfCareQuestions.put("manageOrdersEducationSupervision", "NO");
        dischargeOfCareQuestions.put("orderPlacedChildInCustody", "NO");
        dischargeOfCareQuestions.put("manageOrdersChildAssessment", "NO");
        dischargeOfCareQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        dischargeOfCareQuestions.put("leaveToChangeChildSurname", "NO");
        dischargeOfCareQuestions.put("partyAllowedContactsAndConditions", "NO");
        dischargeOfCareQuestions.put("declarationOfParentage", "NO");
        dischargeOfCareQuestions.put("familyAssistanceOrder", "NO");
        dischargeOfCareQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        epoQuestions.put("selectSingleChild", "NO");
        epoQuestions.put("reasonForSecureAccommodation", "NO");
        epoQuestions.put("childLegalRepresentation", "NO");
        epoQuestions.put("orderJurisdiction", "NO");
        epoQuestions.put("dischargeOfCareDetails", "NO");
        epoQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        epoQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        epoQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        epoQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        epoQuestions.put("closeCase", "YES");
        epoQuestions.put("whichOthers", "NO");
        epoQuestions.put("isFinalOrder", "YES");
        epoQuestions.put("orderIsByConsent", "NO");
        epoQuestions.put("appointedGuardian", "NO");
        epoQuestions.put("respondentsRefused", "NO");
        epoQuestions.put("refuseContactQuestions", "NO");
        epoQuestions.put("parentResponsible", "NO");
        epoQuestions.put("childPlacementApplications", "NO");
        epoQuestions.put("childPlacementQuestions", "NO");
        epoQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        epoQuestions.put("manageOrdersEducationSupervision", "NO");
        epoQuestions.put("orderPlacedChildInCustody", "NO");
        epoQuestions.put("manageOrdersChildAssessment", "NO");
        epoQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        epoQuestions.put("leaveToChangeChildSurname", "NO");
        epoQuestions.put("partyAllowedContactsAndConditions", "NO");
        epoQuestions.put("declarationOfParentage", "NO");
        epoQuestions.put("familyAssistanceOrder", "NO");
        epoQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        blankOrderQuestions.put("selectSingleChild", "NO");
        blankOrderQuestions.put("reasonForSecureAccommodation", "NO");
        blankOrderQuestions.put("childLegalRepresentation", "NO");
        blankOrderQuestions.put("orderJurisdiction", "NO");
        blankOrderQuestions.put("dischargeOfCareDetails", "NO");
        blankOrderQuestions.put("cafcassJurisdictions", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        blankOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        blankOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        blankOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        blankOrderQuestions.put("closeCase", "NO");
        blankOrderQuestions.put("whichOthers", "NO");
        blankOrderQuestions.put("isFinalOrder", "NO");
        blankOrderQuestions.put("orderIsByConsent", "NO");
        blankOrderQuestions.put("appointedGuardian", "NO");
        blankOrderQuestions.put("respondentsRefused", "NO");
        blankOrderQuestions.put("refuseContactQuestions", "NO");
        blankOrderQuestions.put("parentResponsible", "NO");
        blankOrderQuestions.put("childPlacementApplications", "NO");
        blankOrderQuestions.put("childPlacementQuestions", "NO");
        blankOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        blankOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        blankOrderQuestions.put("orderPlacedChildInCustody", "NO");
        blankOrderQuestions.put("manageOrdersChildAssessment", "NO");
        blankOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        blankOrderQuestions.put("leaveToChangeChildSurname", "NO");
        blankOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        blankOrderQuestions.put("declarationOfParentage", "NO");
        blankOrderQuestions.put("familyAssistanceOrder", "NO");
        blankOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> placementBlankOrderQuestions = new HashMap<>(commonQuestions);
        placementBlankOrderQuestions.put("orderTitle", "NO");
        placementBlankOrderQuestions.put("hearingDetails", "NO");
        placementBlankOrderQuestions.put("linkApplication", "NO");
        placementBlankOrderQuestions.put("approvalDate", "YES");
        placementBlankOrderQuestions.put("orderDetails", "NO");
        placementBlankOrderQuestions.put("approvalDateTime", "NO");
        placementBlankOrderQuestions.put("epoIncludePhrase", "NO");
        placementBlankOrderQuestions.put("uploadOrderFile", "NO");
        placementBlankOrderQuestions.put("needSealing", "NO");
        placementBlankOrderQuestions.put("epoChildrenDescription", "NO");
        placementBlankOrderQuestions.put("epoExpiryDate", "NO");
        placementBlankOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        placementBlankOrderQuestions.put("cafcassJurisdictions", "NO");
        placementBlankOrderQuestions.put("selectSingleChild", "NO");
        placementBlankOrderQuestions.put("reasonForSecureAccommodation", "NO");
        placementBlankOrderQuestions.put("childLegalRepresentation", "NO");
        placementBlankOrderQuestions.put("orderJurisdiction", "NO");
        placementBlankOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        placementBlankOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        placementBlankOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        placementBlankOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        placementBlankOrderQuestions.put("whichOthers", "NO");
        placementBlankOrderQuestions.put("dischargeOfCareDetails", "NO");
        placementBlankOrderQuestions.put("closeCase", "NO");
        placementBlankOrderQuestions.put("whichChildren", "NO");
        placementBlankOrderQuestions.put("orderIsByConsent", "NO");
        placementBlankOrderQuestions.put("furtherDirections", "NO");
        placementBlankOrderQuestions.put("isFinalOrder", "YES");
        placementBlankOrderQuestions.put("appointedGuardian", "NO");
        placementBlankOrderQuestions.put("respondentsRefused", "NO");
        placementBlankOrderQuestions.put("refuseContactQuestions", "NO");
        placementBlankOrderQuestions.put("parentResponsible", "NO");
        placementBlankOrderQuestions.put("childPlacementApplications", "YES");
        placementBlankOrderQuestions.put("childPlacementQuestions", "NO");
        placementBlankOrderQuestions.put("childPlacementQuestionsForBlankOrder", "YES");
        placementBlankOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        placementBlankOrderQuestions.put("orderPlacedChildInCustody", "NO");
        placementBlankOrderQuestions.put("manageOrdersChildAssessment", "NO");
        placementBlankOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        placementBlankOrderQuestions.put("leaveToChangeChildSurname", "NO");
        placementBlankOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        placementBlankOrderQuestions.put("declarationOfParentage", "NO");
        placementBlankOrderQuestions.put("familyAssistanceOrder", "NO");
        placementBlankOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        supervisionOrderQuestions.put("selectSingleChild", "NO");
        supervisionOrderQuestions.put("reasonForSecureAccommodation", "NO");
        supervisionOrderQuestions.put("childLegalRepresentation", "NO");
        supervisionOrderQuestions.put("orderJurisdiction", "NO");
        supervisionOrderQuestions.put("dischargeOfCareDetails", "NO");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        supervisionOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        supervisionOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        supervisionOrderQuestions.put("closeCase", "YES");
        supervisionOrderQuestions.put("whichOthers", "NO");
        supervisionOrderQuestions.put("isFinalOrder", "NO");
        supervisionOrderQuestions.put("orderIsByConsent", "NO");
        supervisionOrderQuestions.put("appointedGuardian", "NO");
        supervisionOrderQuestions.put("respondentsRefused", "NO");
        supervisionOrderQuestions.put("refuseContactQuestions", "NO");
        supervisionOrderQuestions.put("parentResponsible", "NO");
        supervisionOrderQuestions.put("childPlacementApplications", "NO");
        supervisionOrderQuestions.put("childPlacementQuestions", "NO");
        supervisionOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        supervisionOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        supervisionOrderQuestions.put("orderPlacedChildInCustody", "NO");
        supervisionOrderQuestions.put("manageOrdersChildAssessment", "NO");
        supervisionOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        supervisionOrderQuestions.put("leaveToChangeChildSurname", "NO");
        supervisionOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        supervisionOrderQuestions.put("declarationOfParentage", "NO");
        supervisionOrderQuestions.put("familyAssistanceOrder", "NO");
        supervisionOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        specialGuardianshipOrderQuestions.put("selectSingleChild", "NO");
        specialGuardianshipOrderQuestions.put("reasonForSecureAccommodation", "NO");
        specialGuardianshipOrderQuestions.put("childLegalRepresentation", "NO");
        specialGuardianshipOrderQuestions.put("orderJurisdiction", "NO");
        specialGuardianshipOrderQuestions.put("dischargeOfCareDetails", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        specialGuardianshipOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        specialGuardianshipOrderQuestions.put("closeCase", "YES");
        specialGuardianshipOrderQuestions.put("isFinalOrder", "YES");
        specialGuardianshipOrderQuestions.put("orderIsByConsent", "YES");
        specialGuardianshipOrderQuestions.put("whichOthers", "NO");
        specialGuardianshipOrderQuestions.put("appointedGuardian", "YES");
        specialGuardianshipOrderQuestions.put("respondentsRefused", "NO");
        specialGuardianshipOrderQuestions.put("refuseContactQuestions", "NO");
        specialGuardianshipOrderQuestions.put("parentResponsible", "NO");
        specialGuardianshipOrderQuestions.put("childPlacementApplications", "NO");
        specialGuardianshipOrderQuestions.put("childPlacementQuestions", "NO");
        specialGuardianshipOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        specialGuardianshipOrderQuestions.put("orderPlacedChildInCustody", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersChildAssessment", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        specialGuardianshipOrderQuestions.put("leaveToChangeChildSurname", "NO");
        specialGuardianshipOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        specialGuardianshipOrderQuestions.put("declarationOfParentage", "NO");
        specialGuardianshipOrderQuestions.put("familyAssistanceOrder", "NO");
        specialGuardianshipOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        appointmentOfChildrensGuardianQuestions.put("selectSingleChild", "NO");
        appointmentOfChildrensGuardianQuestions.put("reasonForSecureAccommodation", "NO");
        appointmentOfChildrensGuardianQuestions.put("childLegalRepresentation", "NO");
        appointmentOfChildrensGuardianQuestions.put("orderJurisdiction", "NO");
        appointmentOfChildrensGuardianQuestions.put("dischargeOfCareDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        appointmentOfChildrensGuardianQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        appointmentOfChildrensGuardianQuestions.put("closeCase", "NO");
        appointmentOfChildrensGuardianQuestions.put("isFinalOrder", "NO");
        appointmentOfChildrensGuardianQuestions.put("orderIsByConsent", "NO");
        appointmentOfChildrensGuardianQuestions.put("appointedGuardian", "NO");
        appointmentOfChildrensGuardianQuestions.put("respondentsRefused", "NO");
        appointmentOfChildrensGuardianQuestions.put("refuseContactQuestions", "NO");
        appointmentOfChildrensGuardianQuestions.put("whichOthers", "NO");
        appointmentOfChildrensGuardianQuestions.put("parentResponsible", "NO");
        appointmentOfChildrensGuardianQuestions.put("childPlacementApplications", "NO");
        appointmentOfChildrensGuardianQuestions.put("childPlacementQuestions", "NO");
        appointmentOfChildrensGuardianQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersEducationSupervision", "NO");
        appointmentOfChildrensGuardianQuestions.put("orderPlacedChildInCustody", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersChildAssessment", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        appointmentOfChildrensGuardianQuestions.put("leaveToChangeChildSurname", "NO");
        appointmentOfChildrensGuardianQuestions.put("partyAllowedContactsAndConditions", "NO");
        appointmentOfChildrensGuardianQuestions.put("declarationOfParentage", "NO");
        appointmentOfChildrensGuardianQuestions.put("familyAssistanceOrder", "NO");
        appointmentOfChildrensGuardianQuestions.put("manageOrdersTransparencyOrder", "NO");

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
        childArrangementSpecificOrder.put("selectSingleChild", "NO");
        childArrangementSpecificOrder.put("reasonForSecureAccommodation", "NO");
        childArrangementSpecificOrder.put("childLegalRepresentation", "NO");
        childArrangementSpecificOrder.put("orderJurisdiction", "NO");
        childArrangementSpecificOrder.put("dischargeOfCareDetails", "NO");
        childArrangementSpecificOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        childArrangementSpecificOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        childArrangementSpecificOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        childArrangementSpecificOrder.put("childArrangementSpecificIssueProhibitedSteps", "YES");
        childArrangementSpecificOrder.put("closeCase", "YES");
        childArrangementSpecificOrder.put("isFinalOrder", "YES");
        childArrangementSpecificOrder.put("orderIsByConsent", "YES");
        childArrangementSpecificOrder.put("appointedGuardian", "NO");
        childArrangementSpecificOrder.put("respondentsRefused", "NO");
        childArrangementSpecificOrder.put("refuseContactQuestions", "NO");
        childArrangementSpecificOrder.put("whichOthers", "NO");
        childArrangementSpecificOrder.put("parentResponsible", "NO");
        childArrangementSpecificOrder.put("childPlacementApplications", "NO");
        childArrangementSpecificOrder.put("childPlacementQuestions", "NO");
        childArrangementSpecificOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        childArrangementSpecificOrder.put("manageOrdersEducationSupervision", "NO");
        childArrangementSpecificOrder.put("orderPlacedChildInCustody", "NO");
        childArrangementSpecificOrder.put("manageOrdersChildAssessment", "NO");
        childArrangementSpecificOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        childArrangementSpecificOrder.put("leaveToChangeChildSurname", "NO");
        childArrangementSpecificOrder.put("partyAllowedContactsAndConditions", "NO");
        childArrangementSpecificOrder.put("declarationOfParentage", "NO");
        childArrangementSpecificOrder.put("familyAssistanceOrder", "NO");
        childArrangementSpecificOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> refusedContactOrderQuestions = new HashMap<>(commonQuestions);
        refusedContactOrderQuestions.put("hearingDetails", "NO");
        refusedContactOrderQuestions.put("linkApplication", "NO");
        refusedContactOrderQuestions.put("furtherDirections", "NO");
        refusedContactOrderQuestions.put("orderTitle", "NO");
        refusedContactOrderQuestions.put("approvalDate", "YES");
        refusedContactOrderQuestions.put("orderDetails", "NO");
        refusedContactOrderQuestions.put("approvalDateTime", "NO");
        refusedContactOrderQuestions.put("epoIncludePhrase", "NO");
        refusedContactOrderQuestions.put("uploadOrderFile", "NO");
        refusedContactOrderQuestions.put("needSealing", "NO");
        refusedContactOrderQuestions.put("epoChildrenDescription", "NO");
        refusedContactOrderQuestions.put("epoExpiryDate", "NO");
        refusedContactOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        refusedContactOrderQuestions.put("cafcassJurisdictions", "NO");
        refusedContactOrderQuestions.put("whichChildren", "YES");
        refusedContactOrderQuestions.put("selectSingleChild", "NO");
        refusedContactOrderQuestions.put("reasonForSecureAccommodation", "NO");
        refusedContactOrderQuestions.put("childLegalRepresentation", "NO");
        refusedContactOrderQuestions.put("orderJurisdiction", "NO");
        refusedContactOrderQuestions.put("dischargeOfCareDetails", "NO");
        refusedContactOrderQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        refusedContactOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        refusedContactOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        refusedContactOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        refusedContactOrderQuestions.put("closeCase", "YES");
        refusedContactOrderQuestions.put("isFinalOrder", "YES");
        refusedContactOrderQuestions.put("orderIsByConsent", "YES");
        refusedContactOrderQuestions.put("appointedGuardian", "NO");
        refusedContactOrderQuestions.put("respondentsRefused", "YES");
        refusedContactOrderQuestions.put("refuseContactQuestions", "YES");
        refusedContactOrderQuestions.put("whichOthers", "NO");
        refusedContactOrderQuestions.put("parentResponsible", "NO");
        refusedContactOrderQuestions.put("childPlacementApplications", "NO");
        refusedContactOrderQuestions.put("childPlacementQuestions", "NO");
        refusedContactOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        refusedContactOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        refusedContactOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        refusedContactOrderQuestions.put("orderPlacedChildInCustody","NO");
        refusedContactOrderQuestions.put("manageOrdersChildAssessment", "NO");
        refusedContactOrderQuestions.put("leaveToChangeChildSurname", "NO");
        refusedContactOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        refusedContactOrderQuestions.put("declarationOfParentage", "NO");
        refusedContactOrderQuestions.put("familyAssistanceOrder", "NO");
        refusedContactOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> secureAccommodationOrderQuestions = new HashMap<>(commonQuestions);
        secureAccommodationOrderQuestions.put("orderTitle", "NO");
        secureAccommodationOrderQuestions.put("approvalDate", "NO");
        secureAccommodationOrderQuestions.put("furtherDirections", "YES");
        secureAccommodationOrderQuestions.put("orderDetails", "NO");
        secureAccommodationOrderQuestions.put("approvalDateTime", "YES");
        secureAccommodationOrderQuestions.put("epoIncludePhrase", "NO");
        secureAccommodationOrderQuestions.put("uploadOrderFile", "NO");
        secureAccommodationOrderQuestions.put("needSealing", "NO");
        secureAccommodationOrderQuestions.put("epoChildrenDescription", "NO");
        secureAccommodationOrderQuestions.put("epoExpiryDate", "NO");
        secureAccommodationOrderQuestions.put("epoTypeAndPreventRemoval", "NO");
        secureAccommodationOrderQuestions.put("cafcassJurisdictions", "NO");
        secureAccommodationOrderQuestions.put("whichChildren", "NO");
        secureAccommodationOrderQuestions.put("selectSingleChild", "YES");
        secureAccommodationOrderQuestions.put("reasonForSecureAccommodation", "YES");
        secureAccommodationOrderQuestions.put("childLegalRepresentation", "YES");
        secureAccommodationOrderQuestions.put("orderIsByConsent", "YES");
        secureAccommodationOrderQuestions.put("orderJurisdiction", "YES");
        secureAccommodationOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        secureAccommodationOrderQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        secureAccommodationOrderQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        secureAccommodationOrderQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        secureAccommodationOrderQuestions.put("dischargeOfCareDetails", "NO");
        secureAccommodationOrderQuestions.put("closeCase", "YES");
        secureAccommodationOrderQuestions.put("isFinalOrder", "YES");
        secureAccommodationOrderQuestions.put("appointedGuardian", "NO");
        secureAccommodationOrderQuestions.put("respondentsRefused", "NO");
        secureAccommodationOrderQuestions.put("refuseContactQuestions", "NO");
        secureAccommodationOrderQuestions.put("whichOthers", "NO");
        secureAccommodationOrderQuestions.put("parentResponsible", "NO");
        secureAccommodationOrderQuestions.put("childPlacementApplications", "NO");
        secureAccommodationOrderQuestions.put("childPlacementQuestions", "NO");
        secureAccommodationOrderQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        secureAccommodationOrderQuestions.put("manageOrdersEducationSupervision", "NO");
        secureAccommodationOrderQuestions.put("orderPlacedChildInCustody", "NO");
        secureAccommodationOrderQuestions.put("manageOrdersChildAssessment", "NO");
        secureAccommodationOrderQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        secureAccommodationOrderQuestions.put("leaveToChangeChildSurname", "NO");
        secureAccommodationOrderQuestions.put("partyAllowedContactsAndConditions", "NO");
        secureAccommodationOrderQuestions.put("declarationOfParentage", "NO");
        secureAccommodationOrderQuestions.put("familyAssistanceOrder", "NO");
        secureAccommodationOrderQuestions.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> contactWithAChildOrderInCareOrder = new HashMap<>(commonQuestions);
        contactWithAChildOrderInCareOrder.put("orderTitle", "NO");
        contactWithAChildOrderInCareOrder.put("hearingDetails", "YES");
        contactWithAChildOrderInCareOrder.put("linkApplication", "YES");
        contactWithAChildOrderInCareOrder.put("approvalDate", "YES");
        contactWithAChildOrderInCareOrder.put("orderDetails", "NO");
        contactWithAChildOrderInCareOrder.put("approvalDateTime", "NO");
        contactWithAChildOrderInCareOrder.put("epoIncludePhrase", "NO");
        contactWithAChildOrderInCareOrder.put("uploadOrderFile", "NO");
        contactWithAChildOrderInCareOrder.put("needSealing", "NO");
        contactWithAChildOrderInCareOrder.put("epoChildrenDescription", "NO");
        contactWithAChildOrderInCareOrder.put("epoExpiryDate", "NO");
        contactWithAChildOrderInCareOrder.put("epoTypeAndPreventRemoval", "NO");
        contactWithAChildOrderInCareOrder.put("cafcassJurisdictions", "NO");
        contactWithAChildOrderInCareOrder.put("selectSingleChild", "NO");
        contactWithAChildOrderInCareOrder.put("reasonForSecureAccommodation", "NO");
        contactWithAChildOrderInCareOrder.put("childLegalRepresentation", "NO");
        contactWithAChildOrderInCareOrder.put("orderJurisdiction", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        contactWithAChildOrderInCareOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        contactWithAChildOrderInCareOrder.put("whichOthers", "NO");
        contactWithAChildOrderInCareOrder.put("dischargeOfCareDetails", "NO");
        contactWithAChildOrderInCareOrder.put("closeCase", "YES");
        contactWithAChildOrderInCareOrder.put("whichChildren", "YES");
        contactWithAChildOrderInCareOrder.put("orderIsByConsent", "YES");
        contactWithAChildOrderInCareOrder.put("furtherDirections", "NO");
        contactWithAChildOrderInCareOrder.put("isFinalOrder", "YES");
        contactWithAChildOrderInCareOrder.put("appointedGuardian", "NO");
        contactWithAChildOrderInCareOrder.put("parentResponsible", "NO");
        contactWithAChildOrderInCareOrder.put("respondentsRefused", "NO");
        contactWithAChildOrderInCareOrder.put("refuseContactQuestions", "NO");
        contactWithAChildOrderInCareOrder.put("childPlacementApplications", "NO");
        contactWithAChildOrderInCareOrder.put("childPlacementQuestions", "NO");
        contactWithAChildOrderInCareOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        contactWithAChildOrderInCareOrder.put("orderPlacedChildInCustody", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersEducationSupervision", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersChildAssessment", "NO");
        contactWithAChildOrderInCareOrder.put("leaveToChangeChildSurname", "NO");
        contactWithAChildOrderInCareOrder.put("partyAllowedContactsAndConditions", "YES");
        contactWithAChildOrderInCareOrder.put("declarationOfParentage", "NO");
        contactWithAChildOrderInCareOrder.put("familyAssistanceOrder", "NO");
        contactWithAChildOrderInCareOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> parentalResponsibilityOrder = new HashMap<>(commonQuestions);
        parentalResponsibilityOrder.put("orderTitle", "NO");
        parentalResponsibilityOrder.put("approvalDate", "NO");
        parentalResponsibilityOrder.put("orderDetails", "NO");
        parentalResponsibilityOrder.put("approvalDateTime", "YES");
        parentalResponsibilityOrder.put("epoIncludePhrase", "NO");
        parentalResponsibilityOrder.put("uploadOrderFile", "NO");
        parentalResponsibilityOrder.put("needSealing", "NO");
        parentalResponsibilityOrder.put("epoChildrenDescription", "NO");
        parentalResponsibilityOrder.put("epoExpiryDate", "NO");
        parentalResponsibilityOrder.put("epoTypeAndPreventRemoval", "NO");
        parentalResponsibilityOrder.put("cafcassJurisdictions", "NO");
        parentalResponsibilityOrder.put("selectSingleChild", "NO");
        parentalResponsibilityOrder.put("reasonForSecureAccommodation", "NO");
        parentalResponsibilityOrder.put("childLegalRepresentation", "NO");
        parentalResponsibilityOrder.put("orderJurisdiction", "NO");
        parentalResponsibilityOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        parentalResponsibilityOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        parentalResponsibilityOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        parentalResponsibilityOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        parentalResponsibilityOrder.put("whichOthers", "NO");
        parentalResponsibilityOrder.put("dischargeOfCareDetails", "NO");
        parentalResponsibilityOrder.put("closeCase", "YES");
        parentalResponsibilityOrder.put("whichChildren", "YES");
        parentalResponsibilityOrder.put("orderIsByConsent", "YES");
        parentalResponsibilityOrder.put("furtherDirections", "YES");
        parentalResponsibilityOrder.put("isFinalOrder", "YES");
        parentalResponsibilityOrder.put("appointedGuardian", "NO");
        parentalResponsibilityOrder.put("respondentsRefused", "NO");
        parentalResponsibilityOrder.put("refuseContactQuestions", "NO");
        parentalResponsibilityOrder.put("parentResponsible", "YES");
        parentalResponsibilityOrder.put("childPlacementApplications", "NO");
        parentalResponsibilityOrder.put("childPlacementQuestions", "NO");
        parentalResponsibilityOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        parentalResponsibilityOrder.put("manageOrdersEducationSupervision", "NO");
        parentalResponsibilityOrder.put("orderPlacedChildInCustody", "NO");
        parentalResponsibilityOrder.put("manageOrdersChildAssessment", "NO");
        parentalResponsibilityOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        parentalResponsibilityOrder.put("leaveToChangeChildSurname", "NO");
        parentalResponsibilityOrder.put("partyAllowedContactsAndConditions", "NO");
        parentalResponsibilityOrder.put("declarationOfParentage", "NO");
        parentalResponsibilityOrder.put("familyAssistanceOrder", "NO");
        parentalResponsibilityOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> recoveryOfChildQuestions = new HashMap<>(commonQuestions);
        recoveryOfChildQuestions.put("orderTitle", "NO");
        recoveryOfChildQuestions.put("approvalDate", "YES");
        recoveryOfChildQuestions.put("orderDetails", "NO");
        recoveryOfChildQuestions.put("approvalDateTime", "NO");
        recoveryOfChildQuestions.put("epoIncludePhrase", "NO");
        recoveryOfChildQuestions.put("uploadOrderFile", "NO");
        recoveryOfChildQuestions.put("needSealing", "NO");
        recoveryOfChildQuestions.put("epoChildrenDescription", "NO");
        recoveryOfChildQuestions.put("epoExpiryDate", "NO");
        recoveryOfChildQuestions.put("epoTypeAndPreventRemoval", "NO");
        recoveryOfChildQuestions.put("cafcassJurisdictions", "NO");
        recoveryOfChildQuestions.put("selectSingleChild", "NO");
        recoveryOfChildQuestions.put("reasonForSecureAccommodation", "NO");
        recoveryOfChildQuestions.put("childLegalRepresentation", "NO");
        recoveryOfChildQuestions.put("orderJurisdiction", "NO");
        recoveryOfChildQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        recoveryOfChildQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        recoveryOfChildQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        recoveryOfChildQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        recoveryOfChildQuestions.put("whichOthers", "NO");
        recoveryOfChildQuestions.put("dischargeOfCareDetails", "NO");
        recoveryOfChildQuestions.put("closeCase", "YES");
        recoveryOfChildQuestions.put("whichChildren", "YES");
        recoveryOfChildQuestions.put("orderIsByConsent", "NO");
        recoveryOfChildQuestions.put("furtherDirections", "YES");
        recoveryOfChildQuestions.put("isFinalOrder", "YES");
        recoveryOfChildQuestions.put("appointedGuardian", "NO");
        recoveryOfChildQuestions.put("refuseContactQuestions", "NO");
        recoveryOfChildQuestions.put("respondentsRefused", "NO");
        recoveryOfChildQuestions.put("parentResponsible", "NO");
        recoveryOfChildQuestions.put("childPlacementApplications", "NO");
        recoveryOfChildQuestions.put("childPlacementQuestions", "NO");
        recoveryOfChildQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        recoveryOfChildQuestions.put("orderPlacedChildInCustody", "YES");
        recoveryOfChildQuestions.put("manageOrdersChildAssessment", "NO");
        recoveryOfChildQuestions.put("manageOrdersEducationSupervision", "NO");
        recoveryOfChildQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        recoveryOfChildQuestions.put("leaveToChangeChildSurname", "NO");
        recoveryOfChildQuestions.put("partyAllowedContactsAndConditions", "NO");
        recoveryOfChildQuestions.put("declarationOfParentage", "NO");
        recoveryOfChildQuestions.put("familyAssistanceOrder", "NO");
        recoveryOfChildQuestions.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> placementOrder = new HashMap<>(commonQuestions);
        placementOrder.put("orderTitle", "NO");
        placementOrder.put("hearingDetails", "NO");
        placementOrder.put("linkApplication", "NO");
        placementOrder.put("approvalDate", "YES");
        placementOrder.put("orderDetails", "NO");
        placementOrder.put("approvalDateTime", "NO");
        placementOrder.put("epoIncludePhrase", "NO");
        placementOrder.put("uploadOrderFile", "NO");
        placementOrder.put("needSealing", "NO");
        placementOrder.put("epoChildrenDescription", "NO");
        placementOrder.put("epoExpiryDate", "NO");
        placementOrder.put("epoTypeAndPreventRemoval", "NO");
        placementOrder.put("cafcassJurisdictions", "NO");
        placementOrder.put("selectSingleChild", "NO");
        placementOrder.put("reasonForSecureAccommodation", "NO");
        placementOrder.put("childLegalRepresentation", "NO");
        placementOrder.put("orderJurisdiction", "NO");
        placementOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        placementOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        placementOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        placementOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        placementOrder.put("whichOthers", "NO");
        placementOrder.put("dischargeOfCareDetails", "NO");
        placementOrder.put("closeCase", "YES");
        placementOrder.put("whichChildren", "NO");
        placementOrder.put("orderIsByConsent", "NO");
        placementOrder.put("furtherDirections", "NO");
        placementOrder.put("isFinalOrder", "YES");
        placementOrder.put("appointedGuardian", "NO");
        placementOrder.put("respondentsRefused", "NO");
        placementOrder.put("refuseContactQuestions", "NO");
        placementOrder.put("parentResponsible", "NO");
        placementOrder.put("childPlacementApplications", "YES");
        placementOrder.put("childPlacementQuestions", "YES");
        placementOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        placementOrder.put("manageOrdersEducationSupervision", "NO");
        placementOrder.put("orderPlacedChildInCustody", "NO");
        placementOrder.put("manageOrdersChildAssessment", "NO");
        placementOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        placementOrder.put("leaveToChangeChildSurname", "NO");
        placementOrder.put("partyAllowedContactsAndConditions", "NO");
        placementOrder.put("declarationOfParentage", "NO");
        placementOrder.put("familyAssistanceOrder", "NO");
        placementOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> childAssessmentOrder = new HashMap<>(commonQuestions);
        childAssessmentOrder.put("orderTitle", "NO");
        childAssessmentOrder.put("hearingDetails", "NO");
        childAssessmentOrder.put("linkApplication", "NO");
        childAssessmentOrder.put("approvalDate", "YES");
        childAssessmentOrder.put("orderDetails", "NO");
        childAssessmentOrder.put("approvalDateTime", "NO");
        childAssessmentOrder.put("epoIncludePhrase", "NO");
        childAssessmentOrder.put("uploadOrderFile", "NO");
        childAssessmentOrder.put("needSealing", "NO");
        childAssessmentOrder.put("epoChildrenDescription", "NO");
        childAssessmentOrder.put("epoExpiryDate", "NO");
        childAssessmentOrder.put("epoTypeAndPreventRemoval", "NO");
        childAssessmentOrder.put("cafcassJurisdictions", "NO");
        childAssessmentOrder.put("selectSingleChild", "YES");
        childAssessmentOrder.put("reasonForSecureAccommodation", "NO");
        childAssessmentOrder.put("childLegalRepresentation", "NO");
        childAssessmentOrder.put("orderJurisdiction", "NO");
        childAssessmentOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        childAssessmentOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        childAssessmentOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        childAssessmentOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        childAssessmentOrder.put("whichOthers", "NO");
        childAssessmentOrder.put("dischargeOfCareDetails", "NO");
        childAssessmentOrder.put("closeCase", "YES");
        childAssessmentOrder.put("whichChildren", "NO");
        childAssessmentOrder.put("orderIsByConsent", "YES");
        childAssessmentOrder.put("furtherDirections", "NO");
        childAssessmentOrder.put("isFinalOrder", "YES");
        childAssessmentOrder.put("appointedGuardian", "NO");
        childAssessmentOrder.put("parentResponsible", "NO");
        childAssessmentOrder.put("childPlacementApplications", "NO");
        childAssessmentOrder.put("childPlacementQuestions", "NO");
        childAssessmentOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        childAssessmentOrder.put("orderPlacedChildInCustody", "NO");
        childAssessmentOrder.put("manageOrdersChildAssessment", "YES");
        childAssessmentOrder.put("refuseContactQuestions", "NO");
        childAssessmentOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        childAssessmentOrder.put("manageOrdersEducationSupervision", "NO");
        childAssessmentOrder.put("respondentsRefused", "NO");
        childAssessmentOrder.put("leaveToChangeChildSurname", "NO");
        childAssessmentOrder.put("partyAllowedContactsAndConditions", "NO");
        childAssessmentOrder.put("declarationOfParentage", "NO");
        childAssessmentOrder.put("familyAssistanceOrder", "NO");
        childAssessmentOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> supervisionEducationOrder = new HashMap<>(Map.of(
            "hearingDetails", "NO",
            "linkApplication", "NO",
            "approver", "YES",
            "previewOrder", "YES",
            "orderToAmend", "NO",
            "translationRequirements", "NO",
            "uploadAmendedOrder", "NO"
        ));
        supervisionEducationOrder.put("orderTitle", "NO");
        supervisionEducationOrder.put("hearingDetails", "NO");
        supervisionEducationOrder.put("linkApplication", "NO");
        supervisionEducationOrder.put("approvalDate", "YES");
        supervisionEducationOrder.put("orderDetails", "NO");
        supervisionEducationOrder.put("approvalDateTime", "NO");
        supervisionEducationOrder.put("epoIncludePhrase", "NO");
        supervisionEducationOrder.put("uploadOrderFile", "NO");
        supervisionEducationOrder.put("needSealing", "NO");
        supervisionEducationOrder.put("epoChildrenDescription", "NO");
        supervisionEducationOrder.put("epoExpiryDate", "NO");
        supervisionEducationOrder.put("epoTypeAndPreventRemoval", "NO");
        supervisionEducationOrder.put("cafcassJurisdictions", "NO");
        supervisionEducationOrder.put("selectSingleChild", "NO");
        supervisionEducationOrder.put("reasonForSecureAccommodation", "NO");
        supervisionEducationOrder.put("childLegalRepresentation", "NO");
        supervisionEducationOrder.put("orderJurisdiction", "NO");
        supervisionEducationOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        supervisionEducationOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        supervisionEducationOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        supervisionEducationOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        supervisionEducationOrder.put("whichOthers", "NO");
        supervisionEducationOrder.put("dischargeOfCareDetails", "NO");
        supervisionEducationOrder.put("closeCase", "YES");
        supervisionEducationOrder.put("whichChildren", "YES");
        supervisionEducationOrder.put("orderIsByConsent", "YES");
        supervisionEducationOrder.put("furtherDirections", "NO");
        supervisionEducationOrder.put("isFinalOrder", "YES");
        supervisionEducationOrder.put("appointedGuardian", "NO");
        supervisionEducationOrder.put("parentResponsible", "NO");
        supervisionEducationOrder.put("respondentsRefused", "NO");
        supervisionEducationOrder.put("refuseContactQuestions", "NO");
        supervisionEducationOrder.put("childPlacementApplications", "NO");
        supervisionEducationOrder.put("childPlacementQuestions", "NO");
        supervisionEducationOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        supervisionEducationOrder.put("orderPlacedChildInCustody", "NO");
        supervisionEducationOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        supervisionEducationOrder.put("manageOrdersEducationSupervision", "YES");
        supervisionEducationOrder.put("manageOrdersChildAssessment", "NO");
        supervisionEducationOrder.put("leaveToChangeChildSurname", "NO");
        supervisionEducationOrder.put("partyAllowedContactsAndConditions", "NO");
        supervisionEducationOrder.put("declarationOfParentage", "NO");
        supervisionEducationOrder.put("familyAssistanceOrder", "NO");
        supervisionEducationOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> varyOrExtendSupervisionOrder = new HashMap<>(commonQuestions);
        varyOrExtendSupervisionOrder.put("orderTitle", "NO");
        varyOrExtendSupervisionOrder.put("hearingDetails", "NO");
        varyOrExtendSupervisionOrder.put("linkApplication", "NO");
        varyOrExtendSupervisionOrder.put("approvalDate", "YES");
        varyOrExtendSupervisionOrder.put("orderDetails", "NO");
        varyOrExtendSupervisionOrder.put("approvalDateTime", "NO");
        varyOrExtendSupervisionOrder.put("epoIncludePhrase", "NO");
        varyOrExtendSupervisionOrder.put("uploadOrderFile", "NO");
        varyOrExtendSupervisionOrder.put("needSealing", "NO");
        varyOrExtendSupervisionOrder.put("epoChildrenDescription", "NO");
        varyOrExtendSupervisionOrder.put("epoExpiryDate", "NO");
        varyOrExtendSupervisionOrder.put("epoTypeAndPreventRemoval", "NO");
        varyOrExtendSupervisionOrder.put("cafcassJurisdictions", "NO");
        varyOrExtendSupervisionOrder.put("selectSingleChild", "NO");
        varyOrExtendSupervisionOrder.put("reasonForSecureAccommodation", "NO");
        varyOrExtendSupervisionOrder.put("childLegalRepresentation", "NO");
        varyOrExtendSupervisionOrder.put("orderJurisdiction", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        varyOrExtendSupervisionOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        varyOrExtendSupervisionOrder.put("whichOthers", "NO");
        varyOrExtendSupervisionOrder.put("dischargeOfCareDetails", "NO");
        varyOrExtendSupervisionOrder.put("closeCase", "NO");
        varyOrExtendSupervisionOrder.put("whichChildren", "YES");
        varyOrExtendSupervisionOrder.put("orderIsByConsent", "YES");
        varyOrExtendSupervisionOrder.put("furtherDirections", "NO");
        varyOrExtendSupervisionOrder.put("isFinalOrder", "YES");
        varyOrExtendSupervisionOrder.put("appointedGuardian", "NO");
        varyOrExtendSupervisionOrder.put("parentResponsible", "NO");
        varyOrExtendSupervisionOrder.put("respondentsRefused", "NO");
        varyOrExtendSupervisionOrder.put("refuseContactQuestions", "NO");
        varyOrExtendSupervisionOrder.put("childPlacementApplications", "NO");
        varyOrExtendSupervisionOrder.put("childPlacementQuestions", "NO");
        varyOrExtendSupervisionOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        varyOrExtendSupervisionOrder.put("orderPlacedChildInCustody", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersEducationSupervision", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "YES");
        varyOrExtendSupervisionOrder.put("manageOrdersChildAssessment", "NO");
        varyOrExtendSupervisionOrder.put("leaveToChangeChildSurname", "NO");
        varyOrExtendSupervisionOrder.put("partyAllowedContactsAndConditions", "NO");
        varyOrExtendSupervisionOrder.put("declarationOfParentage", "NO");
        varyOrExtendSupervisionOrder.put("familyAssistanceOrder", "NO");
        varyOrExtendSupervisionOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> leaveToChangeChildSurname = new HashMap<>(commonQuestions);
        leaveToChangeChildSurname.put("orderTitle", "NO");
        leaveToChangeChildSurname.put("hearingDetails", "NO");
        leaveToChangeChildSurname.put("linkApplication", "NO");
        leaveToChangeChildSurname.put("approvalDate", "YES");
        leaveToChangeChildSurname.put("orderDetails", "NO");
        leaveToChangeChildSurname.put("approvalDateTime", "NO");
        leaveToChangeChildSurname.put("epoIncludePhrase", "NO");
        leaveToChangeChildSurname.put("uploadOrderFile", "NO");
        leaveToChangeChildSurname.put("needSealing", "NO");
        leaveToChangeChildSurname.put("epoChildrenDescription", "NO");
        leaveToChangeChildSurname.put("epoExpiryDate", "NO");
        leaveToChangeChildSurname.put("epoTypeAndPreventRemoval", "NO");
        leaveToChangeChildSurname.put("cafcassJurisdictions", "NO");
        leaveToChangeChildSurname.put("selectSingleChild", "NO");
        leaveToChangeChildSurname.put("reasonForSecureAccommodation", "NO");
        leaveToChangeChildSurname.put("childLegalRepresentation", "NO");
        leaveToChangeChildSurname.put("orderJurisdiction", "NO");
        leaveToChangeChildSurname.put("manageOrdersExpiryDateWithMonth", "NO");
        leaveToChangeChildSurname.put("manageOrdersExclusionRequirementDetails", "NO");
        leaveToChangeChildSurname.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        leaveToChangeChildSurname.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        leaveToChangeChildSurname.put("whichOthers", "NO");
        leaveToChangeChildSurname.put("dischargeOfCareDetails", "NO");
        leaveToChangeChildSurname.put("closeCase", "NO");
        leaveToChangeChildSurname.put("whichChildren", "YES");
        leaveToChangeChildSurname.put("orderIsByConsent", "YES");
        leaveToChangeChildSurname.put("furtherDirections", "NO");
        leaveToChangeChildSurname.put("isFinalOrder", "YES");
        leaveToChangeChildSurname.put("appointedGuardian", "NO");
        leaveToChangeChildSurname.put("parentResponsible", "NO");
        leaveToChangeChildSurname.put("respondentsRefused", "NO");
        leaveToChangeChildSurname.put("refuseContactQuestions", "NO");
        leaveToChangeChildSurname.put("childPlacementApplications", "NO");
        leaveToChangeChildSurname.put("childPlacementQuestions", "NO");
        leaveToChangeChildSurname.put("childPlacementQuestionsForBlankOrder", "NO");
        leaveToChangeChildSurname.put("orderPlacedChildInCustody", "NO");
        leaveToChangeChildSurname.put("manageOrdersEducationSupervision", "NO");
        leaveToChangeChildSurname.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        leaveToChangeChildSurname.put("manageOrdersChildAssessment", "NO");
        leaveToChangeChildSurname.put("leaveToChangeChildSurname", "YES");
        leaveToChangeChildSurname.put("partyAllowedContactsAndConditions", "NO");
        leaveToChangeChildSurname.put("declarationOfParentage", "NO");
        leaveToChangeChildSurname.put("familyAssistanceOrder", "NO");
        leaveToChangeChildSurname.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> declarationOfParentage = new HashMap<>(commonQuestions);
        declarationOfParentage.put("orderTitle", "NO");
        declarationOfParentage.put("hearingDetails", "YES");
        declarationOfParentage.put("linkApplication", "YES");
        declarationOfParentage.put("approvalDate", "YES");
        declarationOfParentage.put("orderDetails", "NO");
        declarationOfParentage.put("approvalDateTime", "NO");
        declarationOfParentage.put("epoIncludePhrase", "NO");
        declarationOfParentage.put("uploadOrderFile", "NO");
        declarationOfParentage.put("needSealing", "NO");
        declarationOfParentage.put("epoChildrenDescription", "NO");
        declarationOfParentage.put("epoExpiryDate", "NO");
        declarationOfParentage.put("epoTypeAndPreventRemoval", "NO");
        declarationOfParentage.put("cafcassJurisdictions", "NO");
        declarationOfParentage.put("selectSingleChild", "YES");
        declarationOfParentage.put("reasonForSecureAccommodation", "NO");
        declarationOfParentage.put("childLegalRepresentation", "NO");
        declarationOfParentage.put("orderJurisdiction", "NO");
        declarationOfParentage.put("manageOrdersExpiryDateWithMonth", "NO");
        declarationOfParentage.put("manageOrdersExclusionRequirementDetails", "NO");
        declarationOfParentage.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        declarationOfParentage.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        declarationOfParentage.put("whichOthers", "NO");
        declarationOfParentage.put("dischargeOfCareDetails", "NO");
        declarationOfParentage.put("closeCase", "YES");
        declarationOfParentage.put("whichChildren", "NO");
        declarationOfParentage.put("orderIsByConsent", "NO");
        declarationOfParentage.put("furtherDirections", "NO");
        declarationOfParentage.put("isFinalOrder", "YES");
        declarationOfParentage.put("appointedGuardian", "NO");
        declarationOfParentage.put("parentResponsible", "NO");
        declarationOfParentage.put("respondentsRefused", "NO");
        declarationOfParentage.put("refuseContactQuestions", "NO");
        declarationOfParentage.put("childPlacementApplications", "NO");
        declarationOfParentage.put("childPlacementQuestions", "NO");
        declarationOfParentage.put("childPlacementQuestionsForBlankOrder", "NO");
        declarationOfParentage.put("orderPlacedChildInCustody", "NO");
        declarationOfParentage.put("manageOrdersEducationSupervision", "NO");
        declarationOfParentage.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        declarationOfParentage.put("manageOrdersChildAssessment", "NO");
        declarationOfParentage.put("leaveToChangeChildSurname", "NO");
        declarationOfParentage.put("partyAllowedContactsAndConditions", "NO");
        declarationOfParentage.put("declarationOfParentage", "YES");
        declarationOfParentage.put("familyAssistanceOrder", "NO");
        declarationOfParentage.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> familyAssistanceOrder = new HashMap<>(commonQuestions);
        familyAssistanceOrder.put("orderTitle", "NO");
        familyAssistanceOrder.put("hearingDetails", "NO");
        familyAssistanceOrder.put("linkApplication", "NO");
        familyAssistanceOrder.put("approvalDate", "YES");
        familyAssistanceOrder.put("orderDetails", "NO");
        familyAssistanceOrder.put("approvalDateTime", "NO");
        familyAssistanceOrder.put("epoIncludePhrase", "NO");
        familyAssistanceOrder.put("uploadOrderFile", "NO");
        familyAssistanceOrder.put("needSealing", "NO");
        familyAssistanceOrder.put("epoChildrenDescription", "NO");
        familyAssistanceOrder.put("epoExpiryDate", "NO");
        familyAssistanceOrder.put("epoTypeAndPreventRemoval", "NO");
        familyAssistanceOrder.put("cafcassJurisdictions", "NO");
        familyAssistanceOrder.put("selectSingleChild", "NO");
        familyAssistanceOrder.put("reasonForSecureAccommodation", "NO");
        familyAssistanceOrder.put("childLegalRepresentation", "NO");
        familyAssistanceOrder.put("orderJurisdiction", "NO");
        familyAssistanceOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        familyAssistanceOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        familyAssistanceOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        familyAssistanceOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        familyAssistanceOrder.put("whichOthers", "NO");
        familyAssistanceOrder.put("dischargeOfCareDetails", "NO");
        familyAssistanceOrder.put("closeCase", "YES");
        familyAssistanceOrder.put("whichChildren", "YES");
        familyAssistanceOrder.put("orderIsByConsent", "YES");
        familyAssistanceOrder.put("furtherDirections", "YES");
        familyAssistanceOrder.put("isFinalOrder", "YES");
        familyAssistanceOrder.put("appointedGuardian", "NO");
        familyAssistanceOrder.put("parentResponsible", "NO");
        familyAssistanceOrder.put("respondentsRefused", "NO");
        familyAssistanceOrder.put("refuseContactQuestions", "NO");
        familyAssistanceOrder.put("childPlacementApplications", "NO");
        familyAssistanceOrder.put("childPlacementQuestions", "NO");
        familyAssistanceOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        familyAssistanceOrder.put("orderPlacedChildInCustody", "NO");
        familyAssistanceOrder.put("manageOrdersEducationSupervision", "NO");
        familyAssistanceOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        familyAssistanceOrder.put("manageOrdersChildAssessment", "NO");
        familyAssistanceOrder.put("leaveToChangeChildSurname", "NO");
        familyAssistanceOrder.put("partyAllowedContactsAndConditions", "NO");
        familyAssistanceOrder.put("declarationOfParentage", "NO");
        familyAssistanceOrder.put("familyAssistanceOrder", "YES");
        familyAssistanceOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> transparencyOrder = new HashMap<>(commonQuestions);
        transparencyOrder.put("orderTitle", "NO");
        transparencyOrder.put("hearingDetails", "NO");
        transparencyOrder.put("linkApplication", "NO");
        transparencyOrder.put("approvalDate", "YES");
        transparencyOrder.put("orderDetails", "NO");
        transparencyOrder.put("approvalDateTime", "NO");
        transparencyOrder.put("epoIncludePhrase", "NO");
        transparencyOrder.put("uploadOrderFile", "NO");
        transparencyOrder.put("needSealing", "NO");
        transparencyOrder.put("epoChildrenDescription", "NO");
        transparencyOrder.put("epoExpiryDate", "NO");
        transparencyOrder.put("epoTypeAndPreventRemoval", "NO");
        transparencyOrder.put("cafcassJurisdictions", "NO");
        transparencyOrder.put("selectSingleChild", "NO");
        transparencyOrder.put("reasonForSecureAccommodation", "NO");
        transparencyOrder.put("childLegalRepresentation", "NO");
        transparencyOrder.put("orderJurisdiction", "NO");
        transparencyOrder.put("manageOrdersExpiryDateWithMonth", "NO");
        transparencyOrder.put("manageOrdersExclusionRequirementDetails", "NO");
        transparencyOrder.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        transparencyOrder.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        transparencyOrder.put("whichOthers", "NO");
        transparencyOrder.put("dischargeOfCareDetails", "NO");
        transparencyOrder.put("closeCase", "YES");
        transparencyOrder.put("whichChildren", "YES");
        transparencyOrder.put("orderIsByConsent", "YES");
        transparencyOrder.put("furtherDirections", "YES");
        transparencyOrder.put("isFinalOrder", "YES");
        transparencyOrder.put("appointedGuardian", "NO");
        transparencyOrder.put("parentResponsible", "NO");
        transparencyOrder.put("respondentsRefused", "NO");
        transparencyOrder.put("refuseContactQuestions", "NO");
        transparencyOrder.put("childPlacementApplications", "NO");
        transparencyOrder.put("childPlacementQuestions", "NO");
        transparencyOrder.put("childPlacementQuestionsForBlankOrder", "NO");
        transparencyOrder.put("orderPlacedChildInCustody", "NO");
        transparencyOrder.put("manageOrdersEducationSupervision", "NO");
        transparencyOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        transparencyOrder.put("manageOrdersChildAssessment", "NO");
        transparencyOrder.put("leaveToChangeChildSurname", "NO");
        transparencyOrder.put("partyAllowedContactsAndConditions", "NO");
        transparencyOrder.put("declarationOfParentage", "NO");
        transparencyOrder.put("familyAssistanceOrder", "NO");
        transparencyOrder.put("manageOrdersTransparencyOrder", "YES");

        return Stream.of(
            Arguments.of(C21_BLANK_ORDER, blankOrderQuestions),
            Arguments.of(A81_PLACEMENT_BLANK_ORDER, placementBlankOrderQuestions),
            Arguments.of(C23_EMERGENCY_PROTECTION_ORDER, epoQuestions),
            Arguments.of(C26_SECURE_ACCOMMODATION_ORDER, secureAccommodationOrderQuestions),
            Arguments.of(C29_RECOVERY_OF_A_CHILD, recoveryOfChildQuestions),
            Arguments.of(C32A_CARE_ORDER, careOrderQuestions),
            Arguments.of(C32B_DISCHARGE_OF_CARE_ORDER, dischargeOfCareQuestions),
            Arguments.of(C34A_CONTACT_WITH_A_CHILD_IN_CARE, contactWithAChildOrderInCareOrder),
            Arguments.of(C35A_SUPERVISION_ORDER, supervisionOrderQuestions),
            Arguments.of(C39_CHILD_ASSESSMENT_ORDER, childAssessmentOrder),
            Arguments.of(C36_VARIATION_OR_EXTENSION_OF_SUPERVISION_ORDERS, varyOrExtendSupervisionOrder),
            Arguments.of(C37_EDUCATION_SUPERVISION_ORDER_DIGITAL, supervisionEducationOrder),
            Arguments.of(C43A_SPECIAL_GUARDIANSHIP_ORDER, specialGuardianshipOrderQuestions),
            Arguments.of(C34B_AUTHORITY_TO_REFUSE_CONTACT, refusedContactOrderQuestions),
            Arguments.of(C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER, childArrangementSpecificOrder),
            Arguments.of(C45A_PARENTAL_RESPONSIBILITY_ORDER, parentalResponsibilityOrder),
            Arguments.of(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN, appointmentOfChildrensGuardianQuestions),
            Arguments.of(A70_PLACEMENT_ORDER, placementOrder),
            Arguments.of(C44A_LEAVE_TO_CHANGE_A_SURNAME, leaveToChangeChildSurname),
            Arguments.of(C63A_DECLARATION_OF_PARENTAGE, declarationOfParentage),
            Arguments.of(C42_FAMILY_ASSISTANCE_ORDER, familyAssistanceOrder),
            Arguments.of(TRANSPARENCY_ORDER, transparencyOrder)
        );
    }

    private static Stream<Arguments> finalManualUploadOrders() {
        return MANUAL_ORDERS_WITH_IS_FINAL_ORDER_QUESTION.stream()
            .map(order -> Arguments.of(order, Map.ofEntries(
                Map.entry("partyAllowedContactsAndConditions", "NO"),
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
                Map.entry("translationRequirements", "YES"),
                Map.entry("closeCase", "YES"),
                Map.entry("orderIsByConsent", "NO"),
                Map.entry("appointedGuardian", "NO"),
                Map.entry("respondentsRefused", "NO"),
                Map.entry("refuseContactQuestions", "NO"),
                Map.entry("whichOthers", "NO"),
                Map.entry("parentResponsible", "NO"),
                Map.entry("selectSingleChild", "NO"),
                Map.entry("reasonForSecureAccommodation", "NO"),
                Map.entry("childLegalRepresentation", "NO"),
                Map.entry("orderJurisdiction", "NO"),
                Map.entry("orderToAmend", "NO"),
                Map.entry("uploadAmendedOrder", "NO"),
                Map.entry("childPlacementApplications", "NO"),
                Map.entry("childPlacementQuestions", "NO"),
                Map.entry("childPlacementQuestionsForBlankOrder", "NO"),
                Map.entry("manageOrdersChildAssessment", "NO"),
                Map.entry("manageOrdersEducationSupervision", "NO"),
                Map.entry("orderPlacedChildInCustody", "NO"),
                Map.entry("manageOrdersVaryOrExtendSupervisionOrder", "NO"),
                Map.entry("leaveToChangeChildSurname", "NO"),
                Map.entry("declarationOfParentage", "NO"),
                Map.entry("familyAssistanceOrder", "NO"),
                Map.entry("manageOrdersTransparencyOrder", "NO")
            )));
    }

    private static Stream<Arguments> nonFinalManualUploadOrders() {
        return Arrays.stream(Order.values())
            .filter(order -> OrderSourceType.MANUAL_UPLOAD == order.getSourceType())
            .filter(order -> !MANUAL_ORDERS_WITH_IS_FINAL_ORDER_QUESTION.contains(order))
            .map(order -> Arguments.of(order, Map.ofEntries(
                Map.entry("partyAllowedContactsAndConditions", "NO"),
                Map.entry("approver", "NO"),
                Map.entry("previewOrder", "YES"),
                Map.entry("orderTitle", "NO"),
                Map.entry("furtherDirections", "NO"),
                Map.entry("orderDetails", "NO"),
                Map.entry("whichChildren", "YES"),
                Map.entry("hearingDetails", "NO"),
                Map.entry("linkApplication", "NO"),
                Map.entry("reasonForSecureAccommodation", "NO"),
                Map.entry("childLegalRepresentation", "NO"),
                Map.entry("selectSingleChild", "NO"),
                Map.entry("orderJurisdiction", "NO"),
                Map.entry("approvalDate", "YES"),
                Map.entry("approvalDateTime", "NO"),
                Map.entry("dischargeOfCareDetails", "NO"),
                Map.entry("childArrangementSpecificIssueProhibitedSteps", "NO"),
                Map.entry("epoIncludePhrase", "NO"),
                Map.entry("epoExpiryDate", "NO"),
                Map.entry("isFinalOrder", "NO"),
                Map.entry("epoTypeAndPreventRemoval", "NO"),
                Map.entry("translationRequirements", "YES"),
                Map.entry("epoChildrenDescription", "NO"),
                Map.entry("manageOrdersExclusionRequirementDetails", "NO"),
                Map.entry("manageOrdersExpiryDateWithEndOfProceedings", "NO"),
                Map.entry("manageOrdersExpiryDateWithMonth", "NO"),
                Map.entry("cafcassJurisdictions", "NO"),
                Map.entry("needSealing", "YES"),
                Map.entry("uploadOrderFile", "YES"),
                Map.entry("closeCase", "YES"),
                Map.entry("orderIsByConsent", "NO"),
                Map.entry("whichOthers", "NO"),
                Map.entry("orderToAmend", "NO"),
                Map.entry("uploadAmendedOrder", "NO"),
                Map.entry("parentResponsible", "NO"),
                Map.entry("childPlacementApplications", "NO"),
                Map.entry("childPlacementQuestions", "NO"),
                Map.entry("childPlacementQuestionsForBlankOrder", "NO"),
                Map.entry("manageOrdersChildAssessment", "NO"),
                Map.entry("manageOrdersEducationSupervision", "NO"),
                Map.entry("manageOrdersVaryOrExtendSupervisionOrder", "NO"),
                Map.entry("appointedGuardian", "NO"),
                Map.entry("respondentsRefused", "NO"),
                Map.entry("refuseContactQuestions", "NO"),
                Map.entry("orderPlacedChildInCustody", "NO"),
                Map.entry("leaveToChangeChildSurname", "NO"),
                Map.entry("declarationOfParentage", "NO"),
                Map.entry("familyAssistanceOrder", "NO"),
                Map.entry("manageOrdersTransparencyOrder", "NO")
            )));
    }
}
