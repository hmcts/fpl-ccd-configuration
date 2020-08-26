package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

class RemoveOrderServiceTest {

    private final RemoveOrderService service = new RemoveOrderService();

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
    void shouldIgnoreOrdersThatAreNotBlankOrdersWhenCreatingDynamicList() {
        List<Element<GeneratedOrder>> orders = List.of(
            element(buildOrder(BLANK_ORDER, "order 1", "15 June 2020")),
            element(buildOrder(EMERGENCY_PROTECTION_ORDER, null, "16 July 2020"))
        );

        DynamicList listOfOrders = service.buildDynamicListOfOrders(orders);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(orders.get(0).getId(), "order 1 - 15 June 2020")
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

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }

    private GeneratedOrder buildOrder(GeneratedOrderType type, String title, String date) {
        return GeneratedOrder.builder()
            .type(getFullOrderType(type))
            .title(title)
            .dateOfIssue(date)
            .build();
    }
}
