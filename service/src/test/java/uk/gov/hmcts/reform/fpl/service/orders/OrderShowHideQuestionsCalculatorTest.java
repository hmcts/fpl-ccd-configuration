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
import static uk.gov.hmcts.reform.fpl.model.order.Order.C37_EDUCATION_SUPERVISION_ORDER_DIGITAL;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C39_CHILD_ASSESSMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C42_FAMILY_ASSISTANCE_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C44A_LEAVE_TO_CHANGE_A_SURNAME;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C45A_PARENTAL_RESPONSIBILITY_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C63A_DECLARATION_OF_PARENTAGE;
import static uk.gov.hmcts.reform.fpl.model.order.Order.FL404A_NON_MOLESTATION_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.Order.TRANSPARENCY_ORDER;

class OrderShowHideQuestionsCalculatorTest {

    private static final Set<Order> MANUAL_ORDERS_WITH_IS_FINAL_ORDER_QUESTION = Set.of(
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
            Map.entry("nonMolestationOrder", "NO"),
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
    @MethodSource("nonFinalManualUploadOrders")
    void calculateManualUpload(Order order, Map<String, String> expectedShowHideMap) {
        assertThat(underTest.calculate(order))
            .containsExactlyInAnyOrderEntriesOf(expectedShowHideMap);
    }

    private static Stream<Arguments> orderWithExpectedMap() {
        Map<String, String> allQuestions = new HashMap<>();
        allQuestions.put("hearingDetails", "YES");
        allQuestions.put("linkApplication", "YES");
        allQuestions.put("approver", "YES");
        allQuestions.put("previewOrder", "YES");
        allQuestions.put("orderToAmend", "NO");
        allQuestions.put("translationRequirements", "NO");
        allQuestions.put("uploadAmendedOrder", "NO");
        allQuestions.put("orderTitle", "NO");
        allQuestions.put("furtherDirections", "NO");
        allQuestions.put("approvalDate", "NO");
        allQuestions.put("approvalDateTime", "NO");
        allQuestions.put("epoIncludePhrase", "NO");
        allQuestions.put("uploadOrderFile", "NO");
        allQuestions.put("needSealing", "NO");
        allQuestions.put("epoChildrenDescription", "NO");
        allQuestions.put("epoExpiryDate", "NO");
        allQuestions.put("epoTypeAndPreventRemoval", "NO");
        allQuestions.put("orderDetails", "NO");
        allQuestions.put("cafcassJurisdictions", "NO");
        allQuestions.put("whichChildren", "NO");
        allQuestions.put("selectSingleChild", "NO");
        allQuestions.put("reasonForSecureAccommodation", "NO");
        allQuestions.put("childLegalRepresentation", "NO");
        allQuestions.put("orderJurisdiction", "NO");
        allQuestions.put("manageOrdersExpiryDateWithMonth", "NO");
        allQuestions.put("manageOrdersExclusionRequirementDetails", "NO");
        allQuestions.put("manageOrdersExpiryDateWithEndOfProceedings", "NO");
        allQuestions.put("childArrangementSpecificIssueProhibitedSteps", "NO");
        allQuestions.put("isFinalOrder", "NO");
        allQuestions.put("closeCase", "NO");
        allQuestions.put("whichOthers", "NO");
        allQuestions.put("dischargeOfCareDetails", "NO");
        allQuestions.put("orderIsByConsent", "NO");
        allQuestions.put("appointedGuardian", "NO");
        allQuestions.put("respondentsRefused", "NO");
        allQuestions.put("refuseContactQuestions", "NO");
        allQuestions.put("parentResponsible", "NO");
        allQuestions.put("childPlacementApplications", "NO");
        allQuestions.put("childPlacementQuestions", "NO");
        allQuestions.put("childPlacementQuestionsForBlankOrder", "NO");
        allQuestions.put("manageOrdersEducationSupervision", "NO");
        allQuestions.put("orderPlacedChildInCustody", "NO");
        allQuestions.put("manageOrdersChildAssessment", "NO");
        allQuestions.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        allQuestions.put("leaveToChangeChildSurname", "NO");
        allQuestions.put("partyAllowedContactsAndConditions", "NO");
        allQuestions.put("declarationOfParentage", "NO");
        allQuestions.put("familyAssistanceOrder", "NO");
        allQuestions.put("nonMolestationOrder", "NO");
        allQuestions.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> careOrderQuestions = new HashMap<>(allQuestions);
        careOrderQuestions.put("furtherDirections", "YES");
        careOrderQuestions.put("approvalDate", "YES");
        careOrderQuestions.put("whichChildren", "YES");
        careOrderQuestions.put("closeCase", "YES");

        Map<String, String> dischargeOfCareQuestions = new HashMap<>(allQuestions);
        dischargeOfCareQuestions.put("furtherDirections", "YES");
        dischargeOfCareQuestions.put("approvalDate", "YES");
        dischargeOfCareQuestions.put("whichChildren", "YES");
        dischargeOfCareQuestions.put("dischargeOfCareDetails", "YES");
        dischargeOfCareQuestions.put("isFinalOrder", "YES");
        dischargeOfCareQuestions.put("closeCase", "YES");


        Map<String, String> epoQuestions = new HashMap<>(allQuestions);
        epoQuestions.put("furtherDirections", "YES");
        epoQuestions.put("approvalDateTime", "YES");
        epoQuestions.put("epoIncludePhrase", "YES");
        epoQuestions.put("epoChildrenDescription", "YES");
        epoQuestions.put("epoExpiryDate", "YES");
        epoQuestions.put("epoTypeAndPreventRemoval", "YES");
        epoQuestions.put("whichChildren", "YES");
        epoQuestions.put("closeCase", "YES");
        epoQuestions.put("isFinalOrder", "YES");


        Map<String, String> blankOrderQuestions = new HashMap<>(allQuestions);
        blankOrderQuestions.put("orderTitle", "YES");
        blankOrderQuestions.put("approvalDate", "YES");
        blankOrderQuestions.put("orderDetails", "YES");
        blankOrderQuestions.put("whichChildren", "YES");


        Map<String, String> placementBlankOrderQuestions = new HashMap<>(allQuestions);
        placementBlankOrderQuestions.put("hearingDetails", "NO");
        placementBlankOrderQuestions.put("linkApplication", "NO");
        placementBlankOrderQuestions.put("approvalDate", "YES");
        placementBlankOrderQuestions.put("isFinalOrder", "YES");
        placementBlankOrderQuestions.put("childPlacementApplications", "YES");
        placementBlankOrderQuestions.put("childPlacementQuestionsForBlankOrder", "YES");


        Map<String, String> supervisionOrderQuestions = new HashMap<>(allQuestions);
        supervisionOrderQuestions.put("approvalDate", "YES");
        supervisionOrderQuestions.put("furtherDirections", "YES");
        supervisionOrderQuestions.put("whichChildren", "YES");
        supervisionOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        supervisionOrderQuestions.put("closeCase", "YES");


        Map<String, String> specialGuardianshipOrderQuestions = new HashMap<>(allQuestions);
        specialGuardianshipOrderQuestions.put("furtherDirections", "YES");
        specialGuardianshipOrderQuestions.put("approvalDateTime", "YES");
        specialGuardianshipOrderQuestions.put("whichChildren", "YES");
        specialGuardianshipOrderQuestions.put("closeCase", "YES");
        specialGuardianshipOrderQuestions.put("isFinalOrder", "YES");
        specialGuardianshipOrderQuestions.put("orderIsByConsent", "YES");
        specialGuardianshipOrderQuestions.put("appointedGuardian", "YES");


        Map<String, String> appointmentOfChildrensGuardianQuestions = new HashMap<>(allQuestions);
        appointmentOfChildrensGuardianQuestions.put("approvalDate", "YES");
        appointmentOfChildrensGuardianQuestions.put("furtherDirections", "YES");
        appointmentOfChildrensGuardianQuestions.put("cafcassJurisdictions", "YES");


        Map<String, String> childArrangementSpecificOrder = new HashMap<>(allQuestions);
        childArrangementSpecificOrder.put("approvalDate", "YES");
        childArrangementSpecificOrder.put("furtherDirections", "NO");
        childArrangementSpecificOrder.put("orderDetails", "YES");
        childArrangementSpecificOrder.put("whichChildren", "YES");
        childArrangementSpecificOrder.put("childArrangementSpecificIssueProhibitedSteps", "YES");
        childArrangementSpecificOrder.put("closeCase", "YES");
        childArrangementSpecificOrder.put("isFinalOrder", "YES");
        childArrangementSpecificOrder.put("orderIsByConsent", "YES");


        Map<String, String> refusedContactOrderQuestions = new HashMap<>(allQuestions);
        refusedContactOrderQuestions.put("hearingDetails", "NO");
        refusedContactOrderQuestions.put("linkApplication", "NO");
        refusedContactOrderQuestions.put("approvalDate", "YES");
        refusedContactOrderQuestions.put("whichChildren", "YES");
        refusedContactOrderQuestions.put("closeCase", "YES");
        refusedContactOrderQuestions.put("isFinalOrder", "YES");
        refusedContactOrderQuestions.put("orderIsByConsent", "YES");
        refusedContactOrderQuestions.put("respondentsRefused", "YES");
        refusedContactOrderQuestions.put("refuseContactQuestions", "YES");


        Map<String, String> secureAccommodationOrderQuestions = new HashMap<>(allQuestions);
        secureAccommodationOrderQuestions.put("furtherDirections", "YES");
        secureAccommodationOrderQuestions.put("approvalDateTime", "YES");
        secureAccommodationOrderQuestions.put("selectSingleChild", "YES");
        secureAccommodationOrderQuestions.put("reasonForSecureAccommodation", "YES");
        secureAccommodationOrderQuestions.put("childLegalRepresentation", "YES");
        secureAccommodationOrderQuestions.put("orderIsByConsent", "YES");
        secureAccommodationOrderQuestions.put("orderJurisdiction", "YES");
        secureAccommodationOrderQuestions.put("manageOrdersExpiryDateWithMonth", "YES");
        secureAccommodationOrderQuestions.put("closeCase", "YES");
        secureAccommodationOrderQuestions.put("isFinalOrder", "YES");


        Map<String, String> contactWithAChildOrderInCareOrder = new HashMap<>(allQuestions);
        contactWithAChildOrderInCareOrder.put("hearingDetails", "YES");
        contactWithAChildOrderInCareOrder.put("linkApplication", "YES");
        contactWithAChildOrderInCareOrder.put("approvalDate", "YES");
        contactWithAChildOrderInCareOrder.put("closeCase", "YES");
        contactWithAChildOrderInCareOrder.put("whichChildren", "YES");
        contactWithAChildOrderInCareOrder.put("orderIsByConsent", "YES");
        contactWithAChildOrderInCareOrder.put("isFinalOrder", "YES");
        contactWithAChildOrderInCareOrder.put("partyAllowedContactsAndConditions", "YES");


        Map<String, String> parentalResponsibilityOrder = new HashMap<>(allQuestions);
        parentalResponsibilityOrder.put("approvalDateTime", "YES");
        parentalResponsibilityOrder.put("closeCase", "YES");
        parentalResponsibilityOrder.put("whichChildren", "YES");
        parentalResponsibilityOrder.put("orderIsByConsent", "YES");
        parentalResponsibilityOrder.put("furtherDirections", "YES");
        parentalResponsibilityOrder.put("isFinalOrder", "YES");
        parentalResponsibilityOrder.put("parentResponsible", "YES");


        Map<String, String> recoveryOfChildQuestions = new HashMap<>(allQuestions);
        recoveryOfChildQuestions.put("approvalDate", "YES");
        recoveryOfChildQuestions.put("closeCase", "YES");
        recoveryOfChildQuestions.put("whichChildren", "YES");
        recoveryOfChildQuestions.put("furtherDirections", "YES");
        recoveryOfChildQuestions.put("isFinalOrder", "YES");
        recoveryOfChildQuestions.put("orderPlacedChildInCustody", "YES");

        Map<String, String> placementOrder = new HashMap<>(allQuestions);
        placementOrder.put("hearingDetails", "NO");
        placementOrder.put("linkApplication", "NO");
        placementOrder.put("approvalDate", "YES");
        placementOrder.put("closeCase", "YES");
        placementOrder.put("isFinalOrder", "YES");
        placementOrder.put("childPlacementApplications", "YES");
        placementOrder.put("childPlacementQuestions", "YES");
        placementOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "NO");
        placementOrder.put("leaveToChangeChildSurname", "NO");
        placementOrder.put("partyAllowedContactsAndConditions", "NO");
        placementOrder.put("declarationOfParentage", "NO");
        placementOrder.put("familyAssistanceOrder", "NO");

        Map<String, String> childAssessmentOrder = new HashMap<>(allQuestions);
        childAssessmentOrder.put("hearingDetails", "NO");
        childAssessmentOrder.put("linkApplication", "NO");
        childAssessmentOrder.put("approvalDate", "YES");
        childAssessmentOrder.put("selectSingleChild", "YES");
        childAssessmentOrder.put("closeCase", "YES");
        childAssessmentOrder.put("orderIsByConsent", "YES");
        childAssessmentOrder.put("isFinalOrder", "YES");
        childAssessmentOrder.put("manageOrdersChildAssessment", "YES");

        Map<String, String> supervisionEducationOrder = new HashMap<>(allQuestions);
        supervisionEducationOrder.put("hearingDetails", "NO");
        supervisionEducationOrder.put("linkApplication", "NO");
        supervisionEducationOrder.put("approvalDate", "YES");
        supervisionEducationOrder.put("closeCase", "YES");
        supervisionEducationOrder.put("whichChildren", "YES");
        supervisionEducationOrder.put("orderIsByConsent", "YES");
        supervisionEducationOrder.put("isFinalOrder", "YES");
        supervisionEducationOrder.put("manageOrdersEducationSupervision", "YES");

        Map<String, String> varyOrExtendSupervisionOrder = new HashMap<>(allQuestions);
        varyOrExtendSupervisionOrder.put("hearingDetails", "NO");
        varyOrExtendSupervisionOrder.put("linkApplication", "NO");
        varyOrExtendSupervisionOrder.put("approvalDate", "YES");
        varyOrExtendSupervisionOrder.put("whichChildren", "YES");
        varyOrExtendSupervisionOrder.put("orderIsByConsent", "YES");
        varyOrExtendSupervisionOrder.put("isFinalOrder", "YES");
        varyOrExtendSupervisionOrder.put("manageOrdersVaryOrExtendSupervisionOrder", "YES");

        Map<String, String> leaveToChangeChildSurname = new HashMap<>(allQuestions);
        leaveToChangeChildSurname.put("hearingDetails", "NO");
        leaveToChangeChildSurname.put("linkApplication", "NO");
        leaveToChangeChildSurname.put("approvalDate", "YES");
        leaveToChangeChildSurname.put("whichChildren", "YES");
        leaveToChangeChildSurname.put("orderIsByConsent", "YES");
        leaveToChangeChildSurname.put("isFinalOrder", "YES");
        leaveToChangeChildSurname.put("leaveToChangeChildSurname", "YES");

        Map<String, String> declarationOfParentage = new HashMap<>(allQuestions);
        declarationOfParentage.put("approvalDate", "YES");
        declarationOfParentage.put("selectSingleChild", "YES");
        declarationOfParentage.put("closeCase", "YES");
        declarationOfParentage.put("isFinalOrder", "YES");
        declarationOfParentage.put("declarationOfParentage", "YES");

        Map<String, String> familyAssistanceOrder = new HashMap<>(allQuestions);
        familyAssistanceOrder.put("hearingDetails", "NO");
        familyAssistanceOrder.put("linkApplication", "NO");
        familyAssistanceOrder.put("approvalDate", "YES");
        familyAssistanceOrder.put("closeCase", "YES");
        familyAssistanceOrder.put("whichChildren", "YES");
        familyAssistanceOrder.put("orderIsByConsent", "YES");
        familyAssistanceOrder.put("furtherDirections", "YES");
        familyAssistanceOrder.put("isFinalOrder", "YES");
        familyAssistanceOrder.put("familyAssistanceOrder", "YES");
        familyAssistanceOrder.put("manageOrdersTransparencyOrder", "NO");

        Map<String, String> transparencyOrder = new HashMap<>(allQuestions);
        transparencyOrder.put("hearingDetails", "NO");
        transparencyOrder.put("linkApplication", "NO");
        transparencyOrder.put("approvalDate", "YES");
        transparencyOrder.put("orderIsByConsent", "YES");
        transparencyOrder.put("isFinalOrder", "YES");
        transparencyOrder.put("manageOrdersTransparencyOrder", "YES");


        Map<String, String> nonMolestationOrder = new HashMap<>(allQuestions);
        nonMolestationOrder.put("linkApplication", "NO");
        nonMolestationOrder.put("approvalDate", "YES");
        nonMolestationOrder.put("whichChildren", "YES");
        nonMolestationOrder.put("orderIsByConsent", "YES");
        nonMolestationOrder.put("nonMolestationOrder", "YES");
        nonMolestationOrder.put("previewOrder", "YES");

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
            Arguments.of(FL404A_NON_MOLESTATION_ORDER, nonMolestationOrder),
            Arguments.of(TRANSPARENCY_ORDER, transparencyOrder)
        );
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
                Map.entry("nonMolestationOrder", "NO"),
                Map.entry("manageOrdersTransparencyOrder", "NO")
            )));
    }
}
