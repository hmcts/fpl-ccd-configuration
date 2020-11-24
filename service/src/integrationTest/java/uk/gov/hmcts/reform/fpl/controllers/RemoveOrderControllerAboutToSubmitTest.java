package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ActiveProfiles("integration-test")
@WebMvcTest(RemoveOrderController.class)
@OverrideAutoConfiguration(enabled = true)
public class RemoveOrderControllerAboutToSubmitTest extends AbstractControllerTest {
    private static final String REASON = "The order was removed because the order was removed";

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

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

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
                "unlinkedHearing", "dummy data",
                "showRemoveCMOFieldsFlag", "dummy data"
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
            "unlinkedHearing",
            "showRemoveCMOFieldsFlag"
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

        CaseData returnedCaseData = mapper.convertValue(response.getData(), CaseData.class);
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

        CaseData returnedCaseData = mapper.convertValue(response.getData(), CaseData.class);
        List<Element<Child>> returnedChildren = returnedCaseData.getChildren1();

        assertThat(returnedChildren).isEqualTo(childrenList);
    }

    @Test
    void shouldRemoveCaseManagementOrderAndRemoveHearingAssociation() {
        UUID removedOrderId = UUID.randomUUID();

        Element<CaseManagementOrder> caseManagementOrder1 = element(removedOrderId, CaseManagementOrder.builder()
            .status(APPROVED)
            .build());

        List<Element<CaseManagementOrder>> caseManagementOrders = List.of(
            caseManagementOrder1,
            element(CaseManagementOrder.builder().build()));

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(HearingBooking.builder()
                .caseManagementOrderId(removedOrderId)
                .build()));

        CaseData caseData = CaseData.builder()
            .sealedCMOs(caseManagementOrders)
            .hearingDetails(hearingBookings)
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(removedOrderId, "Case management order - 15 June 2020"))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);
        List<Element<CaseManagementOrder>> hiddenCMOs = responseData.getHiddenCMOs();

        assertThat(hiddenCMOs).hasSize(1);
        assertThat(hiddenCMOs.get(0)).isEqualTo(caseManagementOrder1);
        assertThat(responseData.getSealedCMOs()).hasSize(1);
        assertThat(responseData.getHearingDetails().get(0).getValue().getCaseManagementOrderId()).isNull();
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
