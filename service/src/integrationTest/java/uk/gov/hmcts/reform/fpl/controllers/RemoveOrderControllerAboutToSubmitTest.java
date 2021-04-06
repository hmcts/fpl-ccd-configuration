package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
class RemoveOrderControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final String REASON = "The order was removed because the order was removed";
    private static final UUID SDO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID REMOVED_ORDER_ID = UUID.randomUUID();

    @MockBean
    private IdentityService identityService;

    private Element<GeneratedOrder> selectedOrder;

    RemoveOrderControllerAboutToSubmitTest() {
        super("remove-order");
    }

    @BeforeEach
    void initialise() {
        selectedOrder = element(buildOrder());
    }

    @Test
    void shouldUpdateGeneratedOrderCollectionAndHiddenGeneratedOrderCollection() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            asCaseDetails(buildCaseData(selectedOrder))
        );

        CaseData responseData = extractCaseData(response);

        selectedOrder.getValue().setRemovalReason(REASON);

        assertThat(responseData.getOrderCollection()).isEmpty();
        assertThat(responseData.getHiddenOrders()).hasSize(1).containsOnly(selectedOrder);
    }

    @Test
    void shouldRemoveTemporaryFields() {
        CaseDetails caseDetails = asCaseDetails(buildCaseData(selectedOrder));

        caseDetails.getData().putAll(
            Map.of(
                "orderToBeRemoved", "dummy data",
                "orderTitleToBeRemoved", "dummy data",
                "orderIssuedDateToBeRemoved", "dummy data",
                "orderDateToBeRemoved", "dummy data",
                "hearingToUnlink", "dummy data",
                "showRemoveCMOFieldsFlag", "dummy data",
                "showReasonFieldFlag", "dummy data",
                "showRemoveSDOWarningFlag", "dummy data"
            )
        );

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        assertThat(response.getData()).doesNotContainKeys(
            "removableOrderList",
            "reasonToRemoveOrder",
            "orderToBeRemoved",
            "orderTitleToBeRemoved",
            "orderIssuedDateToBeRemoved",
            "orderDateToBeRemoved",
            "hearingToUnlink",
            "showRemoveCMOFieldsFlag",
            "showReasonFieldFlag",
            "showRemoveSDOWarningFlag"
        );
    }

    @Test
    void shouldUpdateChildrenPropertiesWhenRemovingAFinalOrder() {
        UUID childOneId = UUID.randomUUID();
        UUID childTwoId = UUID.randomUUID();

        List<Element<Child>> childrenList = List.of(
            element(childOneId, Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build()),
            element(childTwoId, Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build())
        );

        Element<GeneratedOrder> order1 = element(buildOrder(EMERGENCY_PROTECTION_ORDER, childrenList));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData returnedCaseData = extractCaseData(response);
        List<Element<Child>> returnedChildren = returnedCaseData.getChildren1();

        List<Element<Child>> expectedChildrenList = List.of(
            element(childOneId, Child.builder()
                .party(ChildParty.builder().build())
                .build()),
            element(childTwoId, Child.builder()
                .party(ChildParty.builder().build())
                .build())
        );

        assertThat(returnedChildren).isEqualTo(expectedChildrenList);
    }

    @Test
    void shouldNotUpdateChildrenPropertiesWhenRemovingANonFinalOrder() {
        List<Element<Child>> childrenList = List.of(
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build()),
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .party(ChildParty.builder().build())
                .build())
        );

        Element<GeneratedOrder> order1 = element(buildOrder(BLANK_ORDER, childrenList));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData returnedCaseData = extractCaseData(response);
        List<Element<Child>> returnedChildren = returnedCaseData.getChildren1();

        assertThat(returnedChildren).isEqualTo(childrenList);
    }

    @Test
    void shouldRemoveSealedCaseManagementOrderAndRemoveHearingAssociation() {
        Element<HearingOrder> caseManagementOrder1 = element(REMOVED_ORDER_ID, HearingOrder.builder()
            .status(APPROVED)
            .type(AGREED_CMO)
            .build());

        List<Element<HearingOrder>> caseManagementOrders = List.of(
            caseManagementOrder1,
            element(HearingOrder.builder().build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(REMOVED_ORDER_ID)
                .build()));

        CaseData caseData = CaseData.builder()
            .sealedCMOs(caseManagementOrders)
            .hearingDetails(hearingBookings)
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(REMOVED_ORDER_ID, "Sealed case management order issued on 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        List<Element<HearingOrder>> hiddenCMOs = responseData.getHiddenCMOs();
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        assertThat(hiddenCMOs).hasSize(1).first().isEqualTo(caseManagementOrder1);
        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    @Test
    void shouldRemoveDraftOrderFromTheHearingOrdersDraftBundles() {
        Element<HearingOrder> draftOrder = element(REMOVED_ORDER_ID, HearingOrder.builder()
            .status(SEND_TO_JUDGE)
            .type(C21)
            .dateSent(LocalDate.of(2020, 6, 15))
            .build());

        Element<HearingOrder> draftCMO = element(
            HearingOrder.builder().type(AGREED_CMO).status(SEND_TO_JUDGE).build());

        Element<HearingOrdersBundle> hearingOrdersBundle = element(HearingOrdersBundle.builder()
            .orders(newArrayList(draftOrder, draftCMO))
            .build());

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder().build())))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(REMOVED_ORDER_ID, "Draft order sent on 15 June 2020"))
                .build())
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData));

        Element<HearingOrdersBundle> expectedHearingOrderBundle = element(hearingOrdersBundle.getId(),
            HearingOrdersBundle.builder().orders(newArrayList(draftCMO)).build());

        assertThat(responseData.getHearingOrdersBundlesDrafts())
            .hasSize(1)
            .first().isEqualTo(expectedHearingOrderBundle);
    }

    @Test
    void shouldRemoveSDONoticeOfProceedingsAndSetStateToGatekeepingWhenRemovingASealedSDO() {
        UUID newSDOId = UUID.randomUUID();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Watson")
                .build())
            .orderStatus(SEALED)
            .build();

        CaseData caseData = CaseData.builder()
            .state(CASE_MANAGEMENT)
            .reasonToRemoveOrder(REASON)
            .standardDirectionOrder(standardDirectionOrder)
            .noticeOfProceedingsBundle(List.of(element(DocumentBundle.builder().build())))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(SDO_ID, "Gatekeeping order - 15 June 2020"))
                .build())
            .build();

        StandardDirectionOrder expectedSDO = StandardDirectionOrder.builder()
            .orderStatus(SEALED)
            .removalReason(REASON)
            .build();

        when(identityService.generateId()).thenReturn(newSDOId);

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);
        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getHiddenStandardDirectionOrders()).isEqualTo(List.of(element(newSDOId, expectedSDO)));
        assertThat(responseData.getState()).isEqualTo(GATEKEEPING);
        assertNull(responseData.getNoticeOfProceedingsBundle());
    }

    @Test
    void shouldRemoveDraftCaseManagementOrderFromHearingOrderBundleDraftsAndRemoveHearingAssociation() {
        UUID removedOrderId = UUID.randomUUID();
        UUID additionalOrderId = UUID.randomUUID();
        UUID hearingOrderBundleId = UUID.randomUUID();

        Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
            .status(DRAFT)
            .type(HearingOrderType.DRAFT_CMO)
            .build());

        List<Element<HearingOrder>> caseManagementOrders = newArrayList(
            orderToBeRemoved,
            element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(removedOrderId)
                .build()));

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(newArrayList(
                element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(caseManagementOrders).build())
            ))
            .hearingDetails(hearingBookings)
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        assertThat(responseData.getHearingOrdersBundlesDrafts()).isEqualTo(newArrayList(
            element(hearingOrderBundleId, HearingOrdersBundle.builder().orders(newArrayList(
                element(additionalOrderId, HearingOrder.builder().type(HearingOrderType.DRAFT_CMO).build())
            )).build())
        ));

        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    @Test
    void shouldRemoveDraftCaseManagementOrderFromDraftCaseManagementOrdersAndRemoveHearingAssociation() {
        UUID removedOrderId = UUID.randomUUID();
        UUID additionalOrderId = UUID.randomUUID();

        Element<HearingOrder> orderToBeRemoved = element(removedOrderId, HearingOrder.builder()
            .status(DRAFT)
            .type(HearingOrderType.DRAFT_CMO)
            .build());

        List<Element<HearingOrder>> caseManagementOrders = newArrayList(
            orderToBeRemoved,
            element(additionalOrderId, HearingOrder.builder().status(DRAFT).build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(removedOrderId)
                .build()));

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(caseManagementOrders)
            .hearingDetails(hearingBookings)
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(removedOrderId, "Draft case management order - 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(response);
        HearingBooking unlinkedHearing = responseData.getHearingDetails().get(0).getValue();

        Element<HearingOrder> expectedHearingOrderElement = element(additionalOrderId, HearingOrder.builder()
            .title("Draft CMO from advocates' meeting")
            .type(DRAFT_CMO)
            .status(DRAFT).build());

        assertThat(responseData.getDraftUploadedCMOs()).isEqualTo(newArrayList(expectedHearingOrderElement));

        assertNull(unlinkedHearing.getCaseManagementOrderId());
    }

    private CaseData buildCaseData(Element<GeneratedOrder> order) {
        return CaseData.builder()
            .orderCollection(List.of(order))
            .removableOrderList(buildRemovableOrderList(order.getId()))
            .reasonToRemoveOrder(REASON)
            .build();
    }

    private DynamicList buildRemovableOrderList(UUID id) {
        return DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(id)
                .label("order - 12 March 1234")
                .build()))
            .build();
    }

    private GeneratedOrder buildOrder() {
        return GeneratedOrder.builder()
            .type("Blank order (C21)")
            .title("order")
            .dateOfIssue("12 March 1234")
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, List<Element<Child>> children) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .children(children)
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }
}
