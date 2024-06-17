package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;
import uk.gov.hmcts.reform.fpl.model.order.OrderTempQuestions;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersControllerInitialSectionMidEventTest extends AbstractCallbackTest {

    private static final String NO = "NO";
    private static final String YES = "YES";
    private static final DocumentReference ORDER_DOCUMENT = testDocumentReference();
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference();
    private static final DocumentReference UHO_DOCUMENT = testDocumentReference();
    private static final DocumentReference CMO_DOCUMENT = testDocumentReference();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CMO_ID = UUID.randomUUID();
    private static final UUID SDO_ID = StandardDirectionOrder.COLLECTION_ID;
    private static final UUID UHO_ID = UrgentHearingOrder.COLLECTION_ID;

    ManageOrdersControllerInitialSectionMidEventTest() {
        super("manage-orders");
    }

    @ParameterizedTest
    @MethodSource("amendableOrderDocument")
    void shouldPrePopulateDocumentToAmendWhenUsingAmendOperation(UUID selectedId, DocumentReference expectedDocument) {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(OrderOperation.AMEND)
                .manageOrdersAmendmentList(DynamicList.builder()
                    .value(DynamicListElement.builder().code(selectedId).build())
                    .build())
                .build())
            .orderCollection(List.of(
                element(ORDER_ID, GeneratedOrder.builder().document(ORDER_DOCUMENT).build())
            ))
            .sealedCMOs(List.of(element(CMO_ID, HearingOrder.builder().order(CMO_DOCUMENT).build())))
            .urgentHearingOrder(UrgentHearingOrder.builder().order(UHO_DOCUMENT).build())
            .standardDirectionOrder(StandardDirectionOrder.builder().orderDoc(SDO_DOCUMENT).build())
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "initial-selection"));
        ManageOrdersEventData eventData = responseData.getManageOrdersEventData();

        assertThat(eventData.getManageOrdersOrderToAmend()).isEqualTo(expectedDocument);
        assertThat(eventData.getOrderTempQuestions()).isEqualTo(OrderTempQuestions.builder()
            .approver(NO)
            .previewOrder(NO)
            .furtherDirections(NO)
            .orderDetails(NO)
            .whichChildren(NO)
            .childLegalRepresentation(NO)
            .orderIsByConsent(NO)
            .reasonForSecureAccommodation(NO)
            .orderJurisdiction(NO)
            .selectSingleChild(NO)
            .whichOthers(NO)
            .hearingDetails(NO)
            .approvalDate(NO)
            .translationRequirements(NO)
            .approvalDateTime(NO)
            .dischargeOfCareDetails(NO)
            .epoIncludePhrase(NO)
            .epoExpiryDate(NO)
            .epoTypeAndPreventRemoval(NO)
            .epoChildrenDescription(NO)
            .orderTitle(NO)
            .childArrangementSpecificIssueProhibitedSteps(NO)
            .manageOrdersExclusionRequirementDetails(NO)
            .manageOrdersExpiryDateWithEndOfProceedings(NO)
            .manageOrdersExpiryDateWithMonth(NO)
            .cafcassJurisdictions(NO)
            .linkApplication(NO)
            .needSealing(NO)
            .uploadOrderFile(NO)
            .closeCase(NO)
            .isFinalOrder(NO)
            .orderToAmend(YES)
            .uploadAmendedOrder(YES)
            .appointedGuardian(NO)
            .orderIsByConsent(NO)
            .parentResponsible(NO)
            .orderPlacedChildInCustody(NO)
            .childPlacementApplications(NO)
            .childPlacementQuestions(NO)
            .childPlacementQuestionsForBlankOrder(NO)
            .declarationOfParentage(NO)
            .manageOrdersChildAssessment(NO)
            .manageOrdersEducationSupervision(NO)
            .manageOrdersVaryOrExtendSupervisionOrder(NO)
            .respondentsRefused(NO)
            .refuseContactQuestions(NO)
            .leaveToChangeChildSurname(NO)
            .familyAssistanceOrder(NO)
            .partyAllowedContactsAndConditions(NO)
            .nonMolestationOrder(NO)
            .build()
        );
    }

    @Test
    void shouldPrePopulateIssueDetailsSectionDataWhenCreatingBlankOrderForClosedCase() {
        CaseData caseData = CaseData.builder()
            .state(CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperationClosedState(OrderOperation.CREATE)
                .build())
            .build();

        CaseData responseData = extractCaseData(postMidEvent(caseData, "initial-selection"));
        ManageOrdersEventData eventData = responseData.getManageOrdersEventData();

        assertThat(eventData.getManageOrdersState()).isEqualTo(CLOSED);
        assertThat(eventData.getManageOrdersType()).isEqualTo(C21_BLANK_ORDER);
        assertThat(eventData.getOrderTempQuestions()).isEqualTo(OrderTempQuestions.builder()
            .approver(YES)
            .previewOrder(YES)
            .furtherDirections(NO)
            .orderDetails(YES)
            .whichChildren(YES)
            .childLegalRepresentation(NO)
            .orderIsByConsent(NO)
            .reasonForSecureAccommodation(NO)
            .orderJurisdiction(NO)
            .selectSingleChild(NO)
            .whichOthers(NO)
            .hearingDetails(YES)
            .approvalDate(YES)
            .translationRequirements(NO)
            .approvalDateTime(NO)
            .dischargeOfCareDetails(NO)
            .epoIncludePhrase(NO)
            .epoExpiryDate(NO)
            .epoTypeAndPreventRemoval(NO)
            .epoChildrenDescription(NO)
            .appointedGuardian(NO)
            .manageOrdersExclusionRequirementDetails(NO)
            .manageOrdersExpiryDateWithEndOfProceedings(NO)
            .manageOrdersExpiryDateWithMonth(NO)
            .cafcassJurisdictions(NO)
            .linkApplication(NO)
            .needSealing(NO)
            .uploadOrderFile(NO)
            .childArrangementSpecificIssueProhibitedSteps(NO)
            .closeCase(NO)
            .isFinalOrder(NO)
            .orderToAmend(NO)
            .uploadAmendedOrder(NO)
            .appointedGuardian(NO)
            .orderIsByConsent(NO)
            .orderTitle(YES)
            .parentResponsible(NO)
            .childPlacementApplications(NO)
            .childPlacementQuestions(NO)
            .childPlacementQuestionsForBlankOrder(NO)
            .declarationOfParentage(NO)
            .manageOrdersEducationSupervision(NO)
            .orderPlacedChildInCustody(NO)
            .manageOrdersChildAssessment(NO)
            .manageOrdersVaryOrExtendSupervisionOrder(NO)
            .respondentsRefused(NO)
            .refuseContactQuestions(NO)
            .leaveToChangeChildSurname(NO)
            .partyAllowedContactsAndConditions(NO)
            .familyAssistanceOrder(NO)
            .nonMolestationOrder(NO)
            .build()
        );
    }

    @Test
    void shouldNotPopulateHiddenFieldValuesWhenCreatingBlankOrderForTheCaseNotInClosedState() {
        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(OrderOperation.CREATE)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "initial-selection");
        assertThat(response.getData()).doesNotContainKey("issuingDetailsSectionSubHeader");
        assertThat(response.getData()).containsEntry("orderTempQuestions", null);
    }

    private static Stream<Arguments> amendableOrderDocument() {
        return Stream.of(
            Arguments.of(ORDER_ID, ORDER_DOCUMENT),
            Arguments.of(CMO_ID, CMO_DOCUMENT),
            Arguments.of(SDO_ID, SDO_DOCUMENT),
            Arguments.of(UHO_ID, UHO_DOCUMENT)
        );
    }
}
