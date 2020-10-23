package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    RemoveOrderService.class, JacksonAutoConfiguration.class
})
class RemoveOrderServiceTest {

    @Autowired
    private RemoveOrderService service;

    @Test
    void shouldMakeDynamicListOfBlankOrders() {
        List<Element<GeneratedOrder>> orders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(BLANK_ORDER, "order 2", "16 July 2020"))
        );

        DynamicList listOfOrders = service.buildDynamicListOfOrders(orders);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(orders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(orders.get(1).getId(), "order 2 - 16 July 2020")
            ))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldMakeDynamicListOfMixedOrderTypes() {
        List<Element<GeneratedOrder>> orders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(CARE_ORDER, "order 2", "16 July 2020")),
            element(buildOrder(EMERGENCY_PROTECTION_ORDER, "order 3", "17 August 2020")),
            element(buildOrder(SUPERVISION_ORDER, "order 4", "18 September 2020"))
        );

        DynamicList listOfOrders = service.buildDynamicListOfOrders(orders);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(orders.get(0).getId(), "order 1 - 15 June 2020"),
                buildListElement(orders.get(1).getId(), "order 2 - 16 July 2020"),
                buildListElement(orders.get(2).getId(), "order 3 - 17 August 2020"),
                buildListElement(orders.get(3).getId(), "order 4 - 18 September 2020")
            ))
            .build();

        assertThat(listOfOrders).isEqualTo(expectedList);
    }

    @Test
    void shouldRemoveSelectedOrderFromMainListAndAddToHiddenList() {
        Element<GeneratedOrder> order1 = element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020"));
        Element<GeneratedOrder> order2 = element(buildOrder(BLANK_ORDER, "order 2", "16 July 2020"));

        List<Element<GeneratedOrder>> orders = new ArrayList<>(List.of(order1, order2));
        List<Element<GeneratedOrder>> hiddenOrders = new ArrayList<>();

        DynamicList listToRemove = DynamicList.builder()
            .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
            .build();

        String reason = "added to the wrong case for some reason, don't ask me how users do this but they do";

        service.hideOrder(orders, hiddenOrders, listToRemove, reason);

        assertThat(orders).hasSize(1).containsOnly(order2);
        assertThat(hiddenOrders).hasSize(1).containsOnly(order1);
    }

    @Test
    void shouldSetTheRemovalReasonForTheRemovedOrder() {
        Element<GeneratedOrder> order1 = element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020"));
        Element<GeneratedOrder> order2 = element(buildOrder(BLANK_ORDER, "order 2", "16 July 2020"));

        List<Element<GeneratedOrder>> orders = new ArrayList<>(List.of(order1, order2));
        List<Element<GeneratedOrder>> hiddenOrders = new ArrayList<>();

        DynamicList listToRemove = DynamicList.builder()
            .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
            .build();

        String reason = "like really, do they not see the big case number and family man number at the top of the page";

        service.hideOrder(orders, hiddenOrders, listToRemove, reason);

        assertThat(order1.getValue().getRemovalReason()).isEqualTo(reason);
        assertThat(order2.getValue().getRemovalReason()).isNull();
    }

    @Test
    void shouldReturnAMapOfExtractedOrderDetailsWhenIdMatchOrderInList() {
        DocumentReference document = DocumentReference.builder().build();
        String orderTitle = "order title";
        String dateOfIssue = "14 July 2020";
        String dateAndTimeOfUpload = "2:28pm, 31 August 2020";

        Element<GeneratedOrder> order = element(
            buildOrder(BLANK_ORDER, orderTitle, dateOfIssue).toBuilder()
            .date(dateAndTimeOfUpload)
            .document(document)
            .build()
        );

        Map<String, Object> orderFields = service.populateSelectedOrderFields(List.of(order), order.getId());

        Map<String, Object> expectedFields = Map.of(
            "orderToBeRemoved", document,
            "orderTitleToBeRemoved", orderTitle,
            "orderIssuedDateToBeRemoved", dateOfIssue,
            "orderDateToBeRemoved", dateAndTimeOfUpload
        );

        assertThat(orderFields).isEqualTo(expectedFields);
    }

    @Test
    void shouldReturnEmptyMapWhenIdDoesNotMatchOrderInList() {
        DocumentReference document = DocumentReference.builder().build();
        String orderTitle = "order title";
        String dateOfIssue = "14 July 2020";
        String dateAndTimeOfUpload = "2:28pm, 31 August 2020";

        Element<GeneratedOrder> order = element(
            buildOrder(BLANK_ORDER, orderTitle, dateOfIssue).toBuilder()
                .date(dateAndTimeOfUpload)
                .document(document)
                .build()
        );

        Map<String, Object> orderFields = service.populateSelectedOrderFields(List.of(order), UUID.randomUUID());

        assertThat(orderFields).isEmpty();
    }

    @Test
    void shouldModifyChildrenThatHaveBeenAssociatedWithAFinalOrder() {
        List<Element<Child>> childrenList = buildChildrenList();

        Element<GeneratedOrder> order1 = element(buildOrder(
            EMERGENCY_PROTECTION_ORDER,
            "order 1",
            "15 June 2020",
            childrenList
        ));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                .build())
            .build();

        List<Element<Child>> updatedChildren = service.removeFinalOrderPropertiesFromChildren(caseData);

        Child expectedChild = Child.builder().build();

        assertThat(updatedChildren.get(0).getValue()).isEqualTo(expectedChild);
        assertThat(updatedChildren.get(1).getValue()).isEqualTo(expectedChild);
    }

    @Test
    void shouldNotModifyChildrenThatHaveNotBeenAssociatedWithAFinalOrder() {
        List<Element<Child>> childrenList = buildChildrenList();

        Element<GeneratedOrder> order1 = element(buildOrder(
            BLANK_ORDER,
            "order 1",
            "15 June 2020",
            childrenList
        ));

        CaseData caseData = CaseData.builder()
            .children1(childrenList)
            .orderCollection(List.of(order1))
            .removableOrderList(DynamicList.builder()
                .value(buildListElement(order1.getId(), "order 1 - 15 June 2020"))
                .build())
            .build();

        List<Element<Child>> updatedChildren = service.removeFinalOrderPropertiesFromChildren(caseData);

        Child expectedChild = Child.builder().build();

        assertThat(updatedChildren.get(0).getValue()).isNotEqualTo(expectedChild);
        assertThat(updatedChildren.get(1).getValue()).isNotEqualTo(expectedChild);
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .title(title)
            .dateOfIssue(dateOfIssue)
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String dateOfIssue,
                                      List<Element<Child>> children) {
        return buildOrder(type, title, dateOfIssue).toBuilder()
            .children(children)
            .build();
    }

    private List<Element<Child>> buildChildrenList() {
        return List.of(
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .build()),
            element(UUID.randomUUID(), Child.builder()
                .finalOrderIssued("Yes")
                .finalOrderIssuedType("Some type")
                .build())
        );
    }
}
